package com.example.scheduleapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.data.Constants.APP_ADMIN_MISSING_DAY_WARNING
import com.example.scheduleapp.data.Constants.APP_ADMIN_RESET_CHANGES_WARNING
import com.example.scheduleapp.data.Constants.APP_ADMIN_SAVE_CHANGES_WARNING
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.data.UploadStatus
import com.example.scheduleapp.databinding.BasicPopupWindowBinding
import com.example.scheduleapp.databinding.FragmentContainerBinding
import com.example.scheduleapp.utils.Utils.checkIfFlatScheduleDetailedEquals
import com.example.scheduleapp.utils.Utils.changeSingleScheduleDay
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentContainer : Fragment() {
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()
    private lateinit var mainScreenAdapter: MainScreenAdapter
    private lateinit var binding: FragmentContainerBinding
    private lateinit var popupBinding: BasicPopupWindowBinding
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>
    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>

    private var dontEverShowDayWarning = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContainerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (scheduleViewModel.getSavedSchedule() == null) {
            initDownloadObservers()
            mainViewModel.downloadSchedule(currentDownloadStatus)
        } else {
            setupViewPager2()
        }

        binding.saveButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_SAVE_CHANGES_WARNING, true) {
                val uploadSchedule = changeSingleScheduleDay(
                    mainViewModel.getParameters().dayList,
                    baseSchedule = mainViewModel.getSchedule(),
                    newSchedule = scheduleViewModel.getSavedSchedule()!!,
                    mainViewModel.getDayWithOffset(mainViewModel.getChosenDate())
                )
                mainViewModel.uploadCurrentSchedule(currentUploadStatus, uploadSchedule) }
        }
        binding.resetButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_RESET_CHANGES_WARNING) {
                val resetSchedule = changeSingleScheduleDay(
                    mainViewModel.getParameters().dayList,
                    baseSchedule = scheduleViewModel.getSavedSchedule()!!,
                    newSchedule = mainViewModel.getSchedule(),
                    mainViewModel.getDayWithOffset(mainViewModel.getChosenDate())
                )
                scheduleViewModel.saveSchedule(resetSchedule)
                mainViewModel.performTimerEvent(
                    { setupViewPager2() },
                    50L) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewPager2() {
        val groupArray = mainViewModel.getParametersList(APP_BD_PATHS_GROUP_LIST)

        mainScreenAdapter = MainScreenAdapter(this, groupArray.size)
        binding.fragmentViewPager2.adapter = mainScreenAdapter
        binding.fragmentViewPager2.currentItem = 0

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager2) { tab, position ->
            tab.text = position.toString()
        }.attach()

        for (i in 0 until groupArray.size) {
            binding.tabLayout.getTabAt(i)?.text = groupArray[i]
        }
        binding.dayName.text = mainViewModel.getDayToTab(mainViewModel.getChosenDate())

        initUploadObservers()
    }

    private fun createPopupWindow(warning: String, alwaysDisableButtons: Boolean = false, noButtonDisabled: Boolean = false, yesFunc: ()->Unit) {
        updateResetAndSaveButtons(false)
        popupBinding = BasicPopupWindowBinding.inflate(layoutInflater)
        val popupView: View = popupBinding.root

        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupBinding.popupText.text = warning
        popupBinding.yesButton.setOnClickListener {
            yesFunc()
            popupWindow.dismiss()
            if (alwaysDisableButtons) {
                updateResetAndSaveButtons(false)
            }
        }
        popupBinding.noButton.isEnabled = !noButtonDisabled
        popupBinding.noButton.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.showAtLocation(this.view, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
            updateResetAndSaveButtons()
        }
    }

    private fun initDownloadObservers() {
        currentDownloadStatus = MutableLiveData()
        currentDownloadStatus.observe(viewLifecycleOwner) { downloadStatus ->

            when (downloadStatus) {
                is DownloadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
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
                        "Failed to download Schedule: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<FlatScheduleDetailed> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    scheduleViewModel.saveSchedule(mainViewModel.getSchedule())
                    setupViewPager2()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
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
                        "Failed to upload the Data: ${uploadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.performTimerEvent({
                        updateResetAndSaveButtons()
                    }, 50L)
                }
                is UploadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "Succeeded in uploading the Data.",
                        Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.performTimerEvent({
                        updateResetAndSaveButtons()
                    }, 50L)
                }
            }
        }
    }

    fun moveToAddPairFragment() {
        requireView().findNavController()
            .navigate(FragmentContainerDirections.actionFragmentContainerToAddPairFragment())
    }

    private fun showDayMissingWarning() {
        if (dontEverShowDayWarning) { dontEverShowDayWarning = false; return }
        createPopupWindow(APP_ADMIN_MISSING_DAY_WARNING, noButtonDisabled = true) {
            dontEverShowDayWarning = true
            requireView().findNavController()
                .navigate(FragmentContainerDirections.actionFragmentContainerToSettingsFragment())
        }
    }

    fun updateResetAndSaveButtons(forcedBool: Boolean? = null) {
        Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "")

        val comparisonResult: Boolean
        if (forcedBool == null) {
            val theoreticalUploadSchedule: FlatScheduleDetailed
            try {
                theoreticalUploadSchedule = changeSingleScheduleDay(
                    mainViewModel.getParameters().dayList,
                    baseSchedule = mainViewModel.getSchedule(),
                    newSchedule = scheduleViewModel.getSavedSchedule()!!,
                    mainViewModel.getDayWithOffset(mainViewModel.getChosenDate())
                )
            } catch (e: Exception) {
                Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "The day ID was missing!")
                showDayMissingWarning()
                return
            }
            comparisonResult = checkIfFlatScheduleDetailedEquals(mainViewModel.getSchedule(), theoreticalUploadSchedule)
        } else {
            comparisonResult = !forcedBool
            Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "Forced boolean = $forcedBool")
        }
        Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "Are they the same? $comparisonResult.")
        binding.saveButton.isEnabled = !comparisonResult
        binding.resetButton.isEnabled = !comparisonResult
    }
}