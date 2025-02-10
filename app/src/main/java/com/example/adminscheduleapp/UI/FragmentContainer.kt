package com.example.adminscheduleapp.UI

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
import com.example.adminscheduleapp.R
import com.example.adminscheduleapp.adapters.MainScreenAdapter
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CHOOSE_BASE_SCHEDULE_TEXT
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_GROUP_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_FAILED
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_APPLY_BASE_SCHEDULE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_RESET_CHANGES
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_SAVE_CHANGES
import com.example.adminscheduleapp.data.UploadStatus
import com.example.adminscheduleapp.databinding.BasicPopupWindowBinding
import com.example.adminscheduleapp.databinding.FragmentContainerBinding
import com.example.adminscheduleapp.utils.Utils.getItemId
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentContainer : Fragment() {
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var mainScreenAdapter: MainScreenAdapter
    private lateinit var binding: FragmentContainerBinding
    private lateinit var popupBinding: BasicPopupWindowBinding
    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>


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
        setupViewPager2()
        initUploadObservers()

        val currentDateId = getItemId(mainViewModel.getParameters().dayList, mainViewModel.getDateWithOffset())
        binding.saveButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_SAVE_CHANGES, true) {
                mainViewModel.applyStagedChangesToScheduleCurrent(currentUploadStatus, scheduleViewModel, currentDateId!!)
            }
        }
        binding.resetButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_RESET_CHANGES) {
                mainViewModel.resetStagedChangesToScheduleCurrent(currentUploadStatus, scheduleViewModel, currentDateId!!)
            }
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
                            mainViewModel.applyBaseScheduleToCurrent(currentUploadStatus, scheduleViewModel, baseScheduleName = name)
                        }
                    }
                    true -> {}
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.chooseScheduleSpinnerDefaultText.text = APP_ADMIN_CHOOSE_BASE_SCHEDULE_TEXT
        binding.dayName.text = mainViewModel.getDayToTab()

        if (mainViewModel.shouldDataBeUploaded()) {
            mainViewModel.setNextUpload(false)
            val groupId = getItemId(mainViewModel.getParameters().groupList, scheduleViewModel.getChosenGroup())
            mainViewModel.stageCurrentSchedulePair(currentUploadStatus, scheduleViewModel, groupId = groupId!!, dateId = currentDateId!!, pairNum = scheduleViewModel.getChosenPairNum()!!, scheduleViewModel.getChosenScheduleItem()!!)
        }
    }

    private fun setupViewBaseEditMode() {
        setupViewPager2()
        initUploadObservers()

        binding.saveButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_SAVE_CHANGES, true) {
                mainViewModel.applyStagedChangesToScheduleBase(currentUploadStatus, scheduleViewModel, mainViewModel.getChosenDayIndex(), scheduleViewModel.getChosenBaseSchedule()!!) }
        }
        binding.resetButton.setOnClickListener {
            createPopupWindow(APP_ADMIN_WARNING_RESET_CHANGES) {
                mainViewModel.resetStagedChangesToScheduleBase(currentUploadStatus, scheduleViewModel, mainViewModel.getChosenDayIndex(), scheduleViewModel.getChosenBaseSchedule()!!) }
        }

        binding.chooseScheduleSpinner.isEnabled = false
        binding.chooseScheduleSpinnerDefaultText.text = scheduleViewModel.getBaseScheduleName()
        binding.dayName.text = mainViewModel.getDayToTab()

        if (mainViewModel.shouldDataBeUploaded()) {
            mainViewModel.setNextUpload(false)
            val groupId = getItemId(mainViewModel.getParameters().groupList, scheduleViewModel.getChosenGroup())
            mainViewModel.stageBaseSchedulePair(currentUploadStatus, scheduleViewModel, groupId = groupId!!, dayNum = mainViewModel.getChosenDayIndex(), pairNum = scheduleViewModel.getChosenPairNum()!!, nameId = scheduleViewModel.getChosenBaseSchedule()!!, scheduleViewModel.getChosenScheduleItem()!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewPager2() {
        val groupArray = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_GROUP_NAME)

        mainScreenAdapter = MainScreenAdapter(this, groupArray.size)
        binding.fragmentViewPager2.adapter = mainScreenAdapter
        binding.fragmentViewPager2.currentItem = 0

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager2) { tab, position ->
            tab.text = position.toString()
        }.attach()

        for (i in 0 until groupArray.size) {
            binding.tabLayout.getTabAt(i)?.text = groupArray[i]
        }
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
                        "$APP_ADMIN_TOAST_DATA_UPLOAD_FAILED: ${uploadStatus.message}",
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
                        "$APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS.",
                        Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.performTimerEvent({
                        setupViewPager2()
                        updateResetAndSaveButtons()
                    }, 50L)
                }
            }
        }
    }

    fun clearChosenPair(currentDayId: Int, currentGroup: Int, number: Int) {
        when (mainViewModel.getEditMode()) {
            APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> mainViewModel.stageCurrentSchedulePair(currentUploadStatus, scheduleViewModel, currentGroup, currentDayId, number, null)
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> mainViewModel.stageBaseSchedulePair(currentUploadStatus, scheduleViewModel, currentGroup, currentDayId, number, scheduleViewModel.getChosenBaseSchedule()!!, null)
        }
    }

    fun moveToAddPairFragment() {
        requireView().findNavController()
            .navigate(FragmentContainerDirections.actionFragmentContainerToAddPairFragment())
    }

    fun updateResetAndSaveButtons(forcedBool: Boolean? = null) {
        Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "")

        val comparisonResult: Boolean?
        if (forcedBool == null) {
            comparisonResult = performTheoreticalUploadCheck()
        } else {
            comparisonResult = !forcedBool
            Log.d("ADMIN_RESET&UPLOAD_BUTTONS_CHECK", "Forced boolean = $forcedBool")
        }

        if (comparisonResult == null) {
            throw(AssertionError("Null comparison result when updating save&reset buttons."))
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
                return scheduleViewModel.isStagedScheduleCurrentSame().second
            }
            APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> {
                return scheduleViewModel.isStagedScheduleBaseSame().second
            }
            else -> {
                return null
            }
        }
    }
}