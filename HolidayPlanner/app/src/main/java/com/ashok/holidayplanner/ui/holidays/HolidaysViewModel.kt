package com.ashok.holidayplanner.ui.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashok.holidayplanner.data.prefs.SettingsStore
import com.ashok.holidayplanner.data.repository.AuthRepository
import com.ashok.holidayplanner.data.repository.CountryRepository
import com.ashok.holidayplanner.data.repository.HolidayRepository
import com.ashok.holidayplanner.data.repository.LeaveRepository
import com.ashok.holidayplanner.domain.UpcomingHolidayFinder
import com.ashok.holidayplanner.domain.model.Country
import com.ashok.holidayplanner.domain.model.Holiday
import com.ashok.holidayplanner.location.CountryResolver
import com.ashok.holidayplanner.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HolidaysViewModel(
    private val holidayRepository: HolidayRepository,
    private val countryRepository: CountryRepository,
    private val leaveRepository: LeaveRepository,
    private val countryResolver: CountryResolver,
    private val settingsStore: SettingsStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _selectedYear = MutableStateFlow(settingsStore.getSavedYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _isLongWeekendsOnly = MutableStateFlow(false)
    val isLongWeekendsOnly: StateFlow<Boolean> = _isLongWeekendsOnly.asStateFlow()

    private val _rawHolidays = MutableStateFlow<List<Holiday>>(emptyList())

    private val _holidaysUiState = MutableStateFlow<UiState<List<Holiday>>>(UiState.Loading)
    val holidaysUiState: StateFlow<UiState<List<Holiday>>> = _holidaysUiState.asStateFlow()

    private val _unsupportedCountryMessage = MutableStateFlow<String?>(null)
    val unsupportedCountryMessage: StateFlow<String?> = _unsupportedCountryMessage.asStateFlow()

    private val userId: String
        get() = authRepository.getUserId()

    // Combined stream of holidays with leave overlaps & filter applied
    val displayedHolidays: StateFlow<List<Holiday>> = combine(
        _rawHolidays,
        _isLongWeekendsOnly,
        leaveRepository.getLeavesForUser(userId)
    ) { holidays, longOnly, userLeaves ->
        val enriched = holidays.map { holiday ->
            val overlaps = userLeaves.filter { leave ->
                !holiday.date.isBefore(leave.startDate) && !holiday.date.isAfter(leave.endDate)
            }
            holiday.copy(
                isOverlappingLeave = overlaps.isNotEmpty(),
                overlappingLeaveTitles = overlaps.map { it.title }
            )
        }

        if (longOnly) {
            enriched.filter { it.isLongWeekend }
        } else {
            enriched
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Upcoming holiday calculation based on all holidays in the year
    val upcomingResult: StateFlow<UpcomingHolidayFinder.UpcomingResult> = combine(
        _rawHolidays,
        _selectedYear
    ) { holidays, year ->
        UpcomingHolidayFinder.find(
            holidays = holidays,
            today = LocalDate.now(),
            selectedYear = year
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UpcomingHolidayFinder.UpcomingResult.None
    )

    init {
        initializeCountriesAndLocation()
    }

    fun initializeCountriesAndLocation() {
        viewModelScope.launch {
            _holidaysUiState.value = UiState.Loading
            val countryList = countryRepository.getCountries()
            _countries.value = countryList

            // Resolve country via location -> saved -> locale
            val detection = countryResolver.resolveCountryCode()
            val detectedCode = when (detection) {
                is CountryResolver.DetectionSource.GpsLocation -> detection.countryCode
                is CountryResolver.DetectionSource.SavedPreference -> detection.countryCode
                is CountryResolver.DetectionSource.DeviceLocale -> detection.countryCode
                CountryResolver.DetectionSource.None -> "US"
            }

            val matched = countryList.firstOrNull { it.code.equals(detectedCode, ignoreCase = true) }
            if (matched != null) {
                _unsupportedCountryMessage.value = null
                selectCountry(matched)
            } else {
                _unsupportedCountryMessage.value =
                    "Country code '$detectedCode' is not available in Nager.Date. Please choose a country from the dropdown."
                // Default to first country in list if available
                countryList.firstOrNull()?.let { selectCountry(it) }
            }
        }
    }

    fun onCountrySelected(country: Country) {
        if (_selectedCountry.value?.code == country.code) return
        _unsupportedCountryMessage.value = null
        selectCountry(country)
    }

    private fun selectCountry(country: Country) {
        _selectedCountry.value = country
        settingsStore.saveCountry(country.code)
        loadHolidays(country.code, _selectedYear.value)
    }

    fun onYearSelected(year: Int) {
        if (_selectedYear.value == year) return
        _selectedYear.value = year
        settingsStore.saveYear(year)
        _selectedCountry.value?.let { country ->
            loadHolidays(country.code, year)
        }
    }

    fun setFilterLongWeekendsOnly(enabled: Boolean) {
        _isLongWeekendsOnly.value = enabled
    }

    fun refreshHolidays() {
        _selectedCountry.value?.let { country ->
            loadHolidays(country.code, _selectedYear.value, forceRefresh = true)
        }
    }

    private fun loadHolidays(countryCode: String, year: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _holidaysUiState.value = UiState.Loading

            // First check if we have cached holidays in Room
            val cached = holidayRepository.getCachedHolidaysSync(countryCode, year)
            if (cached.isNotEmpty() && !forceRefresh) {
                _rawHolidays.value = cached
                _holidaysUiState.value = UiState.Success(cached)
            }

            // Fetch from remote
            val remoteResult = holidayRepository.fetchHolidaysFromRemote(countryCode, year)
            remoteResult.onSuccess { list ->
                _rawHolidays.value = list
                if (list.isEmpty()) {
                    _holidaysUiState.value = UiState.Empty
                } else {
                    _holidaysUiState.value = UiState.Success(list)
                }
            }.onFailure { error ->
                if (_rawHolidays.value.isNotEmpty()) {
                    // We already have cached data, don't show full error screen
                    _holidaysUiState.value = UiState.Success(_rawHolidays.value)
                } else {
                    _holidaysUiState.value = UiState.Error(
                        message = error.localizedMessage ?: "Failed to load public holidays."
                    )
                }
            }
        }
    }
}
