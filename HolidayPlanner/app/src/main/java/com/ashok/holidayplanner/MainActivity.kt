package com.ashok.holidayplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ashok.holidayplanner.ui.auth.AuthFragment
import com.ashok.holidayplanner.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val app = application as HolidayApplication
            val authRepo = app.container.authRepository

            val initialFragment = if (authRepo.isSignedIn()) {
                HomeFragment()
            } else {
                AuthFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit()
        }
    }
}
