package com.example.adminscheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.adminscheduleapp.databinding.FragmentAdminPanelBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel

class AdminPanelFragment: Fragment() {
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var binding: FragmentAdminPanelBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (scheduleViewModel.getSavedBaseSchedule() != null) {
            if (scheduleViewModel.isStagedScheduleBaseSame().first == true) {
                scheduleViewModel.clearBaseSchedule()
            }
        }
        if (scheduleViewModel.getSavedCurrentSchedule() != null) {
            if (scheduleViewModel.isStagedScheduleCurrentSame().first == true) {
                scheduleViewModel.clearCurrentSchedule()
            }
        }

        setupView()
    }

    private fun setupView() {
        binding.changingBaseData.isEnabled = true
        binding.changingBaseData.setOnClickListener {
            requireView().findNavController()
                .navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToDbFragmentContainer())
        }

        binding.changingBasicSchedule.isEnabled = true
        binding.changingBasicSchedule.setOnClickListener{
            mainViewModel.setBaseScheduleEditMode()
            requireView().findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToChangeBasicScheduleFragment())
        }

        binding.creatingSchedule.isEnabled = true
        binding.creatingSchedule.setOnClickListener{
            mainViewModel.setCurrentScheduleEditMode()
            requireView().findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToChooseDateFragment())
        }
    }
}