package com.ashok.holidayplanner.ui.leaves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashok.holidayplanner.data.local.entities.LeavePlan
import com.ashok.holidayplanner.data.prefs.SettingsStore
import com.ashok.holidayplanner.data.repository.AuthRepository
import com.ashok.holidayplanner.data.repository.HolidayRepository
import com.ashok.holidayplanner.data.repository.LeaveRepository
import com.ashok.holidayplanner.domain.model.LeaveWithHolidays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LeavesViewModel(
    private val leaveRepository: LeaveRepository,
    private val holidayRepository: HolidayRepository,
    private val authRepository: AuthRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val userId: String
        get() = authRepository.getUserId()

    private val _lastDeletedPlan = MutableStateFlow<LeavePlan?>(null)
    val lastDeletedPlan: StateFlow<LeavePlan?> = _lastDeletedPlan.asStateFlow()

    // Stream of LeaveWithHolidays
    val leavesWithHolidays: StateFlow<List<LeaveWithHolidays>> = combine(
        leaveRepository.getLeavesForUser(userId),
        holidayRepository.getCachedHolidays(
            countryCode = settingsStore.getSavedCountry() ?: "US",
            year = settingsStore.getSavedYear()
        )
    ) { leaves, holidays ->
        leaves.map { leave ->
            val included = leaveRepository.findHolidaysInLeaveRange(leave.startDate, leave.endDate, holidays)
            LeaveWithHolidays(leavePlan = leave, includedHolidays = included)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveLeave(
        id: Long = 0,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        note: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            onComplete(false, "Title is required")
            return
        }

        if (endDate.isBefore(startDate)) {
            onComplete(false, "End date cannot be before start date")
            return
        }

        viewModelScope.launch {
            try {
                if (id == 0L) {
                    leaveRepository.addLeave(
                        userId = userId,
                        title = trimmedTitle,
                        startDate = startDate,
                        endDate = endDate,
                        note = note
                    )
                } else {
                    val updated = LeavePlan(
                        id = id,
                        userId = userId,
                        title = trimmedTitle,
                        startDate = startDate,
                        endDate = endDate,
                        note = note
                    )
                    leaveRepository.updateLeave(updated)
                }
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to save leave plan")
            }
        }
    }

    suspend fun checkOverlaps(startDate: LocalDate, endDate: LocalDate, excludeId: Long? = null): List<LeavePlan> {
        return leaveRepository.findOverlappingLeaves(userId, startDate, endDate, excludeId)
    }

    fun deleteLeave(plan: LeavePlan) {
        viewModelScope.launch {
            _lastDeletedPlan.value = plan
            leaveRepository.deleteLeave(plan)
        }
    }

    fun undoDelete() {
        val toRestore = _lastDeletedPlan.value ?: return
        viewModelScope.launch {
            leaveRepository.addLeave(
                userId = toRestore.userId,
                title = toRestore.title,
                startDate = toRestore.startDate,
                endDate = toRestore.endDate,
                note = toRestore.note
            )
            _lastDeletedPlan.value = null
        }
    }
}
