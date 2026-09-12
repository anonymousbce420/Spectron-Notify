package com.spectron.notify

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class BotCommandService(private val context: Context) {
    private var running = false
    private var lastUpdate = 0L
    private val executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor()

    fun start() {
        if (running) return
        running = true
        executor.scheduleWithFixedDelay(
            { if (running) poll() },
            0,
            2500,
            java.util.concurrent.TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        running = false
    }

    private fun poll() {
        val token = Prefs.token(context)
        val chatId = Prefs.chatId(context)
        if (token.isBlank() || chatId.isBlank()) return

        try {
            val url = URL(
                "https://api.telegram.org/bot$token/getUpdates?timeout=1&offset=${lastUpdate + 1}"
            )
            val c = url.openConnection() as HttpURLConnection
            c.connectTimeout = 5000
            c.readTimeout = 7000
            val body = c.inputStream.bufferedReader().use { it.readText() }
            c.disconnect()

            val root = JSONObject(body)
            val results = root.optJSONArray("result") ?: return

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                lastUpdate = maxOf(lastUpdate, item.optLong("update_id", lastUpdate))
                val message = item.optJSONObject("message") ?: continue
                val fromChat = message.optJSONObject("chat")?.optString("id") ?: continue
                if (fromChat != chatId) continue

                val command = message.optString("text").trim().lowercase()
                when (command) {
                    "/location" -> requestLocation()
                    "/ring" -> ring()
                    "/status" -> status()
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun requestLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Telegram.send(context, "SPECTRON NOTIFY\n\nLocation permission is not enabled.")
            return
        }

        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationService::class.java)
        )
    }

    private fun ring() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
        Telegram.send(context, "SPECTRON NOTIFY\n\nRing command received. Android may restrict audible playback when the device is silent or in Do Not Disturb mode.")
    }

    private fun status() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        Telegram.send(
            context,
            "SPECTRON NOTIFY\n\n" +
                "Forwarding: ${if (Prefs.enabled(context)) "ON" else "OFF"}\n" +
                "Location provider: ${if (enabled) "ON" else "OFF"}"
        )
    }
}
