package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AddPairRecyclerViewAdapter
import com.example.scheduleapp.adapters.AddPairRecyclerViewItem
import com.example.scheduleapp.databinding.FragmentAddPairBinding

class AddPairFragment() : Fragment() {
    private lateinit var binding: FragmentAddPairBinding
    private val addPairRecyclerViewAdapter by lazy { AddPairRecyclerViewAdapter() }
    private val arrayOfPairs: ArrayList<AddPairRecyclerViewItem> = arrayListOf(
        AddPairRecyclerViewItem(id = 0, visibility = true),
        AddPairRecyclerViewItem(type = 1, id = 1),
        AddPairRecyclerViewItem(id = 2),
        AddPairRecyclerViewItem(type = 1, id = 3)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPairBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addPairRecyclerViewAdapter.differ.submitList(arrayOfPairs)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }


        updateRecyclerView()

        binding.subPairEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }
        binding.optionalEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }
    }

    fun updateRecyclerView() {
        arrayOfPairs[1].visibility = binding.optionalEnable.isChecked
        arrayOfPairs[2].visibility = binding.subPairEnable.isChecked
        arrayOfPairs[3].visibility = binding.optionalEnable.isChecked && binding.subPairEnable.isChecked

        addPairRecyclerViewAdapter.differ.submitList(arrayOfPairs)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }
    }

}