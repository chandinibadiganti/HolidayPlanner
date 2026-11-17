package com.ashok.holidayplanner.data.repository

import com.ashok.holidayplanner.data.local.dao.HolidayCacheDao
import com.ashok.holidayplanner.data.remote.NagerApi
import com.ashok.holidayplanner.domain.mapper.HolidayMapper
import com.ashok.holidayplanner.domain.model.Holiday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HolidayRepository(
    private val api: NagerApi,
    private val cacheDao: HolidayCacheDao
) {

    fun getCachedHolidays(countryCode: String, year: Int): Flow<List<Holiday>> {
        val upperCountry = countryCode.uppercase()
        return cacheDao.getCachedHolidays(upperCountry, year).map { entities ->
            entities.map { HolidayMapper.entityToDomain(it) }
        }
    }

    suspend fun getCachedHolidaysSync(countryCode: String, year: Int): List<Holiday> = withContext(Dispatchers.IO) {
        val upperCountry = countryCode.uppercase()
        cacheDao.getCachedHolidaysSync(upperCountry, year).map { HolidayMapper.entityToDomain(it) }
    }

    /**
     * Refreshes holidays from the Nager.Date API, updating Room cache on success.
     * Handles 204 No Content, 404 Not Found, 5xx, timeouts, and empty responses safely.
     */
    suspend fun fetchHolidaysFromRemote(countryCode: String, year: Int): Result<List<Holiday>> = withContext(Dispatchers.IO) {
        val upperCountry = countryCode.uppercase()
        try {
            val response = api.getPublicHolidays(year, upperCountry)

            when (response.code()) {
                200 -> {
                    val body = response.body().orEmpty()
                    val entities = body.mapNotNull { HolidayMapper.dtoToCachedEntity(it, year) }

                    // Cache in Room
                    if (entities.isNotEmpty()) {
                        cacheDao.deleteHolidays(upperCountry, year)
                        cacheDao.insertHolidays(entities)
                    }

                    val holidays = entities.map { HolidayMapper.entityToDomain(it) }
                    Result.success(holidays)
                }
                204 -> {
                    // No Content
                    Result.success(emptyList())
                }
                404 -> {
                    // Unsupported country or year
                    Result.failure(Exception("Holidays are not available for $countryCode in $year (HTTP 404)."))
                }
                else -> {
                    Result.failure(Exception("Failed to fetch holidays (${response.code()}: ${response.message()})."))
                }
            }
        } catch (e: Exception) {
            // Check if we have cached data to fall back on
            val cached = cacheDao.getCachedHolidaysSync(upperCountry, year)
            if (cached.isNotEmpty()) {
                Result.success(cached.map { HolidayMapper.entityToDomain(it) })
            } else {
                Result.failure(e)
            }
        }
    }
}
