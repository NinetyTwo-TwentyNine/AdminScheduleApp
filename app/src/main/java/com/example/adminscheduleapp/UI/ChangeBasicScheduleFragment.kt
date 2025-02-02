package com.example.adminscheduleapp.UI

import android.os.Bundle
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
import com.example.adminscheduleapp.data.*
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_FAILED
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DATA_UPLOAD_SUCCESS
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_ID_DELETION
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_RESET_CHANGES
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_WARNING_SAVE_CHANGES
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_SHOULD_UPLOAD_SCHEDULE
import com.example.adminscheduleapp.data.Constants.APP_TOAST_SCHEDULE_DOWNLOAD_FAILED
import com.example.adminscheduleapp.databinding.BasicPopupWindowBinding
import com.example.adminscheduleapp.databinding.FragmentChangeBasicScheduleBinding
import com.example.adminscheduleapp.utils.Utils.checkIfFlatScheduleBaseEquals
import com.example.adminscheduleapp.utils.Utils.getPossibleId
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeBasicScheduleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDownloadObservers()
        mainViewModel.downloadBaseSchedule(currentDownloadStatus)
    }

    private fun setupFunctions() {
        saveButtonCheck = {
            val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
            scheduleViewModel.saveBaseScheduleNames(currentRecyclerList)
            mainViewModel.performTimerEvent({
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
            mainViewModel.performTimerEvent({
                scheduleViewModel.removeBaseSchedule(it)
            }, 50L)
        }

        moveToFragment = { dayNum, scheduleNum ->
            if (scheduleViewModel.checkBaseScheduleIdValidity(mainViewModel.getBaseSchedule(), scheduleNum)) {
                mainViewModel.chooseDay(dayNum)
                scheduleViewModel.chooseBaseSchedule(scheduleNum)
                requireView().findNavController()
                    .navigate(ChangeBasicScheduleFragmentDirections.actionChangeBasicScheduleFragmentToFragmentContainer())
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
        for (i in 0 until scheduleViewModel.getSavedBaseSchedule()!!.nameList.size) {
            currentRecyclerList.add(scheduleViewModel.getSavedBaseSchedule()!!.nameList[i])
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
        for (i in 0 until scheduleViewModel.getSavedBaseSchedule()!!.nameList.size) {
            currentRecyclerList.add(scheduleViewModel.getSavedBaseSchedule()!!.nameList[i])
        }
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
    }

    private fun setupView() {
        binding.addButton.isEnabled = true
        binding.addButton.setOnClickListener {
            val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)

            val new_id = getPossibleId(currentRecyclerList)
            var new_title_number = new_id
            var new_title = "Базовое расписание $new_title_number"

            val namesList = arrayListOf<String>()
            currentRecyclerList.forEach {
                namesList.add(it.title!!)
            }
            while (namesList.contains(new_title)) {
                new_title_number++
                new_title = "Базовое расписание $new_title_number"
            }

            currentRecyclerList.add(0, Data_IntString(new_id, new_title))
            scheduleViewModel.addNewBaseSchedule(new_id, new_title)

            //dbEditorRecyclerViewAdapter.notifyItemChanged(0)
            dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)

            mainViewModel.performTimerEvent(
                {binding.changeBasicSchedule.scrollToPosition(0)},
                50L)
        }

        binding.saveButton.setOnClickListener {
            val namelist_comparison = scheduleViewModel.compareParametersLists(mainViewModel.getBaseSchedule().nameList, scheduleViewModel.getSavedBaseSchedule()!!.nameList)
            var text = APP_ADMIN_WARNING_SAVE_CHANGES
            if (namelist_comparison.second) {
                text = APP_ADMIN_WARNING_ID_DELETION + "\n" + text
            }
            createPopupWindow(text, true) {
                if (checkIfFlatScheduleBaseEquals(mainViewModel.getBaseSchedule(), scheduleViewModel.getSavedBaseSchedule()!!, false) && !namelist_comparison.first) {
                    mainViewModel.uploadBaseScheduleNames(currentUploadStatus, scheduleViewModel.getSavedBaseSchedule()!!)
                } else {
                    mainViewModel.uploadBaseSchedule(currentUploadStatus, scheduleViewModel.getSavedBaseSchedule()!!)
                }
            }
        }
        binding.resetButton.setOnClickListener {
            val text = APP_ADMIN_WARNING_RESET_CHANGES
            createPopupWindow(text) {
                scheduleViewModel.saveBaseSchedule(mainViewModel.getBaseSchedule())
                mainViewModel.performTimerEvent(
                    { setupRecyclerView() },
                    50L) }
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

        val comparison = checkIfFlatScheduleBaseEquals(mainViewModel.getBaseSchedule(), scheduleViewModel.getSavedBaseSchedule()!!, true)
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
                }
                is DownloadStatus.Success<FlatScheduleBase> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    if (scheduleViewModel.getSavedBaseSchedule() == null) {
                        scheduleViewModel.saveBaseSchedule(mainViewModel.getBaseSchedule())
                    }
                    setupView()
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
                        updateRecyclerView()
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
                        updateRecyclerView()
                    }, 50L)
                }
            }
        }
    }
}