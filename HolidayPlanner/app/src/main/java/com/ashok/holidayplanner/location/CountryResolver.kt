package com.ashok.holidayplanner.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.ashok.holidayplanner.data.prefs.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class CountryResolver(
    private val context: Context,
    private val locationProvider: LocationProvider,
    private val settingsStore: SettingsStore
) {

    sealed interface DetectionSource {
        data class GpsLocation(val countryCode: String) : DetectionSource
        data class SavedPreference(val countryCode: String) : DetectionSource
        data class DeviceLocale(val countryCode: String) : DetectionSource
        object None : DetectionSource
    }

    /**
     * Resolves the user's country code (ISO 3166-1 alpha-2, e.g. "US", "IN", "DE")
     * following the strict fallback chain:
     * 1. Fused Location -> Geocoder
     * 2. Saved Country (SettingsStore)
     * 3. Device Locale (Locale.getDefault().country)
     */
    suspend fun resolveCountryCode(): DetectionSource {
        // Step 1: Try GPS & Geocoder if permission is granted
        if (locationProvider.hasLocationPermission()) {
            val location = locationProvider.getCurrentOrLastLocation()
            if (location != null) {
                val detectedCode = getCountryCodeFromCoordinates(location.latitude, location.longitude)
                if (!detectedCode.isNullOrBlank()) {
                    val code = detectedCode.uppercase(Locale.US)
                    settingsStore.saveCountry(code)
                    return DetectionSource.GpsLocation(code)
                }
            }
        }

        // Step 2: Fall back to last saved country
        val saved = settingsStore.getSavedCountry()
        if (!saved.isNullOrBlank()) {
            return DetectionSource.SavedPreference(saved.uppercase(Locale.US))
        }

        // Step 3: Fall back to device locale
        val localeCountry = Locale.getDefault().country
        if (!localeCountry.isNullOrBlank() && localeCountry.length == 2) {
            val code = localeCountry.uppercase(Locale.US)
            settingsStore.saveCountry(code)
            return DetectionSource.DeviceLocale(code)
        }

        return DetectionSource.None
    }

    suspend fun getCountryCodeFromCoordinates(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val countryCode = addresses.firstOrNull()?.countryCode
                            if (continuation.isActive) continuation.resume(countryCode)
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    })
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.countryCode
                } catch (e: IOException) {
                    null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
