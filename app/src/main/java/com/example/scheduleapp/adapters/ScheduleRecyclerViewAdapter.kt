package com.example.scheduleapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.R
import com.example.scheduleapp.data.Constants.APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_CLEAR
import com.example.scheduleapp.data.Constants.APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_TWEAK
import com.example.scheduleapp.data.Schedule
import com.example.scheduleapp.databinding.ScheduleItemBinding


class ScheduleRecyclerViewAdapter(private val editFunction: (Int) -> Unit,
                                  private val clearFunction: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: ScheduleItemBinding


    class ItemViewHolder(private val binding: ScheduleItemBinding, private val recycler: ScheduleRecyclerViewAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSchedule(item: Schedule) {
            binding.apply {
                lesson.text = "${(item.lessonNum!!-1)/2+1}/${(item.lessonNum!!-1)%2+1}"
                discipline.text = item.discipline
                cabinet.text = item.cabinet
                teacher.text = item.teacher

                optionsSpinner.adapter = ArrayAdapter((optionsSpinner.context), R.layout.invisible_spinner_item, arrayListOf(
                    "", APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_TWEAK, APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_CLEAR)).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                optionsSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (optionsSpinner.getItemAtPosition(position) == APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_TWEAK) {
                            recycler.editFunction((item.lessonNum!!-1)/2)
                        }
                        if (optionsSpinner.getItemAtPosition(position) == APP_ADMIN_SCHEDULE_ITEM_EDIT_OPTIONS_CLEAR) {
                            recycler.clearFunction((item.lessonNum!!-1)/2)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, this)
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