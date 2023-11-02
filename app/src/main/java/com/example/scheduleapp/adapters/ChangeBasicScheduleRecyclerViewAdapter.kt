package com.example.scheduleapp.adapters

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.BasicTextItemBinding

class ChangeBasicScheduleRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: BasicTextItemBinding

    class ItemViewHolder(private val binding: BasicTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setBasicSchedule(name: Data_IntString) {
            binding.apply {
                title.text = SpannableStringBuilder(name.title)
                title.inputType = 0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = BasicTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setBasicSchedule(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    val differCallback = object : DiffUtil.ItemCallback<Data_IntString>() {
        override fun areContentsTheSame(oldItem: Data_IntString, newItem: Data_IntString): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(oldItem: Data_IntString, newItem: Data_IntString): Boolean {
            return oldItem.title == newItem.title
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}