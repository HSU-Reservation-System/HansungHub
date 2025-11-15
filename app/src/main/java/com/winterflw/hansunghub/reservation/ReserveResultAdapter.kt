package com.winterflw.hansunghub.reservation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.winterflw.hansunghub.databinding.ItemReserveResultBinding
import com.winterflw.hansunghub.network.model.ReserveResultItem

class ReserveResultAdapter(
    private val items: List<ReserveResultItem>
) : RecyclerView.Adapter<ReserveResultAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemReserveResultBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReserveResultItem) {
            binding.tvTime.text = item.time

            if (item.success) {
                binding.tvResult.text = "성공"
                binding.tvResult.setTextColor(Color.parseColor("#2E7D32")) // 초록
            } else {
                binding.tvResult.text = "실패"
                binding.tvResult.setTextColor(Color.parseColor("#D32F2F")) // 빨강
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReserveResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])
}
