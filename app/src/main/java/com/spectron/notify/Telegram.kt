package com.spectron.notify

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

object Telegram {
    fun send(c: Context, text: String, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        val token = Prefs.token(c)
        val chatId = Prefs.chatId(c)
        if (token.isBlank() || chatId.isBlank()) {
            callback(false, "Bot Token or Chat ID is missing")
            return
        }

        thread(name = "spectron-telegram") {
            try {
                val url = URL("https://api.telegram.org/bot$token/sendMessage")
                val body = "chat_id=${URLEncoder.encode(chatId, "UTF-8")}" +
                    "&text=${URLEncoder.encode(text, "UTF-8")}" +
                    "&disable_web_page_preview=true"

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 10_000
                conn.readTimeout = 15_000
                conn.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
                )
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                conn.disconnect()
                callback(code in 200..299, if (code in 200..299) "Sent successfully" else "Telegram HTTP $code")
            } catch (e: Exception) {
                callback(false, e.message ?: "Network error")
            }
        }
    }
}
