package com.example.adminscheduleapp.UI

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminscheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_FAILED
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_SHOULD_UPLOAD_SCHEDULE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_ID_DELETION
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_RESET_CHANGES
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_SAVE_CHANGES
import com.example.adminscheduleapp.data.Constants.APP_TOAST_SCHEDULE_DOWNLOAD_FAILED
import com.example.adminscheduleapp.data.Data_IntString
import com.example.adminscheduleapp.data.DownloadStatus
import com.example.adminscheduleapp.data.FlatScheduleBase
import com.example.adminscheduleapp.data.UploadStatus
import com.example.adminscheduleapp.databinding.BasicPopupWindowBinding
import com.example.adminscheduleapp.databinding.FragmentChangeBasicScheduleBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel

class ChangeBasicScheduleFragment() : Fragment() {
    private lateinit var binding: FragmentChangeBasicScheduleBinding
    private lateinit var popupBinding: BasicPopupWindowBinding
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatScheduleBase>>

    private lateinit var addButtonCheck: (ArrayList<Int>)->Unit
    private lateinit var saveButtonCheck: ()->Unit
    private lateinit var deleteFunction: (Int)->Unit
    private lateinit var moveToFragment: (Int, Int)->Unit

    private var shouldMoveToNextFragment: Boolean = false
    private var shouldRestartRecyclerView: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeBasicScheduleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDownloadObservers()
        mainViewModel.downloadBaseSchedule(currentDownloadStatus, scheduleViewModel)
    }

    private fun setupFunctions() {
        saveButtonCheck = {
            mainViewModel.performTimerEvent({
                val currentScheduleList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
                for (item in currentScheduleList) {
                    if (scheduleViewModel.getBaseScheduleName(item.id!!) != item.title!!) {
                        mainViewModel.stageBaseScheduleList(currentUploadStatus, scheduleViewModel, item)
                        break
                    }
                }
                updateResetAndSaveButtons()
            }, 50L)
        }

        addButtonCheck = {array ->
            mainViewModel.performTimerEvent({
                binding.addButton.isEnabled = array.isEmpty()
                updateResetAndSaveButtons()
            }, 50L)
        }

        deleteFunction = {
            Log.d("APP_DEBUGGER", "Trying to delete a schedule: ${Data_IntString(it)}.")
            mainViewModel.stageBaseScheduleList(currentUploadStatus, scheduleViewModel, Data_IntString(it))
        }

        moveToFragment = { dayNum, scheduleNum ->
            if (scheduleViewModel.checkBaseScheduleIdValidity(mainViewModel.getBaseSchedule(), scheduleNum)) {
                mainViewModel.chooseDay(dayNum)
                scheduleViewModel.chooseBaseSchedule(scheduleNum)

                initDownloadObservers()
                shouldMoveToNextFragment = true
                mainViewModel.downloadBaseSchedule(currentDownloadStatus, scheduleViewModel, dayNum, scheduleNum)
            } else {
                Toast.makeText(
                    activity,
                    APP_ADMIN_TOAST_SHOULD_UPLOAD_SCHEDULE,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupRecyclerView() {
        dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(updateAddButton = addButtonCheck, updateSaveButton = saveButtonCheck,
            1, deleteFunc = deleteFunction, cardSpinnerFunc = moveToFragment)

        val currentRecyclerList: ArrayList<Data_IntString> = arrayListOf()
        for (schedule in scheduleViewModel.getSavedBaseSchedule()!!.nameList) {
            currentRecyclerList.add(schedule)
        }
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            changeBasicSchedule.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }

    private fun updateRecyclerView() {
        val currentRecyclerList: ArrayList<Data_IntString> = arrayListOf()
        for (schedule in scheduleViewModel.getSavedBaseSchedule()!!.nameList) {
            currentRecyclerList.add(schedule)
        }
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        mainViewModel.performTimerEvent({
            updateResetAndSaveButtons()
        }, 50L)
    }

    private fun setupView() {
        binding.addButton.isEnabled = true
        binding.addButton.setOnClickListener {
            mainViewModel.stageBaseScheduleList(currentUploadStatus, scheduleViewModel, Data_IntString())
        }

        binding.saveButton.setOnClickListener {
            val namelist_comparison = scheduleViewModel.compareParametersLists(mainViewModel.getBaseSchedule().nameList, scheduleViewModel.getSavedBaseSchedule()!!.nameList)
            var text = APP_ADMIN_WARNING_SAVE_CHANGES
            if (namelist_comparison.second) {
                text = APP_ADMIN_WARNING_ID_DELETION + "\n" + text
            }
            createPopupWindow(text, true) {
                mainViewModel.applyStagedChangesToScheduleBase(currentUploadStatus, scheduleViewModel)
            }
        }
        binding.resetButton.setOnClickListener {
            val text = APP_ADMIN_WARNING_RESET_CHANGES
            createPopupWindow(text) {
                shouldRestartRecyclerView = true
                mainViewModel.resetStagedChangesToScheduleBase(currentUploadStatus, scheduleViewModel) }
        }

        setupFunctions()
        setupRecyclerView()
        initUploadObservers()
    }

    private fun updateResetAndSaveButtons(b: Boolean? = null) {
        if (b != null) {
            binding.saveButton.isEnabled = b
            binding.resetButton.isEnabled = b
            return
        }

        val unacceptableUploadState = when (currentUploadStatus.value) {
            is UploadStatus.Progress -> {
                true
            }
            is UploadStatus.WeakProgress -> {
                true
            }
            else -> false
        }
        if (!binding.addButton.isEnabled || unacceptableUploadState) {
            binding.saveButton.isEnabled = false
            binding.resetButton.isEnabled = false
            return
        }

        val comparison = scheduleViewModel.isStagedScheduleBaseSame().first
        if (comparison == null) {
            throw(AssertionError("Null comparison result when updating reset&save buttons."))
        }
        binding.saveButton.isEnabled = !comparison
        binding.resetButton.isEnabled = !comparison
    }

    private fun createPopupWindow(text: String, alwaysDisableButtons: Boolean = false, yesFunc: ()->Unit) {
        updateResetAndSaveButtons(false)
        popupBinding = BasicPopupWindowBinding.inflate(layoutInflater)
        val popupView: View = popupBinding.root

        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(popupView, width, height, true)

        popupBinding.popupText.text = text
        popupBinding.yesButton.setOnClickListener {
            yesFunc()
            popupWindow.dismiss()
            if (alwaysDisableButtons) {
                updateResetAndSaveButtons(false)
            }
        }
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
                        "$APP_TOAST_SCHEDULE_DOWNLOAD_FAILED: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    shouldMoveToNextFragment = false
                }
                is DownloadStatus.Success<FlatScheduleBase> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)

                    if (shouldMoveToNextFragment) {
                        shouldMoveToNextFragment = false
                        requireView().findNavController()
                            .navigate(ChangeBasicScheduleFragmentDirections.actionChangeBasicScheduleFragmentToFragmentContainer())
                    } else {
                        setupView()
                    }
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
                        "$APP_ADMIN_TOAST_DATA_UPLOAD_FAILED: ${uploadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.performTimerEvent({
                            updateRecyclerView() }, 50L)
                }
                is UploadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS.",
                        Toast.LENGTH_LONG
                    ).show()
                    mainViewModel.performTimerEvent({
                        if (shouldRestartRecyclerView) {
                            shouldRestartRecyclerView = false
                            setupRecyclerView()
                        } else {
                            updateRecyclerView()
                        }
                    }, 50L)
                }
            }
        }
    }
}