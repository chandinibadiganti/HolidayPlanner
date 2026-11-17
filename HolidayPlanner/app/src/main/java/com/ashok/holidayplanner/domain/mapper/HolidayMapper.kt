package com.ashok.holidayplanner.domain.mapper

import com.ashok.holidayplanner.data.local.entities.CachedHoliday
import com.ashok.holidayplanner.data.remote.dto.CountryDto
import com.ashok.holidayplanner.data.remote.dto.HolidayDto
import com.ashok.holidayplanner.domain.LongWeekendCalculator
import com.ashok.holidayplanner.domain.model.Country
import com.ashok.holidayplanner.domain.model.Holiday
import com.ashok.holidayplanner.util.DateUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

object HolidayMapper {

    private val gson = Gson()

    fun dtoToDomain(dto: HolidayDto): Holiday? {
        val dateString = dto.date ?: return null
        val date = try {
            DateUtils.parseIsoDate(dateString)
        } catch (e: Exception) {
            return null
        }

        val weekendInfo = LongWeekendCalculator.calculate(date)
        val bridge = LongWeekendCalculator.calculateBridgeLeave(date)

        return Holiday(
            date = date,
            localName = dto.localName?.trim().orEmpty(),
            name = dto.name?.trim().orEmpty(),
            countryCode = dto.countryCode?.trim().orEmpty(),
            fixed = dto.fixed ?: false,
            global = dto.global ?: false,
            counties = dto.counties,
            launchYear = dto.launchYear,
            types = dto.types,
            isLongWeekend = weekendInfo.isLongWeekend,
            longWeekendSpan = weekendInfo.spanDescription,
            bridgeSuggestion = bridge
        )
    }

    fun dtoToCachedEntity(dto: HolidayDto, year: Int): CachedHoliday? {
        val dateString = dto.date ?: return null
        val date = try {
            DateUtils.parseIsoDate(dateString)
        } catch (e: Exception) {
            return null
        }

        val countryCode = dto.countryCode?.trim().orEmpty()
        val name = dto.name?.trim().orEmpty()
        val localName = dto.localName?.trim().orEmpty()

        return CachedHoliday(
            countryCode = countryCode,
            year = year,
            date = date,
            localName = localName,
            name = name,
            fixed = dto.fixed ?: false,
            global = dto.global ?: false,
            counties = if (dto.counties != null) gson.toJson(dto.counties) else null,
            launchYear = dto.launchYear,
            types = if (dto.types != null) gson.toJson(dto.types) else null
        )
    }

    fun entityToDomain(entity: CachedHoliday): Holiday {
        val weekendInfo = LongWeekendCalculator.calculate(entity.date)
        val bridge = LongWeekendCalculator.calculateBridgeLeave(entity.date)

        val countiesList: List<String>? = if (!entity.counties.isNullOrBlank()) {
            try {
                gson.fromJson(entity.counties, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                null
            }
        } else null

        val typesList: List<String>? = if (!entity.types.isNullOrBlank()) {
            try {
                gson.fromJson(entity.types, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                null
            }
        } else null

        return Holiday(
            date = entity.date,
            localName = entity.localName,
            name = entity.name,
            countryCode = entity.countryCode,
            fixed = entity.fixed,
            global = entity.global,
            counties = countiesList,
            launchYear = entity.launchYear,
            types = typesList,
            isLongWeekend = weekendInfo.isLongWeekend,
            longWeekendSpan = weekendInfo.spanDescription,
            bridgeSuggestion = bridge
        )
    }

    fun countryDtoToDomain(dto: CountryDto): Country? {
        val code = dto.countryCode?.trim().orEmpty()
        val name = dto.name?.trim().orEmpty()
        if (code.isBlank() || name.isBlank()) return null
        return Country(code = code, name = name)
    }
}
