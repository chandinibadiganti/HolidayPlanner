package com.ashok.holidayplanner.data.prefs

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "holiday_planner_prefs"
        private const val KEY_SAVED_COUNTRY = "saved_country_code"
        private const val KEY_SAVED_YEAR = "saved_year"
        private const val KEY_IS_GUEST = "is_guest_mode"
        private const val KEY_GUEST_NAME = "guest_user_name"
    }

    fun saveCountry(countryCode: String) {
        prefs.edit().putString(KEY_SAVED_COUNTRY, countryCode.uppercase().trim()).apply()
    }

    fun getSavedCountry(): String? {
        return prefs.getString(KEY_SAVED_COUNTRY, null)
    }

    fun saveYear(year: Int) {
        prefs.edit().putInt(KEY_SAVED_YEAR, year).apply()
    }

    fun getSavedYear(): Int {
        val currentYear = LocalDate.now().year
        return prefs.getInt(KEY_SAVED_YEAR, currentYear)
    }

    fun setGuestMode(isGuest: Boolean, guestName: String = "Guest User") {
        prefs.edit()
            .putBoolean(KEY_IS_GUEST, isGuest)
            .putString(KEY_GUEST_NAME, guestName)
            .apply()
    }

    fun isGuestMode(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }

    fun getGuestName(): String {
        return prefs.getString(KEY_GUEST_NAME, "Guest User") ?: "Guest User"
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
