package com.tejpratapsingh.lyricsmaker.data.store

import android.content.Context
import android.os.Build
import androidx.core.content.edit

class RecentSearchHelper(private val context: Context) {
    companion object {
        private const val PREF_NAME = "recent_searches"
        private const val KEY_SEARCHES = "searches"
    }

    fun saveSearch(
        query: String,
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val searches = getSearches().toMutableList()
        searches.remove(query)
        searches.add(0, query)
        if (searches.size > 10) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                searches.removeLast()
            }
        }
        prefs.edit { putStringSet(KEY_SEARCHES, searches.toSet()) }
    }

    fun getSearches(): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SEARCHES, emptySet())?.toList() ?: emptyList()
    }
}
