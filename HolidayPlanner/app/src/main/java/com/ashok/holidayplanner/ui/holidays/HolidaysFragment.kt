package com.ashok.holidayplanner.ui.holidays

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ashok.holidayplanner.HolidayApplication
import com.ashok.holidayplanner.R
import com.ashok.holidayplanner.databinding.FragmentHolidaysBinding
import com.ashok.holidayplanner.domain.UpcomingHolidayFinder
import com.ashok.holidayplanner.domain.model.Country
import com.ashok.holidayplanner.domain.model.Holiday
import com.ashok.holidayplanner.ui.common.UiState
import com.ashok.holidayplanner.ui.leaves.LeaveEditorDialogFragment
import com.ashok.holidayplanner.util.DateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate

class HolidaysFragment : Fragment() {

    private var _binding: FragmentHolidaysBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HolidaysViewModel by activityViewModels {
        (requireActivity().application as HolidayApplication).container.viewModelFactory
    }

    private lateinit var holidayAdapter: HolidayAdapter
    private var countryListAdapter: ArrayAdapter<Country>? = null
    private var yearListAdapter: ArrayAdapter<Int>? = null

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.initializeCountriesAndLocation()
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.location_permission_denied),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHolidaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupYearSpinner()
        setupFilters()
        setupSwipeRefresh()
        checkLocationPermission()
        observeViewModel()
    }

    private fun checkLocationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.location_rationale_title)
                    .setMessage(R.string.location_rationale_message)
                    .setPositiveButton(R.string.permission_grant) { _, _ ->
                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    .setNegativeButton(R.string.permission_deny, null)
                    .show()
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private fun setupRecyclerView() {
        holidayAdapter = HolidayAdapter(
            onBridgeClickListener = { holiday, bridgeDates ->
                openBridgeLeaveEditor(holiday, bridgeDates)
            }
        )
        binding.rvHolidays.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = holidayAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupYearSpinner() {
        val currentYear = LocalDate.now().year
        val years = ((currentYear - 5)..(currentYear + 5)).toList()

        yearListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            years
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerYear.adapter = yearListAdapter

        val selectedYear = viewModel.selectedYear.value
        val defaultIndex = years.indexOf(selectedYear).coerceAtLeast(0)
        binding.spinnerYear.setSelection(defaultIndex, false)

        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = years[position]
                viewModel.onYearSelected(year)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val longOnly = checkedIds.contains(R.id.chipFilterLongWeekends)
            viewModel.setFilterLongWeekendsOnly(longOnly)
        }

        binding.btnRetryHolidays.setOnClickListener {
            viewModel.refreshHolidays()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshHolidays.setOnRefreshListener {
            viewModel.refreshHolidays()
        }
    }

    private fun openBridgeLeaveEditor(holiday: Holiday, bridgeDates: Pair<LocalDate, LocalDate>) {
        val holidayName = holiday.localName.ifBlank { holiday.name }
        val dialog = LeaveEditorDialogFragment.newInstance(
            initialTitle = "Bridge $holidayName",
            initialStartDate = bridgeDates.first,
            initialEndDate = bridgeDates.second,
            initialNote = "Long weekend extension around $holidayName"
        )
        dialog.show(parentFragmentManager, "LeaveEditorDialogFragment")
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe country list
                launch {
                    viewModel.countries.collect { countries ->
                        if (countries.isNotEmpty()) {
                            countryListAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                countries
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            binding.spinnerCountry.adapter = countryListAdapter

                            viewModel.selectedCountry.value?.let { current ->
                                val index = countries.indexOfFirst { it.code == current.code }
                                if (index >= 0) binding.spinnerCountry.setSelection(index, false)
                            }

                            binding.spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    viewModel.onCountrySelected(countries[position])
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }
                        }
                    }
                }

                // Observe selected country
                launch {
                    viewModel.selectedCountry.collect { country ->
                        country?.let {
                            val adapter = countryListAdapter ?: return@collect
                            val count = adapter.count
                            for (i in 0 until count) {
                                if (adapter.getItem(i)?.code == it.code) {
                                    if (binding.spinnerCountry.selectedItemPosition != i) {
                                        binding.spinnerCountry.setSelection(i, false)
                                    }
                                    break
                                }
                            }
                        }
                    }
                }

                // Observe unsupported country notice
                launch {
                    viewModel.unsupportedCountryMessage.collect { msg ->
                        if (msg != null) {
                            binding.tvCountryNotice.text = msg
                            binding.tvCountryNotice.visibility = View.VISIBLE
                        } else {
                            binding.tvCountryNotice.visibility = View.GONE
                        }
                    }
                }

                // Observe filtered & enriched holidays list
                launch {
                    viewModel.displayedHolidays.collect { holidays ->
                        holidayAdapter.submitList(holidays)
                    }
                }

                // Observe upcoming holiday card
                launch {
                    viewModel.upcomingResult.collect { result ->
                        when (result) {
                            is UpcomingHolidayFinder.UpcomingResult.Highlighted -> {
                                binding.cardUpcomingHoliday.visibility = View.VISIBLE
                                binding.cardAllPassed.visibility = View.GONE

                                val h = result.holiday
                                binding.tvUpcomingHolidayName.text = if (h.name.isNotBlank()) h.name else h.localName
                                if (h.localName.isNotBlank() && !h.localName.equals(h.name, ignoreCase = true)) {
                                    binding.tvUpcomingHolidayLocalName.text = h.localName
                                    binding.tvUpcomingHolidayLocalName.visibility = View.VISIBLE
                                } else {
                                    binding.tvUpcomingHolidayLocalName.visibility = View.GONE
                                }
                                binding.tvUpcomingHolidayDate.text = DateUtils.formatToDisplay(h.date)
                                binding.tvUpcomingDaysLeftBadge.text = result.badgeText

                                val bridge = h.bridgeSuggestion
                                if (bridge != null) {
                                    binding.btnUpcomingBridge.visibility = View.VISIBLE
                                    binding.btnUpcomingBridge.setOnClickListener {
                                        openBridgeLeaveEditor(h, bridge)
                                    }
                                } else {
                                    binding.btnUpcomingBridge.visibility = View.GONE
                                }
                            }
                            UpcomingHolidayFinder.UpcomingResult.AllPassed -> {
                                binding.cardUpcomingHoliday.visibility = View.GONE
                                binding.cardAllPassed.visibility = View.VISIBLE
                            }
                            UpcomingHolidayFinder.UpcomingResult.None -> {
                                binding.cardUpcomingHoliday.visibility = View.GONE
                                binding.cardAllPassed.visibility = View.GONE
                            }
                        }
                    }
                }

                // Observe UI state (Loading / Success / Empty / Error)
                launch {
                    viewModel.holidaysUiState.collect { state ->
                        binding.swipeRefreshHolidays.isRefreshing = false
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressHolidays.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressHolidays.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.GONE
                            }
                            is UiState.Empty -> {
                                binding.progressHolidays.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                                binding.layoutEmpty.visibility = View.VISIBLE
                            }
                            is UiState.Error -> {
                                binding.progressHolidays.visibility = View.GONE
                                binding.layoutError.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = state.message
                                binding.layoutEmpty.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
