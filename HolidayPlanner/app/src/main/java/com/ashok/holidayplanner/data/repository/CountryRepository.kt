package com.ashok.holidayplanner.data.repository

import com.ashok.holidayplanner.data.remote.NagerApi
import com.ashok.holidayplanner.domain.mapper.HolidayMapper
import com.ashok.holidayplanner.domain.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class CountryRepository(
    private val api: NagerApi
) {

    @Volatile
    private var inMemoryCountries: List<Country>? = null

    suspend fun getCountries(forceRefresh: Boolean = false): List<Country> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !inMemoryCountries.isNullOrEmpty()) {
            return@withContext inMemoryCountries!!
        }

        try {
            val response = api.getAvailableCountries()
            if (response.isSuccessful) {
                val dtoList = response.body().orEmpty()
                val mapped = dtoList.mapNotNull { HolidayMapper.countryDtoToDomain(it) }
                    .sortedBy { it.name.lowercase(Locale.ROOT) }
                if (mapped.isNotEmpty()) {
                    inMemoryCountries = mapped
                    return@withContext mapped
                }
            }
        } catch (e: Exception) {
            // Network failure or timeout
        }

        // If in-memory cache existed, return it
        if (!inMemoryCountries.isNullOrEmpty()) {
            return@withContext inMemoryCountries!!
        }

        // Offline fallback list of common countries supported by Nager.Date
        val fallbackList = getFallbackCountries().sortedBy { it.name }
        inMemoryCountries = fallbackList
        return@withContext fallbackList
    }

    fun findCountryByCode(code: String): Country? {
        val upper = code.uppercase(Locale.US)
        return inMemoryCountries?.firstOrNull { it.code.equals(upper, ignoreCase = true) }
    }

    private fun getFallbackCountries(): List<Country> {
        return listOf(
            Country("US", "United States"),
            Country("IN", "India"),
            Country("GB", "United Kingdom"),
            Country("CA", "Canada"),
            Country("AU", "Australia"),
            Country("DE", "Germany"),
            Country("FR", "France"),
            Country("IT", "Italy"),
            Country("ES", "Spain"),
            Country("JP", "Japan"),
            Country("BR", "Brazil"),
            Country("MX", "Mexico"),
            Country("NL", "Netherlands"),
            Country("NZ", "New Zealand"),
            Country("SG", "Singapore"),
            Country("ZA", "South Africa"),
            Country("CH", "Switzerland"),
            Country("SE", "Sweden"),
            Country("NO", "Norway"),
            Country("IE", "Ireland")
        )
    }
}
