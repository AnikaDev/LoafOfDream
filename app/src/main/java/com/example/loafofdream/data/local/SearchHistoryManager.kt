package com.example.loafofdream.data.local

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("search_history", Context.MODE_PRIVATE)

    fun getHistory(): List<String> {
        val raw = prefs.getString(KEY_HISTORY, "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split(DELIMITER)
    }

    fun addToHistory(query: String) {
        if (query.isBlank()) return
        val current = getHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        val trimmed = current.take(MAX_HISTORY_SIZE)
        prefs.edit().putString(KEY_HISTORY, trimmed.joinToString(DELIMITER)).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val KEY_HISTORY = "history"
        private const val DELIMITER = "|||"
        private const val MAX_HISTORY_SIZE = 10
    }
}
