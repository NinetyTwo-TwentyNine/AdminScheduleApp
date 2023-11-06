package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.FragmentChangeBasicScheduleBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel

class ChangeBasicScheduleFragment() : Fragment() {
    private lateinit var binding: FragmentChangeBasicScheduleBinding
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()

    private lateinit var addButtonCheck: (ArrayList<Int>)->Unit
    private lateinit var saveButtonCheck: ()->Unit


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeBasicScheduleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addButton.setOnClickListener {
            val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
            val new_id = scheduleViewModel.getPossibleId(currentRecyclerList)
            currentRecyclerList.add(0, Data_IntString(new_id, "Базовое расписание $new_id"))

            //dbEditorRecyclerViewAdapter.notifyItemChanged(0)
            dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)

            mainViewModel.performTimerEvent(
                {binding.changeBasicSchedule.scrollToPosition(0)},
                50L)
        }

        setupFunctions()
        dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(addButtonCheck, saveButtonCheck, 1)
        setupRecyclerView()
    }

    private fun setupFunctions() {
        saveButtonCheck = {
            mainViewModel.performTimerEvent({
                //updateSaveButton()
            }, 50L)
        }

        addButtonCheck = {array ->
            mainViewModel.performTimerEvent({
                binding.addButton.isEnabled = array.isEmpty()
                //updateSaveButton()
            }, 50L)
        }
    }

    private fun setupRecyclerView() {
        val currentRecyclerList = arrayListOf(Data_IntString(0, "Базовое расписание 0"))
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            changeBasicSchedule.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }
}