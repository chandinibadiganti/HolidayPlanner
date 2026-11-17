package com.ashok.holidayplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ashok.holidayplanner.data.local.entities.CachedHoliday
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayCacheDao {

    @Query("SELECT * FROM cached_holidays WHERE countryCode = :countryCode AND year = :year ORDER BY date ASC")
    fun getCachedHolidays(countryCode: String, year: Int): Flow<List<CachedHoliday>>

    @Query("SELECT * FROM cached_holidays WHERE countryCode = :countryCode AND year = :year ORDER BY date ASC")
    suspend fun getCachedHolidaysSync(countryCode: String, year: Int): List<CachedHoliday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<CachedHoliday>)

    @Query("DELETE FROM cached_holidays WHERE countryCode = :countryCode AND year = :year")
    suspend fun deleteHolidays(countryCode: String, year: Int): Int
}
