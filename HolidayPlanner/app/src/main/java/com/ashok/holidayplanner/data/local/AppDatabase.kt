package com.ashok.holidayplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ashok.holidayplanner.data.local.converters.Converters
import com.ashok.holidayplanner.data.local.dao.HolidayCacheDao
import com.ashok.holidayplanner.data.local.dao.LeavePlanDao
import com.ashok.holidayplanner.data.local.entities.CachedHoliday
import com.ashok.holidayplanner.data.local.entities.LeavePlan

@Database(
    entities = [
        LeavePlan::class,
        CachedHoliday::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leavePlanDao(): LeavePlanDao
    abstract fun holidayCacheDao(): HolidayCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "holiday_planner.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
