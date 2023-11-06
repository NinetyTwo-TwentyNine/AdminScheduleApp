package com.example.scheduleapp.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scheduleapp.R
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.databinding.FragmentAdminGroupAndDateBinding
import com.example.scheduleapp.databinding.FragmentContainerBinding

/**
 * Main admin screen to choose group and date to set schedule.
 */
class AdminGroupAndDateFragment : Fragment() {

    private lateinit var binding: FragmentAdminGroupAndDateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAdminGroupAndDateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateToCreateScheduleDatePicker.minDate = -7L
        binding.dateToCreateScheduleDatePicker.maxDate = 7L
    }
}