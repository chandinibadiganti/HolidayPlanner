package com.ashok.holidayplanner.data.remote

import com.ashok.holidayplanner.data.remote.dto.CountryDto
import com.ashok.holidayplanner.data.remote.dto.HolidayDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NagerApi {

    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): Response<List<HolidayDto>>

    @GET("api/v3/AvailableCountries")
    suspend fun getAvailableCountries(): Response<List<CountryDto>>
}
