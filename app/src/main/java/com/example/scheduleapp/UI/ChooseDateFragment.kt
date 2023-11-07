package com.example.scheduleapp.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scheduleapp.databinding.FragmentChooseDateBinding

/**
 * Main admin screen to choose group and date to set schedule.
 */
class ChooseDateFragment : Fragment() {

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

        binding.dateToCreateScheduleDatePicker.minDate = -7L
        binding.dateToCreateScheduleDatePicker.maxDate = 7L
    }
}