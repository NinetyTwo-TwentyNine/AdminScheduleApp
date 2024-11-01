package com.example.adminscheduleapp.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.adminscheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.adminscheduleapp.databinding.FragmentChooseDateBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.data.Date
import java.util.*

class ChooseDateFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentChooseDateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChooseDateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mCalendar = Calendar.getInstance()
        val minDate = viewModel.getDateWithOffset(0)
        val maxDate = viewModel.getDateWithOffset(PAGE_COUNT-1)

        mCalendar.set(minDate.year!!, minDate.month!!-1, minDate.day!!)
        binding.dateToCreateScheduleDatePicker.minDate = mCalendar.timeInMillis

        mCalendar.set(maxDate.year!!, maxDate.month!!-1, maxDate.day!!)
        binding.dateToCreateScheduleDatePicker.maxDate = mCalendar.timeInMillis

        binding.goNextButton.setOnClickListener {
            viewModel.chooseDay(Date(binding.dateToCreateScheduleDatePicker.year, binding.dateToCreateScheduleDatePicker.month+1, binding.dateToCreateScheduleDatePicker.dayOfMonth))
            requireView().findNavController()
                .navigate(ChooseDateFragmentDirections.actionChooseDateFragmentToFragmentContainer())
        }
    }
}