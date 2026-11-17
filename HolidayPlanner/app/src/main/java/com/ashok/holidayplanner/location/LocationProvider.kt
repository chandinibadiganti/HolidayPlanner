package com.ashok.holidayplanner.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentOrLastLocation(): Location? {
        if (!hasLocationPermission()) return null

        val cts = CancellationTokenSource()

        return try {
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            ).await()

            currentLocation ?: fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            try {
                fusedLocationClient.lastLocation.await()
            } catch (fallbackException: Exception) {
                null
            }
        }
    }
}
