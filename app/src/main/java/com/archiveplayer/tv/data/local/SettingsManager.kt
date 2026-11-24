package com.archiveplayer.tv.data.local

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "archive_player_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_TMDB_API_KEY = "tmdb_api_key"
    }

    var tmdbApiKey: String?
        get() = prefs.getString(KEY_TMDB_API_KEY, null)
        set(value) {
            prefs.edit().putString(KEY_TMDB_API_KEY, value).apply()
        }

    fun hasApiKey(): Boolean {
        return !tmdbApiKey.isNullOrBlank()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_TMDB_API_KEY).apply()
    }
}