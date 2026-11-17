package com.ashok.holidayplanner

import com.ashok.holidayplanner.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateValidationTest {

    @Test
    fun testDateRangeInclusiveDays() {
        val start = LocalDate.of(2026, 10, 1)
        val end = LocalDate.of(2026, 10, 5)
        assertEquals(5L, DateUtils.calculateInclusiveDays(start, end))

        val singleDay = LocalDate.of(2026, 10, 1)
        assertEquals(1L, DateUtils.calculateInclusiveDays(singleDay, singleDay))
    }

    @Test
    fun testUtcMillisConversionWithoutOffByOne() {
        val date = LocalDate.of(2026, 8, 15)
        val utcMillis = DateUtils.localDateToUtcMillis(date)
        val converted = DateUtils.utcMillisToLocalDate(utcMillis)

        assertEquals(date, converted)
    }

    @Test
    fun testDateRangeValidation() {
        val start = LocalDate.of(2026, 4, 10)
        val validEnd = LocalDate.of(2026, 4, 12)
        val invalidEnd = LocalDate.of(2026, 4, 8)

        assertFalse(validEnd.isBefore(start))
        assertTrue(invalidEnd.isBefore(start))
    }

    @Test
    fun testFormatRange() {
        val start = LocalDate.of(2026, 1, 10)
        val end = LocalDate.of(2026, 1, 15)
        val range = DateUtils.formatRange(start, end)
        assertTrue(range.contains("10 Jan 2026"))
        assertTrue(range.contains("15 Jan 2026"))
    }
}
