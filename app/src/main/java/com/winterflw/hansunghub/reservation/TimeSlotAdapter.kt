package com.winterflw.hansunghub.reservation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.winterflw.hansunghub.databinding.ItemTimeSlotBinding
import com.winterflw.hansunghub.network.model.TimeSlot
import com.winterflw.hansunghub.R

class TimeSlotAdapter(
    private val items: List<TimeSlot>,
    private val onClick: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(val binding: ItemTimeSlotBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val item = items[position]

        // 반드시 먼저 초기화해야 함 (재활용 문제 해결)
        holder.binding.tvTime.isEnabled = true

        holder.binding.tvTime.text = item.time

        when {
            !item.isAvailable -> {
                holder.binding.tvTime.isEnabled = false
                holder.binding.tvTime.setBackgroundResource(R.drawable.bg_time_disabled)
            }
            item.isSelected -> {
                holder.binding.tvTime.setBackgroundResource(R.drawable.bg_time_selected)
            }
            else -> {
                holder.binding.tvTime.setBackgroundResource(R.drawable.bg_time_default)
            }
        }

        holder.binding.tvTime.setOnClickListener {
            if (holder.binding.tvTime.isEnabled) {
                onClick(item)
            }
        }
    }


    override fun getItemCount(): Int = items.size
}

