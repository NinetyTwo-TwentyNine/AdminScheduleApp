package com.example.scheduleapp.adapters

import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
                    editPair.adapter = ArrayAdapter((editPair.context), R.layout.spinner_item, recycler.disciplineList).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until editPair.adapter.count) {
                        if (editPair.getItemAtPosition(i).toString() == data.namePair) {
                            editPair.setSelection(i)
                            break
                        }
                    }
                    editPair.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (editPair.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].namePair) {
                                data.namePair = editPair.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    editCabinet.adapter = ArrayAdapter((editCabinet.context), R.layout.spinner_item, recycler.cabinetList).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until editCabinet.adapter.count) {
                        if (editCabinet.getItemAtPosition(i).toString() == data.cabinet) {
                            editCabinet.setSelection(i)
                            break
                        }
                    }
                    editCabinet.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (editCabinet.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].cabinet) {
                                data.cabinet = editCabinet.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    editTeacher.adapter = ArrayAdapter((editTeacher.context), R.layout.spinner_item, recycler.teacherList).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until editTeacher.adapter.count) {
                        if (editTeacher.getItemAtPosition(i).toString() == data.namePair) {
                            editTeacher.setSelection(i)
                            break
                        }
                    }
                    editTeacher.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (editTeacher.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].teacher) {
                                data.teacher = editTeacher.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                    cabinetSecond.adapter = ArrayAdapter((cabinetSecond.context), R.layout.spinner_item, ArrayList(recycler.cabinetList)).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    cabinetThird.adapter = ArrayAdapter((cabinetThird.context), R.layout.spinner_item, ArrayList(recycler.cabinetList)).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until recycler.cabinetList.count()) {
                        if (cabinetSecond.getItemAtPosition(i).toString() == data.cabinetSecond) {
                            cabinetSecond.setSelection(i)
                        }
                        if (cabinetThird.getItemAtPosition(i).toString() == data.cabinetThird) {
                            cabinetThird.setSelection(i)
                        }
                    }
                    cabinetSecond.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (cabinetSecond.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].cabinetSecond) {
                                data.cabinetSecond = cabinetSecond.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                    cabinetThird.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (cabinetThird.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].cabinetThird) {
                                data.cabinetThird = cabinetThird.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    teacherSecond.adapter = ArrayAdapter((teacherSecond.context), R.layout.spinner_item, recycler.teacherList).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    teacherThird.adapter = ArrayAdapter((teacherThird.context), R.layout.spinner_item, recycler.teacherList).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until recycler.teacherList.count()) {
                        if (teacherSecond.getItemAtPosition(i).toString() == data.teacherSecond) {
                            teacherSecond.setSelection(i)
                        }
                        if (teacherThird.getItemAtPosition(i).toString() == data.teacherThird) {
                            teacherThird.setSelection(i)
                        }
                    }
                    teacherSecond.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (teacherSecond.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].teacherSecond) {
                                data.teacherSecond = teacherSecond.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                    teacherThird.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (teacherThird.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].teacherThird) {
                                data.teacherThird = teacherThird.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    subgroup.adapter = ArrayAdapter((subgroup.context), R.layout.spinner_item, arrayListOf("1", "2")).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    for (i in 0 until subgroup.adapter.count) {
                        if (subgroup.getItemAtPosition(i).toString() == data.subGroup) {
                            subgroup.setSelection(i)
                            break
                        }
                    }
                    subgroup.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (subgroup.getItemAtPosition(position).toString() != recycler.differ.currentList[adapterPosition].subGroup) {
                                data.subGroup = subgroup.getItemAtPosition(position).toString()
                                recycler.editFunction(adapterPosition, data)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
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