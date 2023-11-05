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
        AddPairRecyclerViewItem(type = true, id = 1),
        AddPairRecyclerViewItem(id = 2),
        AddPairRecyclerViewItem(type = true, id = 3)
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
        binding.apply {
            if (subPairEnable.isChecked && !optionalEnable.isChecked) {
                arrayOfPairs[1].visibility = true
                arrayOfPairs[2].visibility = false
                arrayOfPairs[3].visibility = false
                updateRecyclerView()
            } else if (!subPairEnable.isChecked && optionalEnable.isChecked) {
                arrayOfPairs[1].visibility = false
                arrayOfPairs[2].visibility = true
                arrayOfPairs[3].visibility = false
                updateRecyclerView()
            } else if (!subPairEnable.isChecked && optionalEnable.isChecked) {
                arrayOfPairs[1].visibility = true
                arrayOfPairs[2].visibility = true
                arrayOfPairs[3].visibility = true
                updateRecyclerView()
            }
        }
    }

    fun updateRecyclerView() {
        addPairRecyclerViewAdapter.differ.submitList(arrayOfPairs)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }
    }

}