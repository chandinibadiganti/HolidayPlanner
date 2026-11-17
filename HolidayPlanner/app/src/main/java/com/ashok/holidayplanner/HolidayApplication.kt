package com.ashok.holidayplanner

import android.app.Application
import com.ashok.holidayplanner.di.AppContainer

class HolidayApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
