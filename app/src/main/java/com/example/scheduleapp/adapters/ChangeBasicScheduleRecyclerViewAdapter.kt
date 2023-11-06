package com.example.scheduleapp.adapters

/*
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduleapp.R
import com.example.scheduleapp.data.Constants
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.BasicTextItemBinding

class ChangeBasicScheduleRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: BasicTextItemBinding

    class ItemViewHolder(private val recycler: ChangeBasicScheduleRecyclerViewAdapter, private val binding: BasicTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setBasicSchedule(name: Data_IntString) {
            binding.apply {
                title.text = SpannableStringBuilder(name.title)
                title.inputType = 0

                moreVertexSpinner.adapter = ArrayAdapter(moreVertexSpinner.context, R.layout.spinner_item, arrayListOf(
                    Constants.APP_ADMIN_EDIT_OPTIONS_OFF, Constants.APP_ADMIN_EDIT_OPTIONS_ON, Constants.APP_ADMIN_EDIT_OPTIONS_DELETE
                )).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                moreVertexSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position == 0) {
                            if (title.inputType == 0) {
                                if (title.text.isEmpty()) {
                                    moreVertexSpinner.setSelection(1)
                                }
                            } else if ( title.text.toString().isEmpty() || recycler.checkForSimilarTitle(title.text.toString(), name.id!!) ) {
                                moreVertexSpinner.setSelection(1)
                                Toast.makeText(moreVertexSpinner.context, "Can not save such a name.", Toast.LENGTH_SHORT).show()
                            } else {
                                recycler.editFunction(adapterPosition, title.text.toString())
                                title.inputType = 0
                                recycler.removeValueFromViewStates(item.id!!)
                            }
                        }
                        if (position == 1) {
                            title.inputType = 1
                            recycler.addValueToViewStates(item.id!!)
                        }
                        if (position == 2) {
                            recycler.deleteFunction(adapterPosition, binding)
                            recycler.removeValueFromViewStates(item.id!!)
                        }
                        recycler.updateAddButton(recycler.viewStates)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) { }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = BasicTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(this, binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).setBasicSchedule(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun checkForSimilarTitle(title: String, id: Int): Boolean {
        differ.currentList.forEach {
            if (it.title == title && it.id != id) {
                return true
            }
        }
        return false
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
*/