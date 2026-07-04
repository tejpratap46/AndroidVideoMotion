package com.tejpratapsingh.motionstore.infra

import android.content.Context
import android.content.SharedPreferences
import com.tejpratapsingh.motionstore.dao.MotionProjectDao

class PreferenceManager(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lyrics_maker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SORT_ORDER = "project_sort_order"
        val DEFAULT_SORT_ORDER = MotionProjectDao.COL_UPDATED
    }

    var projectSortOrder: String
        get() = prefs.getString(KEY_SORT_ORDER, DEFAULT_SORT_ORDER) ?: DEFAULT_SORT_ORDER
        set(value) {
            prefs.edit().putString(KEY_SORT_ORDER, value).apply()
        }
}
