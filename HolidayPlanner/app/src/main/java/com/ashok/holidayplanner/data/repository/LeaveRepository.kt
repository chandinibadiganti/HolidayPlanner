package com.ashok.holidayplanner.data.repository

import com.ashok.holidayplanner.data.local.dao.LeavePlanDao
import com.ashok.holidayplanner.data.local.entities.LeavePlan
import com.ashok.holidayplanner.domain.model.Holiday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate

class LeaveRepository(
    private val leavePlanDao: LeavePlanDao
) {

    fun getLeavesForUser(userId: String): Flow<List<LeavePlan>> {
        return leavePlanDao.getLeavePlans(userId)
    }

    suspend fun getLeaveById(id: Long, userId: String): LeavePlan? = withContext(Dispatchers.IO) {
        leavePlanDao.getLeavePlanById(id, userId)
    }

    suspend fun addLeave(
        userId: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        note: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val plan = LeavePlan(
            userId = userId,
            title = title.trim(),
            startDate = startDate,
            endDate = endDate,
            note = note?.trim()?.ifBlank { null }
        )
        leavePlanDao.insertLeavePlan(plan)
    }

    suspend fun updateLeave(plan: LeavePlan) = withContext(Dispatchers.IO) {
        leavePlanDao.updateLeavePlan(plan)
    }

    suspend fun deleteLeave(plan: LeavePlan) = withContext(Dispatchers.IO) {
        leavePlanDao.deleteLeavePlan(plan)
    }

    suspend fun deleteLeaveById(id: Long, userId: String) = withContext(Dispatchers.IO) {
        leavePlanDao.deleteLeavePlanById(id, userId)
    }

    suspend fun findOverlappingLeaves(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        excludeId: Long? = null
    ): List<LeavePlan> = withContext(Dispatchers.IO) {
        val allLeaves = leavePlanDao.getLeavePlans(userId).firstOrNull().orEmpty()
        allLeaves.filter { existing ->
            if (excludeId != null && existing.id == excludeId) return@filter false
            // Overlap condition: start <= existing.end && end >= existing.start
            !startDate.isAfter(existing.endDate) && !endDate.isBefore(existing.startDate)
        }
    }

    /**
     * Determines which holidays fall inside a leave range [startDate, endDate].
     */
    fun findHolidaysInLeaveRange(
        startDate: LocalDate,
        endDate: LocalDate,
        holidays: List<Holiday>
    ): List<Holiday> {
        return holidays.filter { holiday ->
            !holiday.date.isBefore(startDate) && !holiday.date.isAfter(endDate)
        }
    }
}
