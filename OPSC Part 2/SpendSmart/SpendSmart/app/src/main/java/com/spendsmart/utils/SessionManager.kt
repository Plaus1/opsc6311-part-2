package com.spendsmart.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "SpendSmartSession"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_LAST_LOG_DATE = "last_log_date"
        private const val KEY_TOTAL_BADGES = "total_badges"
    }

    fun saveSession(userId: Long, username: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    fun setStreak(days: Int) = prefs.edit().putInt(KEY_STREAK, days).apply()

    fun getLastLogDate(): Long = prefs.getLong(KEY_LAST_LOG_DATE, 0L)

    fun setLastLogDate(date: Long) = prefs.edit().putLong(KEY_LAST_LOG_DATE, date).apply()

    fun getTotalBadges(): Int = prefs.getInt(KEY_TOTAL_BADGES, 0)

    fun setTotalBadges(count: Int) = prefs.edit().putInt(KEY_TOTAL_BADGES, count).apply()
}
