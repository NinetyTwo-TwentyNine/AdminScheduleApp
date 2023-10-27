package com.example.scheduleapp.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scheduleapp.R
import com.example.scheduleapp.databinding.FragmentAddScheduleForPositionBinding
import com.example.scheduleapp.databinding.FragmentAdminGroupAndDateBinding

/**
 *  Admin screen to choose add schedule to specific date to specific group
 *  writes data to firebase about lector, place and subject
 */
class AddScheduleForPosition : Fragment() {

    private lateinit var binding: FragmentAddScheduleForPositionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddScheduleForPositionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}