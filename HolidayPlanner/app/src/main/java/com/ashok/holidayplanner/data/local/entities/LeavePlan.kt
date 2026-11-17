package com.ashok.holidayplanner.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "leave_plans")
data class LeavePlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
