package com.ashok.holidayplanner.ui.leaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ashok.holidayplanner.HolidayApplication
import com.ashok.holidayplanner.R
import com.ashok.holidayplanner.databinding.DialogLeaveEditorBinding
import com.ashok.holidayplanner.util.DateUtils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.LocalDate

class LeaveEditorDialogFragment : DialogFragment() {

    private var _binding: DialogLeaveEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeavesViewModel by activityViewModels {
        (requireActivity().application as HolidayApplication).container.viewModelFactory
    }

    private var leaveId: Long = 0
    private var selectedStartDate: LocalDate = LocalDate.now()
    private var selectedEndDate: LocalDate = LocalDate.now()

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_START_DATE = "arg_start_date"
        private const val ARG_END_DATE = "arg_end_date"
        private const val ARG_NOTE = "arg_note"

        fun newInstance(
            id: Long = 0,
            initialTitle: String = "",
            initialStartDate: LocalDate = LocalDate.now(),
            initialEndDate: LocalDate = LocalDate.now(),
            initialNote: String = ""
        ): LeaveEditorDialogFragment {
            return LeaveEditorDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                    putString(ARG_TITLE, initialTitle)
                    putLong(ARG_START_DATE, initialStartDate.toEpochDay())
                    putLong(ARG_END_DATE, initialEndDate.toEpochDay())
                    putString(ARG_NOTE, initialNote)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLeaveEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            leaveId = args.getLong(ARG_ID, 0)
            binding.etLeaveTitle.setText(args.getString(ARG_TITLE, ""))
            selectedStartDate = LocalDate.ofEpochDay(args.getLong(ARG_START_DATE, LocalDate.now().toEpochDay()))
            selectedEndDate = LocalDate.ofEpochDay(args.getLong(ARG_END_DATE, LocalDate.now().toEpochDay()))
            binding.etLeaveNote.setText(args.getString(ARG_NOTE, ""))
        }

        binding.tvDialogTitle.text = if (leaveId == 0L) getString(R.string.add_leave) else getString(R.string.edit_leave)

        updateDateRangeDisplay()
        checkOverlapWarning()

        binding.etLeaveDates.setOnClickListener {
            openDateRangePicker()
        }
        binding.tilLeaveDates.setEndIconOnClickListener {
            openDateRangePicker()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveLeave()
        }
    }

    private fun openDateRangePicker() {
        val startUtc = DateUtils.localDateToUtcMillis(selectedStartDate)
        val endUtc = DateUtils.localDateToUtcMillis(selectedEndDate)

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.leave_dates_hint)
            .setSelection(Pair(startUtc, endUtc))
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            if (selection.first != null && selection.second != null) {
                selectedStartDate = DateUtils.utcMillisToLocalDate(selection.first)
                selectedEndDate = DateUtils.utcMillisToLocalDate(selection.second)
                updateDateRangeDisplay()
                checkOverlapWarning()
            }
        }

        picker.show(parentFragmentManager, "MaterialDatePicker")
    }

    private fun updateDateRangeDisplay() {
        val rangeStr = DateUtils.formatRange(selectedStartDate, selectedEndDate)
        val days = DateUtils.calculateInclusiveDays(selectedStartDate, selectedEndDate)
        val daysText = if (days == 1L) "1 day" else "$days days"
        binding.etLeaveDates.setText("$rangeStr ($daysText)")
    }

    private fun checkOverlapWarning() {
        viewLifecycleOwner.lifecycleScope.launch {
            val overlaps = viewModel.checkOverlaps(
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                excludeId = if (leaveId != 0L) leaveId else null
            )
            if (overlaps.isNotEmpty()) {
                val titles = overlaps.joinToString(", ") { it.title }
                binding.tvOverlapWarningText.text = "Warning: Overlaps with existing leave '$titles'."
                binding.layoutOverlapWarning.visibility = View.VISIBLE
            } else {
                binding.layoutOverlapWarning.visibility = View.GONE
            }
        }
    }

    private fun saveLeave() {
        val title = binding.etLeaveTitle.text?.toString().orEmpty()
        val note = binding.etLeaveNote.text?.toString()

        binding.tilLeaveTitle.error = null

        if (title.isBlank()) {
            binding.tilLeaveTitle.error = getString(R.string.title_required)
            return
        }

        if (selectedEndDate.isBefore(selectedStartDate)) {
            Snackbar.make(binding.root, R.string.invalid_dates, Snackbar.LENGTH_LONG).show()
            return
        }

        viewModel.saveLeave(
            id = leaveId,
            title = title,
            startDate = selectedStartDate,
            endDate = selectedEndDate,
            note = note
        ) { success, errorMsg ->
            if (success) {
                dismiss()
            } else {
                Snackbar.make(
                    binding.root,
                    errorMsg ?: "Failed to save leave plan",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
