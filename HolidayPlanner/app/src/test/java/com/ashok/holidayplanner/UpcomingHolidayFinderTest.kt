package com.ashok.holidayplanner

import com.ashok.holidayplanner.domain.UpcomingHolidayFinder
import com.ashok.holidayplanner.domain.model.Holiday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UpcomingHolidayFinderTest {

    private val holidays = listOf(
        Holiday(
            date = LocalDate.of(2026, 1, 26),
            localName = "Republic Day",
            name = "Republic Day",
            countryCode = "IN"
        ),
        Holiday(
            date = LocalDate.of(2026, 8, 15),
            localName = "Independence Day",
            name = "Independence Day",
            countryCode = "IN"
        ),
        Holiday(
            date = LocalDate.of(2026, 10, 2),
            localName = "Gandhi Jayanti",
            name = "Gandhi Jayanti",
            countryCode = "IN"
        )
    )

    @Test
    fun testFindUpcomingInCurrentYear() {
        // Today is June 1st, 2026 -> next holiday should be Aug 15
        val today = LocalDate.of(2026, 6, 1)
        val result = UpcomingHolidayFinder.find(holidays, today = today, selectedYear = 2026)

        assertTrue(result is UpcomingHolidayFinder.UpcomingResult.Highlighted)
        val highlighted = result as UpcomingHolidayFinder.UpcomingResult.Highlighted
        assertEquals("Independence Day", highlighted.holiday.name)
        assertEquals(75L, highlighted.daysLeft)
        assertEquals("75 days left", highlighted.badgeText)
    }

    @Test
    fun testTodayAndTomorrowDaysLeftFormatting() {
        assertEquals("Today!", UpcomingHolidayFinder.formatDaysLeft(0))
        assertEquals("Tomorrow", UpcomingHolidayFinder.formatDaysLeft(1))
        assertEquals("5 days left", UpcomingHolidayFinder.formatDaysLeft(5))
        assertEquals("Passed", UpcomingHolidayFinder.formatDaysLeft(-1))
    }

    @Test
    fun testPastYearReturnsAllPassed() {
        val today = LocalDate.of(2026, 6, 1)
        val result = UpcomingHolidayFinder.find(holidays, today = today, selectedYear = 2025)

        assertTrue(result is UpcomingHolidayFinder.UpcomingResult.AllPassed)
    }

    @Test
    fun testFutureYearHighlightsFirstHoliday() {
        val today = LocalDate.of(2026, 6, 1)
        val futureHolidays = listOf(
            Holiday(
                date = LocalDate.of(2027, 1, 1),
                localName = "New Year's Day",
                name = "New Year's Day",
                countryCode = "US"
            )
        )
        val result = UpcomingHolidayFinder.find(futureHolidays, today = today, selectedYear = 2027)

        assertTrue(result is UpcomingHolidayFinder.UpcomingResult.Highlighted)
        val highlighted = result as UpcomingHolidayFinder.UpcomingResult.Highlighted
        assertEquals("New Year's Day", highlighted.holiday.name)
    }

    @Test
    fun testEmptyHolidaysListReturnsNone() {
        val result = UpcomingHolidayFinder.find(emptyList(), today = LocalDate.now(), selectedYear = 2026)
        assertTrue(result is UpcomingHolidayFinder.UpcomingResult.None)
    }
}
