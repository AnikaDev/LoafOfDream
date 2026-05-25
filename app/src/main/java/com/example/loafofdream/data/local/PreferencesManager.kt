package com.example.loafofdream.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.loafofdream.data.api.RetrofitClient

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("loafofdream_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
            RetrofitClient.setToken(value)
        }

    var userRole: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_NAME, null)
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    fun isLoggedIn() = token != null

    fun logout() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_ROLE).remove(KEY_NAME).remove(KEY_USER_ID).apply()
        RetrofitClient.setToken(null)
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
