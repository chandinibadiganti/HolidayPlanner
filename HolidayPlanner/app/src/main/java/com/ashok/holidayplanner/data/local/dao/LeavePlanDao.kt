package com.ashok.holidayplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ashok.holidayplanner.data.local.entities.LeavePlan
import kotlinx.coroutines.flow.Flow

@Dao
interface LeavePlanDao {

    @Query("SELECT * FROM leave_plans WHERE userId = :userId ORDER BY startDate ASC")
    fun getLeavePlans(userId: String): Flow<List<LeavePlan>>

    @Query("SELECT * FROM leave_plans WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getLeavePlanById(id: Long, userId: String): LeavePlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeavePlan(plan: LeavePlan): Long

    @Update
    suspend fun updateLeavePlan(plan: LeavePlan): Int

    @Delete
    suspend fun deleteLeavePlan(plan: LeavePlan): Int

    @Query("DELETE FROM leave_plans WHERE id = :id AND userId = :userId")
    suspend fun deleteLeavePlanById(id: Long, userId: String): Int
}
