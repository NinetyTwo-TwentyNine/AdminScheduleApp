package com.example.scheduleapp.adapters

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.databinding.ScheduleAddItemBinding
import com.example.scheduleapp.databinding.ScheduleAddSpecialItemBinding

class AddPairRecyclerViewAdapter(
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ScheduleAddItemBinding
    private lateinit var bindingSpecial: ScheduleAddSpecialItemBinding

    val common = 0
    val special = 1

    class ItemViewHolder(private val binding: ScheduleAddItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: AddPairRecyclerViewItem) {
            binding.apply {
                if (data.visibility) {
                    cardView.visibility = View.VISIBLE
                } else {
                    cardView.visibility = View.GONE
                }
                editPair.text = SpannableStringBuilder(data.namePair)
                editCabinet.text = SpannableStringBuilder(data.cabinet)
                editTeacher.text = SpannableStringBuilder(data.teacher)
            }
        }
    }

    class SpecialItemViewHolder(private val binding: ScheduleAddSpecialItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSpecialData(data: AddPairRecyclerViewItem) {
            binding.apply {
                if (data.visibility) {
                    cardView.visibility = View.VISIBLE
                } else {
                    cardView.visibility = View.GONE
                }
                teacherSecond.text = SpannableStringBuilder(data.teacherSecond)
                teacherThird.text = SpannableStringBuilder(data.teacherThird)
                cabinetSecond.text = SpannableStringBuilder(data.cabinetSecond)
                cabinetThird.text = SpannableStringBuilder(data.cabinetThird)
                subgroup.text = SpannableStringBuilder(data.subGroup.toString())
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (differ.currentList[position].type) special
        else common
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == common) {
            binding =
                ScheduleAddItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        } else {
            bindingSpecial = ScheduleAddSpecialItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SpecialItemViewHolder(bindingSpecial)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == common) {
            (holder as ItemViewHolder).setData(differ.currentList[position])
            holder.setIsRecyclable(false)
        } else {
            (holder as SpecialItemViewHolder).setSpecialData(differ.currentList[position])
            holder.setIsRecyclable(false)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    val differCallback = object : DiffUtil.ItemCallback<AddPairRecyclerViewItem>() {
        override fun areContentsTheSame(
            oldItem: AddPairRecyclerViewItem, newItem: AddPairRecyclerViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(
            oldItem: AddPairRecyclerViewItem, newItem: AddPairRecyclerViewItem
        ): Boolean {
            return oldItem.teacher == newItem.teacherSecond
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

}