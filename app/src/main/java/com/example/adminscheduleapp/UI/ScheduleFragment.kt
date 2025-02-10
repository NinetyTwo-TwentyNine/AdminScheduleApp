package com.example.adminscheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminscheduleapp.adapters.ScheduleRecyclerViewAdapter
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.adminscheduleapp.data.Schedule
import com.example.adminscheduleapp.databinding.FragmentScheduleBinding
import com.example.adminscheduleapp.utils.Utils.getItemId
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment() : Fragment() {
    private var index: Int? = null
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var binding: FragmentScheduleBinding
    private lateinit var scheduleRecyclerViewAdapter: ScheduleRecyclerViewAdapter

    private lateinit var editButtonFunction: (Int)->Unit
    private lateinit var clearButtonFunction: (Int)->Unit


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScheduleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        val args = arguments
        index = args?.getInt("index", 0)

        if (index != null) {
            when(mainViewModel.getEditMode()) {
                APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> setupViewCurrentEditMode()
                APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> setupViewBaseEditMode()
            }
        }
    }

    private fun setupViewCurrentEditMode() {
        val scheduleParams = mainViewModel.getParameters()
        val currentGroup = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_GROUP_NAME)[index!!]
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
        val currentDate = mainViewModel.getDateWithOffset()
        val currentDateId = getItemId(scheduleParams.dayList, currentDate)
        Log.d("TAG_FS", "currentGroup = ${currentGroupId.toString()}, currentDay = ${currentDateId.toString()}, index = ${index!!}")
        var currentSchedule = scheduleViewModel.getScheduleByGroupAndDay(currentGroupId, currentDateId, scheduleParams, APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE)

        editButtonFunction = {number ->
            if (scheduleViewModel.chooseScheduleItemCurrent(scheduleParams, currentDate, currentGroup, number, mainViewModel.getCurrentSchedule())) {
                (this.parentFragment as FragmentContainer).moveToAddPairFragment()
            }
        }
        clearButtonFunction = {number ->
            (this.parentFragment as FragmentContainer).clearChosenPair(currentDateId!!, currentGroupId!!, number)
        }

        setupRecyclerView(currentSchedule)
    }

    private fun setupViewBaseEditMode() {
        val scheduleParams = mainViewModel.getParameters()
        val currentGroup = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_GROUP_NAME)[index!!]
        val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
        val currentDayId = mainViewModel.getChosenDayIndex()
        Log.d("TAG_FS", "currentGroup = ${currentGroupId.toString()}, currentDay = ${currentDayId}, index = ${index!!}")
        var currentSchedule = scheduleViewModel.getScheduleByGroupAndDay(currentGroupId, currentDayId, scheduleParams, APP_ADMIN_BASE_SCHEDULE_EDIT_MODE)

        editButtonFunction = {number ->
            if (scheduleViewModel.chooseScheduleItemBase(scheduleParams, currentDayId, currentGroup, number, baseSchedule = mainViewModel.getBaseSchedule())) {
                (this.parentFragment as FragmentContainer).moveToAddPairFragment()
            }
        }
        clearButtonFunction = {number ->
            (this.parentFragment as FragmentContainer).clearChosenPair(mainViewModel.getChosenDayIndex(), currentGroupId!!, number)
        }

        setupRecyclerView(currentSchedule)
    }

    private fun setupRecyclerView(currentSchedule: ArrayList<Schedule>) {
        scheduleRecyclerViewAdapter = ScheduleRecyclerViewAdapter(editButtonFunction, clearButtonFunction)
        scheduleRecyclerViewAdapter.differ.submitList(currentSchedule)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = scheduleRecyclerViewAdapter
            }
        }

        mainViewModel.performTimerEvent(
            {(this.parentFragment as FragmentContainer).updateResetAndSaveButtons()},
            50L)
    }

    companion object {
        fun newInstance(position: Int): ScheduleFragment {
            val fragment = ScheduleFragment()
            val args = Bundle()
            args.putInt("index", position)
            fragment.arguments = args
            return fragment
        }
    }
}