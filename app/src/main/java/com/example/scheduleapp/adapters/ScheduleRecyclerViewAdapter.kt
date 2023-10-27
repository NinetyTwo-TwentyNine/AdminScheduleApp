package com.example.scheduleapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.data.Schedule
import com.example.scheduleapp.databinding.ScheduleItemBinding


class ScheduleRecyclerViewAdapter(
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ScheduleItemBinding

    class ItemViewHolder(private val binding: ScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSchedule(item: Schedule) {
            binding.apply {
                lesson.text = "${(item.lessonNum!!-1)/2+1}/${(item.lessonNum!!-1)%2+1}"
                discipline.text = item.discipline
                cabinet.text = item.cabinet
                teacher.text = item.teacher
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setSchedule(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.lessonNum == newItem.lessonNum
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}