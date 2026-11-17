package com.ashok.holidayplanner.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ashok.holidayplanner.data.local.AppDatabase
import com.ashok.holidayplanner.data.prefs.SettingsStore
import com.ashok.holidayplanner.data.remote.NagerApi
import com.ashok.holidayplanner.data.remote.RetrofitProvider
import com.ashok.holidayplanner.data.repository.AuthRepository
import com.ashok.holidayplanner.data.repository.CountryRepository
import com.ashok.holidayplanner.data.repository.HolidayRepository
import com.ashok.holidayplanner.data.repository.LeaveRepository
import com.ashok.holidayplanner.location.CountryResolver
import com.ashok.holidayplanner.location.LocationProvider
import com.ashok.holidayplanner.ui.auth.AuthViewModel
import com.ashok.holidayplanner.ui.holidays.HolidaysViewModel
import com.ashok.holidayplanner.ui.leaves.LeavesViewModel
import com.google.firebase.auth.FirebaseAuth

class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val nagerApi: NagerApi by lazy {
        RetrofitProvider.createNagerApi()
    }

    val settingsStore: SettingsStore by lazy {
        SettingsStore(context)
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, settingsStore)
    }

    val countryRepository: CountryRepository by lazy {
        CountryRepository(nagerApi)
    }

    val holidayRepository: HolidayRepository by lazy {
        HolidayRepository(nagerApi, database.holidayCacheDao())
    }

    val leaveRepository: LeaveRepository by lazy {
        LeaveRepository(database.leavePlanDao())
    }

    val locationProvider: LocationProvider by lazy {
        LocationProvider(context)
    }

    val countryResolver: CountryResolver by lazy {
        CountryResolver(context, locationProvider, settingsStore)
    }

    val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        AuthViewModel(authRepository) as T
                    }
                    modelClass.isAssignableFrom(HolidaysViewModel::class.java) -> {
                        HolidaysViewModel(
                            holidayRepository = holidayRepository,
                            countryRepository = countryRepository,
                            leaveRepository = leaveRepository,
                            countryResolver = countryResolver,
                            settingsStore = settingsStore,
                            authRepository = authRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(LeavesViewModel::class.java) -> {
                        LeavesViewModel(
                            leaveRepository = leaveRepository,
                            holidayRepository = holidayRepository,
                            authRepository = authRepository,
                            settingsStore = settingsStore
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
