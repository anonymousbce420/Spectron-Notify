package com.spectron.notify

import android.content.Context

object Prefs {
    private const val FILE = "spectron_notify"
    private const val TOKEN = "bot_token"
    private const val CHAT_ID = "chat_id"
    private const val ENABLED = "forwarding_enabled"

    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun token(c: Context) = p(c).getString(TOKEN, "") ?: ""
    fun chatId(c: Context) = p(c).getString(CHAT_ID, "") ?: ""
    fun enabled(c: Context) = p(c).getBoolean(ENABLED, true)

    fun save(c: Context, token: String, chatId: String) {
        p(c).edit().putString(TOKEN, token.trim()).putString(CHAT_ID, chatId.trim()).apply()
    }

    fun setEnabled(c: Context, value: Boolean) {
        p(c).edit().putBoolean(ENABLED, value).apply()
    }
}
