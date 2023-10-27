package com.example.scheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.ScheduleRecyclerViewAdapter
import com.example.scheduleapp.databinding.FragmentScheduleBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment() : Fragment() {
    private var index: Int? = null
    private val scheduleRecyclerViewAdapter by lazy { ScheduleRecyclerViewAdapter() }
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()
    private lateinit var binding: FragmentScheduleBinding


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
            val flatSchedule = mainViewModel.getSchedule()
            val currentGroup = scheduleViewModel.getGroupId(flatSchedule.groupList, scheduleViewModel.getGroup())
            val currentDate = scheduleViewModel.getDayId(flatSchedule.dayList, index!!)
            Log.d("TAG_FS", "currentGroup = ${currentGroup.toString()}, currentDay = ${currentDate.toString()}, index = ${index!!}")
            if (currentGroup != null && currentDate != null) {
                val currentSchedule = scheduleViewModel.getScheduleByGroupAndDayDetailed(currentGroup, currentDate, flatSchedule)
                if (currentSchedule != null) {
                    scheduleRecyclerViewAdapter.differ.submitList(currentSchedule)
                    binding.apply {
                        schedulesRecyclerView.apply {
                            layoutManager = LinearLayoutManager(activity)
                            adapter = scheduleRecyclerViewAdapter
                        }
                    }
                }
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