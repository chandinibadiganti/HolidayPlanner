package com.ashok.holidayplanner.ui.leaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ashok.holidayplanner.HolidayApplication
import com.ashok.holidayplanner.R
import com.ashok.holidayplanner.databinding.FragmentLeavesBinding
import com.ashok.holidayplanner.domain.model.LeaveWithHolidays
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LeavesFragment : Fragment() {

    private var _binding: FragmentLeavesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeavesViewModel by activityViewModels {
        (requireActivity().application as HolidayApplication).container.viewModelFactory
    }

    private lateinit var leaveAdapter: LeaveAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeavesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeLeaves()
    }

    private fun setupRecyclerView() {
        leaveAdapter = LeaveAdapter(
            onEditClick = { item ->
                openEditLeave(item)
            },
            onDeleteClick = { item ->
                confirmDeleteLeave(item)
            }
        )

        binding.rvLeaves.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaveAdapter
        }
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = leaveAdapter.currentList[position]
                    viewModel.deleteLeave(item.leavePlan)
                    showUndoSnackbar()
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvLeaves)
    }

    private fun setupFab() {
        binding.fabAddLeave.setOnClickListener {
            val dialog = LeaveEditorDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "LeaveEditorDialogFragment")
        }
    }

    private fun openEditLeave(item: LeaveWithHolidays) {
        val plan = item.leavePlan
        val dialog = LeaveEditorDialogFragment.newInstance(
            id = plan.id,
            initialTitle = plan.title,
            initialStartDate = plan.startDate,
            initialEndDate = plan.endDate,
            initialNote = plan.note.orEmpty()
        )
        dialog.show(parentFragmentManager, "LeaveEditorDialogFragment")
    }

    private fun confirmDeleteLeave(item: LeaveWithHolidays) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_leave)
            .setMessage(R.string.confirm_delete_leave)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteLeave(item.leavePlan)
                showUndoSnackbar()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showUndoSnackbar() {
        Snackbar.make(binding.root, R.string.leave_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                viewModel.undoDelete()
            }
            .show()
    }

    private fun observeLeaves() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.leavesWithHolidays.collect { leaves ->
                    leaveAdapter.submitList(leaves)
                    if (leaves.isEmpty()) {
                        binding.layoutEmptyLeaves.visibility = View.VISIBLE
                        binding.rvLeaves.visibility = View.GONE
                    } else {
                        binding.layoutEmptyLeaves.visibility = View.GONE
                        binding.rvLeaves.visibility = View.VISIBLE
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
