package com.example.scheduleapp.adapters

import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.R
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.ScheduleAddItemBinding
import com.example.scheduleapp.databinding.ScheduleAddSpecialItemBinding

class AddPairRecyclerViewAdapter(private val disciplineList: ArrayList<String>,
                                 private val teacherList: ArrayList<String>,
                                 private val cabinetList: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ScheduleAddItemBinding
    private lateinit var bindingSpecial: ScheduleAddSpecialItemBinding

    val common = 0
    val special = 1

    class ItemViewHolder(private val binding: ScheduleAddItemBinding, private val recycler: AddPairRecyclerViewAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: AddPairRecyclerViewItem) {
            binding.apply {
                if (data.visibility) {
                    recycler.setupSpinner(editPair, editPairDefaultText, recycler.disciplineList, data.namePair) { pos: Int ->
                        data.namePair = editPair.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }
                    recycler.setupSpinner(editCabinet, editCabinetDefaultText, recycler.cabinetList, data.cabinet) { pos: Int ->
                        data.cabinet = editCabinet.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }
                    recycler.setupSpinner(editTeacher, editTeacherDefaultText, recycler.teacherList, data.teacher) { pos: Int ->
                        data.teacher = editTeacher.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }

                    cardView.visibility = View.VISIBLE
                } else {
                    cardView.visibility = View.GONE
                }
            }
        }
    }

    class SpecialItemViewHolder(private val binding: ScheduleAddSpecialItemBinding, private val recycler: AddPairRecyclerViewAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        fun setSpecialData(data: AddPairRecyclerViewItem) {
            binding.apply {
                if (data.visibility) {
                    recycler.setupSpinner(cabinetSecond, cabinetSecondDefaultText, recycler.cabinetList, data.cabinetSecond) { pos: Int ->
                        data.cabinetSecond = cabinetSecond.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }
                    recycler.setupSpinner(cabinetThird, cabinetThirdDefaultText, recycler.cabinetList, data.cabinetThird) { pos: Int ->
                        data.cabinetThird = cabinetThird.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }

                    recycler.setupSpinner(teacherSecond, teacherSecondDefaultText, recycler.teacherList, data.teacherSecond) { pos: Int ->
                        data.teacherSecond = teacherSecond.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }
                    recycler.setupSpinner(teacherThird, teacherThirdDefaultText, recycler.teacherList, data.teacherThird) { pos: Int ->
                        data.teacherThird = teacherThird.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }

                    recycler.setupSpinner(subgroup, subgroupDefaultText, arrayListOf("1", "2"), data.subGroup) { pos: Int ->
                        data.subGroup = subgroup.getItemAtPosition(pos).toString()
                        recycler.editFunction(adapterPosition, data)
                    }

                    cardView.visibility = View.VISIBLE
                } else {
                    cardView.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == common) {
            binding = ScheduleAddItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding, this)
        } else {
            bindingSpecial = ScheduleAddSpecialItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return SpecialItemViewHolder(bindingSpecial, this)
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

    fun setupSpinner(spinner: Spinner, defaultText: TextView, choices: ArrayList<String>, stringToChange: String, editItem: (Int)->Unit) {
        if (!choices.contains("")) {
            choices.add(0, "")
        }
        spinner.adapter = ArrayAdapter((spinner.context), R.layout.spinner_item, choices).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                defaultText.visibility = when(spinner.getItemAtPosition(position).toString().isEmpty()) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
                if (spinner.getItemAtPosition(position).toString() != stringToChange) {
                    editItem(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        for (i in 0 until choices.count()) {
            if (spinner.getItemAtPosition(i).toString() == stringToChange) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun editFunction(pos: Int, data: AddPairRecyclerViewItem) {
        val currentRecyclerList = ArrayList(differ.currentList)
        currentRecyclerList[pos] = data

        differ.submitList(currentRecyclerList)
    }

    val differCallback = object : DiffUtil.ItemCallback<AddPairRecyclerViewItem>() {
        override fun areContentsTheSame(
            oldItem: AddPairRecyclerViewItem, newItem: AddPairRecyclerViewItem
        ): Boolean {
            return oldItem.equals(newItem)
        }

        override fun areItemsTheSame(
            oldItem: AddPairRecyclerViewItem, newItem: AddPairRecyclerViewItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}