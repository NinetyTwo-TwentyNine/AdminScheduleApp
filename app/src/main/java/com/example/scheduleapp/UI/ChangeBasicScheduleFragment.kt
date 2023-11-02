package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.ChangeBasicScheduleRecyclerViewAdapter
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.FragmentChangeBasicScheduleBinding

class ChangeBasicScheduleFragment() : Fragment() {

    private lateinit var binding: FragmentChangeBasicScheduleBinding
    private val changeBasicScheduleRecyclerViewAdapter by lazy { ChangeBasicScheduleRecyclerViewAdapter() }
    private val basicSchedule: ArrayList<Data_IntString> =
        arrayListOf(Data_IntString(0, "Базовое расписание 0"))


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeBasicScheduleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        changeBasicScheduleRecyclerViewAdapter.differ.submitList(basicSchedule)
        binding.apply {
            changeBasicSchedule.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = changeBasicScheduleRecyclerViewAdapter
            }
        }

        binding.cardView.setOnClickListener {
            basicSchedule.add(Data_IntString(checkId(), "Базовое расписание ${checkId()}"))
            updateList()
        }
    }


    private fun updateList(){
        changeBasicScheduleRecyclerViewAdapter.differ.submitList(basicSchedule)
        binding.apply {
            changeBasicSchedule.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = changeBasicScheduleRecyclerViewAdapter
            }
        }
    }

    private fun checkId(): Int {
        var q=0
        for(i in basicSchedule){
           if(q==i.id!!){
               q=i.id!!+1
           }
        }
        return q
    }
}