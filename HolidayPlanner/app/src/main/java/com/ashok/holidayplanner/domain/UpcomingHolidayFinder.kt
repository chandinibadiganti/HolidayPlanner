package com.ashok.holidayplanner.domain

import com.ashok.holidayplanner.domain.model.Holiday
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object UpcomingHolidayFinder {

    sealed interface UpcomingResult {
        data class Highlighted(
            val holiday: Holiday,
            val daysLeft: Long,
            val badgeText: String
        ) : UpcomingResult

        object AllPassed : UpcomingResult
        object None : UpcomingResult
    }

    /**
     * Determines the next upcoming holiday based on the selected year and today's date.
     *
     * Rules:
     * - "Upcoming" means date >= today in the selected year.
     * - If viewing a past year (or all holidays in current year have passed), returns AllPassed.
     * - If viewing a future year, highlights the first holiday of that year.
     * - Days left: 0 -> "Today!", 1 -> "Tomorrow", otherwise "X days left".
     */
    fun find(
        holidays: List<Holiday>,
        today: LocalDate = LocalDate.now(),
        selectedYear: Int = today.year
    ): UpcomingResult {
        if (holidays.isEmpty()) return UpcomingResult.None

        val sorted = holidays.sortedBy { it.date }

        return when {
            selectedYear < today.year -> {
                UpcomingResult.AllPassed
            }
            selectedYear > today.year -> {
                val first = sorted.first()
                val days = ChronoUnit.DAYS.between(today, first.date)
                UpcomingResult.Highlighted(
                    holiday = first,
                    daysLeft = days,
                    badgeText = formatDaysLeft(days)
                )
            }
            else -> {
                val next = sorted.firstOrNull { !it.date.isBefore(today) }
                if (next != null) {
                    val days = ChronoUnit.DAYS.between(today, next.date)
                    UpcomingResult.Highlighted(
                        holiday = next,
                        daysLeft = days,
                        badgeText = formatDaysLeft(days)
                    )
                } else {
                    UpcomingResult.AllPassed
                }
            }
        }
    }

    fun formatDaysLeft(days: Long): String {
        return when {
            days < 0 -> "Passed"
            days == 0L -> "Today!"
            days == 1L -> "Tomorrow"
            else -> "$days days left"
        }
    }
}
