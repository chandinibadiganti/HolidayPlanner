package com.ashok.holidayplanner.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    private val SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun parseIsoDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, ISO_DATE_FORMATTER)
    }

    fun formatToDisplay(date: LocalDate): String {
        return date.format(DISPLAY_DATE_FORMATTER)
    }

    fun formatToShortDisplay(date: LocalDate): String {
        return date.format(SHORT_DATE_FORMATTER)
    }

    fun formatRange(startDate: LocalDate, endDate: LocalDate): String {
        return if (startDate == endDate) {
            formatToDisplay(startDate)
        } else {
            "${formatToShortDisplay(startDate)} – ${formatToShortDisplay(endDate)}"
        }
    }

    /**
     * Converts MaterialDatePicker UTC millis to LocalDate accurately without timezone shift bugs.
     */
    fun utcMillisToLocalDate(utcMillis: Long): LocalDate {
        return Instant.ofEpochMilli(utcMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }

    /**
     * Converts LocalDate to UTC millis for MaterialDatePicker initialization.
     */
    fun localDateToUtcMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    /**
     * Calculates days left from 'today' to 'targetDate'.
     */
    fun daysBetween(from: LocalDate, to: LocalDate): Long {
        return ChronoUnit.DAYS.between(from, to)
    }

    /**
     * Returns true if the holiday is adjacent to a weekend (Friday or Monday).
     */
    fun isLongWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.FRIDAY || date.dayOfWeek == DayOfWeek.MONDAY
    }

    /**
     * Returns the description of the long weekend span.
     */
    fun getLongWeekendSpan(date: LocalDate): String? {
        return when (date.dayOfWeek) {
            DayOfWeek.FRIDAY -> "Fri–Sun (3 days)"
            DayOfWeek.MONDAY -> "Sat–Mon (3 days)"
            else -> null
        }
    }

    /**
     * Calculates total days inclusive in a leave range.
     */
    fun calculateInclusiveDays(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1
    }
}
