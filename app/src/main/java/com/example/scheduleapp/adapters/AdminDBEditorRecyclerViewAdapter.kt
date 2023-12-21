package com.example.scheduleapp.adapters

import android.annotation.SuppressLint
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
import com.example.scheduleapp.data.Constants.APP_ADMIN_TABLE_EDIT_OPTIONS_DELETE
import com.example.scheduleapp.data.Constants.APP_ADMIN_TABLE_EDIT_OPTIONS_OFF
import com.example.scheduleapp.data.Constants.APP_ADMIN_TABLE_EDIT_OPTIONS_ON
import com.example.scheduleapp.data.Constants.APP_CALENDER_DAY_OF_WEEK
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.BasicTextItemBinding

class AdminDBEditorRecyclerViewAdapter(private val updateAddButton: (ArrayList<Int>) -> Unit,
                                       private val updateSaveButton: () -> Unit,
                                       private val minTextLength: Int,
                                       private val deleteFunc: ((Int)->Unit)? = null,
                                       private val cardSpinnerFunc: ((Int, Int)->Unit)? = null,
                                       private val checkIfDeletionIsPossible: ((Int)->Boolean)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val viewStates: ArrayList<Int> = ArrayList()
    private lateinit var binding: BasicTextItemBinding

    class ItemViewHolder(private val binding: BasicTextItemBinding,
                         private val recycler: AdminDBEditorRecyclerViewAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setupView(item: Data_IntString) {
            binding.apply {
                title.text = SpannableStringBuilder(item.title)
                title.inputType = 0

                if (recycler.cardSpinnerFunc != null) {
                    val adapterArray = ArrayList(APP_CALENDER_DAY_OF_WEEK)
                    adapterArray[0] = ""
                    cardSpinner.adapter = ArrayAdapter(cardSpinner.context, R.layout.invisible_spinner_item, adapterArray).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    cardSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (position != 0 && title.inputType != 1) {
                                recycler.cardSpinnerFunc!!(position, item.id!!)
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) { }
                    }
                } else {
                    cardSpinner.isEnabled = false
                }


                moreVertexSpinner.adapter = ArrayAdapter(moreVertexSpinner.context, R.layout.spinner_item, arrayListOf(APP_ADMIN_TABLE_EDIT_OPTIONS_OFF, APP_ADMIN_TABLE_EDIT_OPTIONS_ON, APP_ADMIN_TABLE_EDIT_OPTIONS_DELETE)).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                moreVertexSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position == 0) {
                            title.text = SpannableStringBuilder(title.text.toString().trim())
                            if (title.inputType == 0) {
                                if (title.text.isEmpty()) {
                                    moreVertexSpinner.setSelection(1)
                                }
                            } else if ( !recycler.checkTitleValidity(title.text.toString(), item.id!!) ) {
                                moreVertexSpinner.setSelection(1)
                                Toast.makeText(moreVertexSpinner.context, "Can not save such a title.", Toast.LENGTH_SHORT).show()
                            } else {
                                title.inputType = 0
                                cardSpinner.isEnabled = (recycler.cardSpinnerFunc != null)
                                recycler.editFunction(adapterPosition, title.text.toString())
                                recycler.removeValueFromViewStates(item.id!!)
                            }
                        }
                        if (position == 1) {
                            title.inputType = 1
                            cardSpinner.isEnabled = false
                            recycler.addValueToViewStates(item.id!!)
                        }
                        if (position == 2) {
                            var shouldDelete = true
                            if (recycler.checkIfDeletionIsPossible != null) {
                                shouldDelete = recycler.checkIfDeletionIsPossible!!(item.id!!)
                            }

                            if (shouldDelete) {
                                recycler.deleteCardView(adapterPosition, binding)
                                recycler.removeValueFromViewStates(item.id!!)
                            } else {
                                moreVertexSpinner.setSelection(title.inputType)
                                Toast.makeText(moreVertexSpinner.context, "Can not delete this item.", Toast.LENGTH_SHORT).show()
                            }
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
        return AdminDBEditorRecyclerViewAdapter.ItemViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AdminDBEditorRecyclerViewAdapter.ItemViewHolder).setupView(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun deleteCardView(pos: Int, binding: BasicTextItemBinding) {
        val currentRecyclerList = ArrayList(differ.currentList)
        deleteFunc?.let { it(currentRecyclerList[pos].id!!) }
        currentRecyclerList.removeAt(pos)

        completelyRemoveTheView(binding)
        //notifyItemRemoved(pos)
        differ.submitList(currentRecyclerList)
        updateSaveButton()
    }

    private fun editFunction(pos: Int, title: String) {
        val currentRecyclerList = ArrayList(differ.currentList)
        currentRecyclerList[pos].title = title

        differ.submitList(currentRecyclerList)
        updateSaveButton()
    }

    fun addValueToViewStates(id: Int) {
        if (!viewStates.contains(id)) {
            viewStates.add(id)
        }
    }

    fun removeValueFromViewStates(id: Int) {
        if (viewStates.contains(id)) {
            viewStates.remove(id)
        }
    }

    private fun completelyRemoveTheView(binding: BasicTextItemBinding) {
        binding.cardSpinner.isEnabled = false
        binding.moreVertexSpinner.isClickable = false
        binding.title.isClickable = false
        binding.root.isClickable = false
        binding.root.visibility = View.GONE
    }

    fun checkTitleValidity(title: String, id: Int): Boolean {
        if (title.length < minTextLength) {
            return false
        }
        differ.currentList.forEach {
            if (it.title == title && it.id != id) {
                return false
            }
        }
        return true
    }


    private val differCallback = object : DiffUtil.ItemCallback<Data_IntString>() {
        override fun areItemsTheSame(oldItem: Data_IntString, newItem: Data_IntString): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Data_IntString, newItem: Data_IntString): Boolean {
            return oldItem.title == newItem.title
        }
    }
    val differ = AsyncListDiffer(this, differCallback)
}