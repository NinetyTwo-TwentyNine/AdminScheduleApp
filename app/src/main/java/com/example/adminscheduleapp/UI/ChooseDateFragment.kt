package com.example.adminscheduleapp.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.adminscheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.adminscheduleapp.data.Constants.APP_TOAST_SCHEDULE_DOWNLOAD_FAILED
import com.example.adminscheduleapp.databinding.FragmentChooseDateBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.data.Date
import com.example.adminscheduleapp.data.DownloadStatus
import com.example.adminscheduleapp.data.FlatScheduleDetailed
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel
import java.util.*

class ChooseDateFragment : Fragment() {
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var binding: FragmentChooseDateBinding
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChooseDateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDownloadObservers()
        mainViewModel.downloadCurrentSchedule(currentDownloadStatus)
    }

    private fun setupView() {
        binding.dateToCreateScheduleDatePicker.isEnabled = true
        binding.goNextButton.isEnabled = true

        val mCalendar = Calendar.getInstance()
        val minDate = mainViewModel.getDateWithOffset(0)
        val maxDate = mainViewModel.getDateWithOffset(PAGE_COUNT-1)

        mCalendar.set(minDate.year!!, minDate.month!!-1, minDate.day!!)
        binding.dateToCreateScheduleDatePicker.minDate = mCalendar.timeInMillis

        mCalendar.set(maxDate.year!!, maxDate.month!!-1, maxDate.day!!)
        binding.dateToCreateScheduleDatePicker.maxDate = mCalendar.timeInMillis

        binding.goNextButton.setOnClickListener {
            mainViewModel.chooseDay(Date(binding.dateToCreateScheduleDatePicker.year, binding.dateToCreateScheduleDatePicker.month+1, binding.dateToCreateScheduleDatePicker.dayOfMonth))
            requireView().findNavController()
                .navigate(ChooseDateFragmentDirections.actionChooseDateFragmentToFragmentContainer())
        }
    }

    private fun initDownloadObservers() {
        currentDownloadStatus = MutableLiveData()
        currentDownloadStatus.observe(viewLifecycleOwner) { downloadStatus ->

            when (downloadStatus) {
                is DownloadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.dateToCreateScheduleDatePicker.isEnabled = false
                    binding.goNextButton.isEnabled = false
                }
                is DownloadStatus.WeakProgress -> {
                    Toast.makeText(
                        activity,
                        downloadStatus.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    Toast.makeText(
                        activity,
                        "${APP_TOAST_SCHEDULE_DOWNLOAD_FAILED}: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<FlatScheduleDetailed> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    if (scheduleViewModel.getSavedCurrentSchedule() == null) {
                        scheduleViewModel.saveCurrentSchedule(mainViewModel.getCurrentSchedule())
                    }
                    setupView()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }
}