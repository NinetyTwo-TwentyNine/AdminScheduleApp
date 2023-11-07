package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AddPairRecyclerViewAdapter
import com.example.scheduleapp.adapters.AddPairRecyclerViewItem
import com.example.scheduleapp.data.Constants.APP_ADMIN_EDIT_PAIR_ARRAY
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_CABINET_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_DISCIPLINE_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_TEACHER_LIST
import com.example.scheduleapp.databinding.FragmentAddPairBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel

class AddPairFragment() : Fragment() {
    private lateinit var binding: FragmentAddPairBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var addPairRecyclerViewAdapter: AddPairRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPairBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        binding.subPairEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }
        binding.optionalEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }
    }

    private fun setupRecyclerView() {
        addPairRecyclerViewAdapter = AddPairRecyclerViewAdapter(
            viewModel.getParametersList(APP_BD_PATHS_DISCIPLINE_LIST),
            viewModel.getParametersList(APP_BD_PATHS_TEACHER_LIST),
            viewModel.getParametersList(APP_BD_PATHS_CABINET_LIST))

        addPairRecyclerViewAdapter.differ.submitList(APP_ADMIN_EDIT_PAIR_ARRAY)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }
    }

    private fun updateRecyclerView() {
        val arrayOfPairs = ArrayList(addPairRecyclerViewAdapter.differ.currentList)

        arrayOfPairs[1].visibility = binding.optionalEnable.isChecked
        arrayOfPairs[2].visibility = binding.subPairEnable.isChecked
        arrayOfPairs[3].visibility = binding.optionalEnable.isChecked && binding.subPairEnable.isChecked

        for (i in 0 until arrayOfPairs.size) {
            if (!arrayOfPairs[i].visibility) {
                arrayOfPairs[i] = APP_ADMIN_EDIT_PAIR_ARRAY[i]
            }
        }

        addPairRecyclerViewAdapter.differ.submitList(arrayOfPairs)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }
    }

}