package com.ashok.holidayplanner

import com.ashok.holidayplanner.data.remote.dto.CountryDto
import com.ashok.holidayplanner.data.remote.dto.HolidayDto
import com.ashok.holidayplanner.domain.mapper.HolidayMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HolidayMapperTest {

    @Test
    fun testDtoToDomainSuccess() {
        val dto = HolidayDto(
            date = "2026-12-25",
            localName = "Christmas Day",
            name = "Christmas Day",
            countryCode = "US",
            fixed = true,
            global = true,
            counties = null,
            launchYear = 1870,
            types = listOf("Public")
        )

        val domain = HolidayMapper.dtoToDomain(dto)
        assertNotNull(domain)
        assertEquals(LocalDate.of(2026, 12, 25), domain?.date)
        assertEquals("Christmas Day", domain?.name)
        assertEquals("Christmas Day", domain?.localName)
        assertEquals("US", domain?.countryCode)
        assertTrue(domain?.fixed == true)
        assertTrue(domain?.isLongWeekend == true) // Dec 25, 2026 is Friday!
    }

    @Test
    fun testDtoWithNullFieldsHandledGracefully() {
        val dto = HolidayDto(
            date = "2026-07-04",
            localName = null,
            name = null,
            countryCode = null,
            fixed = null,
            global = null,
            counties = null,
            launchYear = null,
            types = null
        )

        val domain = HolidayMapper.dtoToDomain(dto)
        assertNotNull(domain)
        assertEquals("", domain?.name)
        assertEquals("", domain?.localName)
        assertEquals("", domain?.countryCode)
    }

    @Test
    fun testInvalidDateReturnsNull() {
        val dto = HolidayDto(
            date = "invalid-date-string",
            localName = "Sample",
            name = "Sample",
            countryCode = "US"
        )

        val domain = HolidayMapper.dtoToDomain(dto)
        assertNull(domain)
    }

    @Test
    fun testCountryDtoToDomain() {
        val dto = CountryDto(countryCode = "CA", name = "Canada")
        val domain = HolidayMapper.countryDtoToDomain(dto)
        assertNotNull(domain)
        assertEquals("CA", domain?.code)
        assertEquals("Canada", domain?.name)

        val blankDto = CountryDto(countryCode = "", name = "")
        assertNull(HolidayMapper.countryDtoToDomain(blankDto))
    }
}
