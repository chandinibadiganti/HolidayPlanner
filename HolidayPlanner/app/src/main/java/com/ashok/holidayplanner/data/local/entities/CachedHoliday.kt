package com.ashok.holidayplanner.data.local.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity(
    tableName = "cached_holidays",
    primaryKeys = ["countryCode", "year", "date", "name"]
)
data class CachedHoliday(
    val countryCode: String,
    val year: Int,
    val date: LocalDate,
    val localName: String,
    val name: String,
    val fixed: Boolean = false,
    val global: Boolean = false,
    val counties: String? = null,
    val launchYear: Int? = null,
    val types: String? = null
)
