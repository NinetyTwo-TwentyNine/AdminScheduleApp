package com.example.scheduleapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.scheduleapp.R
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.scheduleapp.data.Constants.APP_ADMIN_CHOOSE_BASE_SCHEDULE_TEXT
import com.example.scheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_APPLY_BASE_SCHEDULE
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_MISSING_DAY
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_RESET_CHANGES
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_SAVE_CHANGES
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.data.UploadStatus
import com.example.scheduleapp.databinding.BasicPopupWindowBinding
import com.example.scheduleapp.databinding.FragmentContainerBinding
import com.example.scheduleapp.utils.Utils.checkIfFlatScheduleDetailedEquals
import com.example.scheduleapp.utils.Utils.changeSingleScheduleDay
import com.example.scheduleapp.utils.Utils.checkIfFlatScheduleBaseEquals
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

        when(mainViewModel.getEditMode()) {
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> setupViewBaseEditMode()
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> setupViewCurrentEditMode()
        }
    }

    private fun setupViewCurrentEditMode() {
        initDownloadObservers()
        mainViewModel.downloadCurrentSchedule(currentDownloadStatus)

        binding.saveButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_SAVE_CHANGES, true) {
                val uploadSchedule = changeSingleScheduleDay(
                    mainViewModel.getParameters().dayList,
                    baseSchedule = mainViewModel.getCurrentSchedule(),
                    newSchedule = scheduleViewModel.getSavedCurrentSchedule()!!,
                    mainViewModel.getDateWithOffset(mainViewModel.getChosenDayIndex())
                )
                mainViewModel.uploadCurrentSchedule(currentUploadStatus, uploadSchedule) }
        }
        binding.resetButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_RESET_CHANGES) {
                val resetSchedule = changeSingleScheduleDay(
                    mainViewModel.getParameters().dayList,
                    baseSchedule = scheduleViewModel.getSavedCurrentSchedule()!!,
                    newSchedule = mainViewModel.getCurrentSchedule(),
                    mainViewModel.getDateWithOffset(mainViewModel.getChosenDayIndex())
                )
                scheduleViewModel.saveCurrentSchedule(resetSchedule)
                mainViewModel.performTimerEvent(
                    { setupViewPager2() },
                    50L) }
        }

        val adapterArray = arrayListOf("")
        mainViewModel.getBaseSchedule().nameList.forEach { adapterArray.add(it.title!!) }
        binding.chooseScheduleSpinner.adapter = ArrayAdapter(binding.chooseScheduleSpinner.context, R.layout.spinner_item, adapterArray).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        binding.chooseScheduleSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val name = binding.chooseScheduleSpinner.getItemAtPosition(position).toString()
                when(position == 0) {
                    false -> {
                        binding.chooseScheduleSpinner.setSelection(0)
                        createPopupWindow(APP_ADMIN_WARNING_APPLY_BASE_SCHEDULE) {
                            scheduleViewModel.applyBaseSchedule(mainViewModel.getParameters().dayList, mainViewModel.getDateWithOffset(), mainViewModel.getBaseSchedule(), name, mainViewModel.getDayToTab())
                            mainViewModel.performTimerEvent(
                                { setupViewPager2() },
                                50L)
                        }
                    }
                    true -> {}
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.chooseScheduleSpinnerDefaultText.text = APP_ADMIN_CHOOSE_BASE_SCHEDULE_TEXT
        binding.dayName.text = mainViewModel.getDayToTab()
    }

    private fun setupViewBaseEditMode() {
        dontEverShowDayWarning = true
        setupViewPager2()

        binding.saveButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_SAVE_CHANGES, true) {
                val uploadSchedule = changeSingleScheduleDay(
                    dateId = mainViewModel.getChosenDayIndex(),
                    baseSchedule = mainViewModel.getBaseSchedule(),
                    newSchedule = scheduleViewModel.getSavedBaseSchedule()!!,
                    nameId = scheduleViewModel.getChosenBaseSchedule()!!
                )
                mainViewModel.uploadBaseSchedule(currentUploadStatus, uploadSchedule) }
        }
        binding.resetButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_RESET_CHANGES) {
                val resetSchedule = changeSingleScheduleDay(
                    dateId = mainViewModel.getChosenDayIndex(),
                    baseSchedule = scheduleViewModel.getSavedBaseSchedule()!!,
                    newSchedule = mainViewModel.getBaseSchedule(),
                    nameId = scheduleViewModel.getChosenBaseSchedule()!!
                )
                scheduleViewModel.saveBaseSchedule(resetSchedule)
                mainViewModel.performTimerEvent(
                    { setupViewPager2() },
                    50L) }
        }

        binding.chooseScheduleSpinner.isEnabled = false
        binding.chooseScheduleSpinnerDefaultText.text = scheduleViewModel.getBaseScheduleName()
        binding.dayName.text = mainViewModel.getDayToTab()
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
                    if (scheduleViewModel.getSavedCurrentSchedule() == null) {
                        scheduleViewModel.saveCurrentSchedule(mainViewModel.getCurrentSchedule())
                    }
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
        createPopupWindow(APP_ADMIN_WARNING_MISSING_DAY, noButtonDisabled = true) {
            dontEverShowDayWarning = true
            requireView().findNavController()
                .navigate(FragmentContainerDirections.actionFragmentContainerToSettingsFragment())
        }
    }

    fun updateResetAndSaveButtons(forcedBool: Boolean? = null) {
        Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "")

        val comparisonResult: Boolean?
        if (forcedBool == null) {
            comparisonResult = performTheoreticalUploadCheck()
            if (comparisonResult == null) {
                when(mainViewModel.getEditMode()) {
                    APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> {
                        showDayMissingWarning()
                        return
                    }
                    APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                        throw(Exception("Theoretical upload check result and edit mode mismatch."))
                    }
                }
            }
        } else {
            comparisonResult = !forcedBool
            Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "Forced boolean = $forcedBool")
        }

        if (forcedBool == null) {
            Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "Are they the same? $comparisonResult.")
        }
        binding.saveButton.isEnabled = !comparisonResult
        binding.resetButton.isEnabled = !comparisonResult
    }

    private fun performTheoreticalUploadCheck(): Boolean? {
        when (mainViewModel.getEditMode()) {
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> {
                val theoreticalUploadSchedule: FlatScheduleDetailed
                try {
                    theoreticalUploadSchedule = changeSingleScheduleDay(
                        mainViewModel.getParameters().dayList,
                        baseSchedule = mainViewModel.getCurrentSchedule(),
                        newSchedule = scheduleViewModel.getSavedCurrentSchedule()!!,
                        mainViewModel.getDateWithOffset()
                    )
                } catch (e: Exception) {
                    Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "The day ID was missing!")
                    return null
                }
                return checkIfFlatScheduleDetailedEquals(mainViewModel.getCurrentSchedule(), theoreticalUploadSchedule)
            }
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                val theoreticalUploadSchedule = changeSingleScheduleDay(
                    mainViewModel.getChosenDayIndex(),
                    baseSchedule = mainViewModel.getBaseSchedule(),
                    newSchedule = scheduleViewModel.getSavedBaseSchedule()!!,
                    scheduleViewModel.getChosenBaseSchedule()!!
                )
                return checkIfFlatScheduleBaseEquals(mainViewModel.getBaseSchedule(), theoreticalUploadSchedule, false)
            }
            else -> {
                return null
            }
        }
    }
}