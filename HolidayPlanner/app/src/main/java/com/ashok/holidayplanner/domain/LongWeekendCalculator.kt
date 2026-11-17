package com.ashok.holidayplanner.domain

import java.time.DayOfWeek
import java.time.LocalDate

object LongWeekendCalculator {

    data class LongWeekendInfo(
        val isLongWeekend: Boolean,
        val spanDescription: String?
    )

    /**
     * Determines whether a holiday creates a 3-day long weekend.
     * Per requirement: A holiday is a long weekend if its date falls on Friday or Monday.
     */
    fun calculate(date: LocalDate): LongWeekendInfo {
        return when (date.dayOfWeek) {
            DayOfWeek.FRIDAY -> LongWeekendInfo(
                isLongWeekend = true,
                spanDescription = "Fri–Sun (3 days)"
            )
            DayOfWeek.MONDAY -> LongWeekendInfo(
                isLongWeekend = true,
                spanDescription = "Sat–Mon (3 days)"
            )
            else -> LongWeekendInfo(
                isLongWeekend = false,
                spanDescription = null
            )
        }
    }

    /**
     * Calculates bridge opportunity:
     * If a holiday is on Tuesday, suggest bridging Monday (1 working day gap).
     * If a holiday is on Thursday, suggest bridging Friday (1 working day gap).
     * Returns suggested bridge leave range (start, end) or null.
     */
    fun calculateBridgeLeave(date: LocalDate): Pair<LocalDate, LocalDate>? {
        return when (date.dayOfWeek) {
            DayOfWeek.TUESDAY -> {
                // Bridge Monday: Take leave on Monday, enjoy Sat - Tue (4 days)
                val monday = date.minusDays(1)
                Pair(monday, monday)
            }
            DayOfWeek.THURSDAY -> {
                // Bridge Friday: Take leave on Friday, enjoy Thu - Sun (4 days)
                val friday = date.plusDays(1)
                Pair(friday, friday)
            }
            else -> null
        }
    }
}
