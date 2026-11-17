package com.ashok.holidayplanner

import com.ashok.holidayplanner.domain.LongWeekendCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LongWeekendCalculatorTest {

    @Test
    fun testFridayIsLongWeekend() {
        // 2026-05-01 is Friday
        val friday = LocalDate.of(2026, 5, 1)
        val result = LongWeekendCalculator.calculate(friday)

        assertTrue(result.isLongWeekend)
        assertEquals("Fri–Sun (3 days)", result.spanDescription)
    }

    @Test
    fun testMondayIsLongWeekend() {
        // 2026-05-25 is Monday
        val monday = LocalDate.of(2026, 5, 25)
        val result = LongWeekendCalculator.calculate(monday)

        assertTrue(result.isLongWeekend)
        assertEquals("Sat–Mon (3 days)", result.spanDescription)
    }

    @Test
    fun testMidWeekDaysAreNotLongWeekends() {
        // Tuesday
        assertFalse(LongWeekendCalculator.calculate(LocalDate.of(2026, 5, 5)).isLongWeekend)
        // Wednesday
        assertFalse(LongWeekendCalculator.calculate(LocalDate.of(2026, 5, 6)).isLongWeekend)
        // Thursday
        assertFalse(LongWeekendCalculator.calculate(LocalDate.of(2026, 5, 7)).isLongWeekend)
        // Saturday
        assertFalse(LongWeekendCalculator.calculate(LocalDate.of(2026, 5, 2)).isLongWeekend)
        // Sunday
        assertFalse(LongWeekendCalculator.calculate(LocalDate.of(2026, 5, 3)).isLongWeekend)
    }

    @Test
    fun testBridgeOpportunities() {
        // Tuesday holiday: 2026-05-05 -> bridges Monday 2026-05-04
        val tuesday = LocalDate.of(2026, 5, 5)
        val bridgeTue = LongWeekendCalculator.calculateBridgeLeave(tuesday)
        assertNotNull(bridgeTue)
        assertEquals(LocalDate.of(2026, 5, 4), bridgeTue?.first)

        // Thursday holiday: 2026-05-07 -> bridges Friday 2026-05-08
        val thursday = LocalDate.of(2026, 5, 7)
        val bridgeThu = LongWeekendCalculator.calculateBridgeLeave(thursday)
        assertNotNull(bridgeThu)
        assertEquals(LocalDate.of(2026, 5, 8), bridgeThu?.first)

        // Wednesday holiday -> no adjacent weekend bridge
        val wednesday = LocalDate.of(2026, 5, 6)
        assertNull(LongWeekendCalculator.calculateBridgeLeave(wednesday))
    }
}
