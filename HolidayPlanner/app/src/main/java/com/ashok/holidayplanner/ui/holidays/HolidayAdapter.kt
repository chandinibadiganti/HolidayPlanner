package com.ashok.holidayplanner.ui.holidays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashok.holidayplanner.databinding.ItemHolidayBinding
import com.ashok.holidayplanner.domain.model.Holiday
import com.ashok.holidayplanner.util.DateUtils
import java.time.LocalDate

class HolidayAdapter(
    private val onBridgeClickListener: ((Holiday, Pair<LocalDate, LocalDate>) -> Unit)? = null
) : ListAdapter<Holiday, HolidayAdapter.HolidayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val binding = ItemHolidayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HolidayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HolidayViewHolder(
        private val binding: ItemHolidayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(holiday: Holiday) {
            // Formatted Date
            binding.tvHolidayDate.text = DateUtils.formatToDisplay(holiday.date)

            // Holiday Name
            val primaryName = if (holiday.name.isNotBlank()) holiday.name else holiday.localName
            binding.tvHolidayName.text = primaryName

            // Local name (hide if identical or blank)
            if (holiday.localName.isNotBlank() && !holiday.localName.equals(holiday.name, ignoreCase = true)) {
                binding.tvHolidayLocalName.text = holiday.localName
                binding.tvHolidayLocalName.visibility = View.VISIBLE
            } else {
                binding.tvHolidayLocalName.visibility = View.GONE
            }

            // Long Weekend Badge
            if (holiday.isLongWeekend && !holiday.longWeekendSpan.isNullOrBlank()) {
                binding.tvLongWeekendBadge.text = holiday.longWeekendSpan
                binding.tvLongWeekendBadge.visibility = View.VISIBLE
            } else {
                binding.tvLongWeekendBadge.visibility = View.GONE
            }

            // Leave Overlap Indicator
            if (holiday.isOverlappingLeave) {
                val overlapMsg = if (holiday.overlappingLeaveTitles.isNotEmpty()) {
                    "Overlaps with: ${holiday.overlappingLeaveTitles.joinToString(", ")}"
                } else {
                    "Overlaps your planned leave"
                }
                binding.tvOverlapText.text = overlapMsg
                binding.layoutOverlapIndicator.visibility = View.VISIBLE
            } else {
                binding.layoutOverlapIndicator.visibility = View.GONE
            }

            // Bridge This Action
            val bridge = holiday.bridgeSuggestion
            if (bridge != null) {
                binding.btnBridgeThis.visibility = View.VISIBLE
                binding.btnBridgeThis.setOnClickListener {
                    onBridgeClickListener?.invoke(holiday, bridge)
                }
            } else {
                binding.btnBridgeThis.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Holiday>() {
        override fun areItemsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
            return oldItem.date == newItem.date && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
            return oldItem == newItem
        }
    }
}
