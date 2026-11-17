package com.ashok.holidayplanner.ui.leaves

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashok.holidayplanner.databinding.ItemLeaveBinding
import com.ashok.holidayplanner.domain.model.LeaveWithHolidays
import com.ashok.holidayplanner.util.DateUtils

class LeaveAdapter(
    private val onEditClick: (LeaveWithHolidays) -> Unit,
    private val onDeleteClick: (LeaveWithHolidays) -> Unit
) : ListAdapter<LeaveWithHolidays, LeaveAdapter.LeaveViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val binding = ItemLeaveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LeaveViewHolder(
        private val binding: ItemLeaveBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveWithHolidays) {
            val plan = item.leavePlan

            binding.tvLeaveTitle.text = plan.title

            val days = item.durationDays
            binding.tvDurationBadge.text = if (days == 1L) "1 day" else "$days days"

            binding.tvLeaveDateRange.text = DateUtils.formatRange(plan.startDate, plan.endDate)

            // Included Public Holidays Summary
            binding.tvIncludedHolidays.text = item.holidaysSummaryText

            // Note
            if (!plan.note.isNullOrBlank()) {
                binding.tvLeaveNote.text = plan.note
                binding.tvLeaveNote.visibility = View.VISIBLE
            } else {
                binding.tvLeaveNote.visibility = View.GONE
            }

            // Click on whole card or Edit button triggers edit
            binding.root.setOnClickListener {
                onEditClick(item)
            }
            binding.btnEditLeave.setOnClickListener {
                onEditClick(item)
            }

            binding.btnDeleteLeave.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LeaveWithHolidays>() {
        override fun areItemsTheSame(oldItem: LeaveWithHolidays, newItem: LeaveWithHolidays): Boolean {
            return oldItem.leavePlan.id == newItem.leavePlan.id
        }

        override fun areContentsTheSame(oldItem: LeaveWithHolidays, newItem: LeaveWithHolidays): Boolean {
            return oldItem == newItem
        }
    }
}
