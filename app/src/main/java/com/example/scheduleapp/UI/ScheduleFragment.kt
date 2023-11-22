package com.example.scheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.adapters.ScheduleRecyclerViewAdapter
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.Schedule
import com.example.scheduleapp.databinding.FragmentScheduleBinding
import com.example.scheduleapp.utils.Utils.getItemId
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment() : Fragment() {
    private var index: Int? = null
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()
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
            val scheduleParams = mainViewModel.getParameters()
            val currentGroup = mainViewModel.getParametersList(APP_BD_PATHS_GROUP_LIST)[index!!]
            val currentGroupId = getItemId(scheduleParams.groupList, currentGroup)
            val currentDate = mainViewModel.getDayWithOffset(mainViewModel.getChosenDate())
            val currentDateId = getItemId(scheduleParams.dayList, currentDate)
            Log.d("TAG_FS", "currentGroup = ${currentGroupId.toString()}, currentDay = ${currentDateId.toString()}, index = ${index!!}")
            var currentSchedule = scheduleViewModel.getScheduleByGroupAndDay(currentGroupId, currentDateId, scheduleParams)

            editButtonFunction = {number ->
                scheduleViewModel.chooseScheduleItem(scheduleParams, currentDate, currentGroup, number)
                (this.parentFragment as FragmentContainer).moveToAddPairFragment()
            }
            clearButtonFunction = {number ->
                scheduleViewModel.removeScheduleItem(scheduleParams, currentDate, currentGroup, number)
                currentSchedule = scheduleViewModel.getScheduleByGroupAndDay(currentGroupId, currentDateId, scheduleParams)
                setupRecyclerView(currentSchedule)

                mainViewModel.performTimerEvent(
                    {binding.schedulesRecyclerView.scrollToPosition(number + 1)},
                50L)
            }

            setupRecyclerView(currentSchedule)
        }
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