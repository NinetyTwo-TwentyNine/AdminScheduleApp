package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.scheduleapp.data.Constants.APP_ADMIN_TOAST_DAY_LIST_UPLOAD_FAILED
import com.example.scheduleapp.data.Constants.APP_ADMIN_TOAST_DAY_LIST_UPLOAD_SUCCESS
import com.example.scheduleapp.data.Constants.APP_PREFERENCES_AUTOUPDATE
import com.example.scheduleapp.data.UploadStatus
import com.example.scheduleapp.databinding.FragmentAdminPanelBinding
import com.example.scheduleapp.utils.Utils.checkIfFlatScheduleBaseEquals
import com.example.scheduleapp.utils.Utils.checkIfFlatScheduleDetailedEquals
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleViewModel

class AdminPanelFragment: Fragment() {
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var binding: FragmentAdminPanelBinding
    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>

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
            if (checkIfFlatScheduleBaseEquals(mainViewModel.getBaseSchedule(), scheduleViewModel.getSavedBaseSchedule()!!, true)) {
                scheduleViewModel.clearBaseSchedule()
            }
        }
        if (scheduleViewModel.getSavedCurrentSchedule() != null) {
            if (checkIfFlatScheduleDetailedEquals(mainViewModel.getCurrentSchedule(), scheduleViewModel.getSavedCurrentSchedule()!!)) {
                scheduleViewModel.clearCurrentSchedule()
            }
        }

        initUploadObservers()
        if (mainViewModel.getPreference(APP_PREFERENCES_AUTOUPDATE, true)) {
            if (!mainViewModel.updateAndUploadTheDayList(currentUploadStatus)) {
                setupView()
            }
        } else {
            setupView()
        }
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

    private fun initUploadObservers() {
        currentUploadStatus = MutableLiveData()
        currentUploadStatus.observe(viewLifecycleOwner) { uploadStatus ->
            when (uploadStatus) {
                is UploadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UploadStatus.WeakProgress -> {
                    Toast.makeText(
                        activity,
                        uploadStatus.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is UploadStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_ADMIN_TOAST_DAY_LIST_UPLOAD_FAILED: ${uploadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    currentUploadStatus.removeObservers(viewLifecycleOwner)
                }
                is UploadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_ADMIN_TOAST_DAY_LIST_UPLOAD_SUCCESS.",
                        Toast.LENGTH_LONG
                    ).show()
                    currentUploadStatus.removeObservers(viewLifecycleOwner)
                    setupView()
                }
            }
        }
    }
}