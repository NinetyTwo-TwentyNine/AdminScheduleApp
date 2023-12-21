package com.example.scheduleapp.UI

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_ID_DELETION
import com.example.scheduleapp.data.Constants.APP_ADMIN_WARNING_SAVE_CHANGES
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.UploadStatus
import com.example.scheduleapp.databinding.BasicPopupWindowBinding
import com.example.scheduleapp.databinding.FragmentDataBaseEditBinding
import com.example.scheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.scheduleapp.utils.Utils.getPossibleId
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel
import kotlin.collections.ArrayList


class DataBaseEditFragment: Fragment() {
    private var index: Int? = null
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter
    private lateinit var binding: FragmentDataBaseEditBinding
    private lateinit var popupBinding: BasicPopupWindowBinding
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()

    private lateinit var addButtonCheck: (ArrayList<Int>)->Unit
    private lateinit var saveButtonCheck: ()->Unit
    private lateinit var checkIfDeletionIsPossible: (Int)->Boolean

    private lateinit var uploadRecyclerList: ArrayList<Data_IntString>
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<ArrayList<Data_IntString>>>
    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataBaseEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        val args = arguments
        index = args?.getInt("index", 0)

        if (index != null) {
            initDownloadObservers()
            mainViewModel.downloadParametersList(currentDownloadStatus, mainViewModel.getReferenceByIndex(index!!))
        }
    }

    private fun setupView() {
        binding.addButton.setOnClickListener {
            val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
            val currentId = getPossibleId(currentRecyclerList)
            currentRecyclerList.add(0, Data_IntString(currentId, ""))

            //dbEditorRecyclerViewAdapter.notifyItemChanged(0)
            dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)

            mainViewModel.performTimerEvent(
                {binding.parametersRecyclerView.scrollToPosition(0)},
                50L)
        }

        binding.saveButton.setOnClickListener {
            createPopupWindow()
        }

        setupFunctions()
        dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(updateAddButton = addButtonCheck, updateSaveButton = saveButtonCheck,
            2, checkIfDeletionIsPossible = checkIfDeletionIsPossible)
        setupRecyclerView()
        initUploadObservers()
    }

    private fun setupFunctions() {
        saveButtonCheck = {
            mainViewModel.performTimerEvent({
                updateSaveButton()
            }, 50L)
        }

        addButtonCheck = {array ->
            mainViewModel.performTimerEvent({
                binding.addButton.isEnabled = array.isEmpty()
                updateSaveButton()
            }, 50L)
        }

        checkIfDeletionIsPossible = {id ->
            val reference = mainViewModel.getReferenceByIndex(index!!)
            !(mainViewModel.checkIfParameterIsNecessary(reference, id) || scheduleViewModel.checkIfParameterIsNecessary(reference, id))
        }
    }

    private fun setupRecyclerView() {
        val currentRecyclerList = mainViewModel.getParametersByIndex(index!!)
        currentRecyclerList.sortBy { it.title }
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            parametersRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }

    private fun createPopupWindow() {
        updateSaveButton(false)
        val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)

        popupBinding = BasicPopupWindowBinding.inflate(layoutInflater)
        val popupView: View = popupBinding.root

        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        var text = APP_ADMIN_WARNING_SAVE_CHANGES
        if (scheduleViewModel.compareParametersLists(mainViewModel.getParametersByIndex(index!!), currentRecyclerList).second) {
            text = APP_ADMIN_WARNING_ID_DELETION + "\n" + text
        }

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupBinding.popupText.text = text
        popupBinding.yesButton.setOnClickListener {
            uploadRecyclerList = getItemArrayDeepCopy(currentRecyclerList)
            mainViewModel.uploadParameters(currentUploadStatus, index!!, uploadRecyclerList)
            popupWindow.dismiss()
        }
        popupBinding.noButton.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.showAtLocation(this.view, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
            updateSaveButton()
        }
    }

    private fun updateSaveButton(b: Boolean? = null) {
        if (b != null) {
            binding.saveButton.isEnabled = b
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
            return
        }

        val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
        val comparison = scheduleViewModel.compareParametersLists(mainViewModel.getParametersByIndex(index!!), currentRecyclerList)
        binding.saveButton.isEnabled = !comparison.first
    }

    private fun initUploadObservers() {
        currentUploadStatus = MutableLiveData()
        currentUploadStatus.observe(viewLifecycleOwner) { uploadStatus ->
            //(this.parentFragment as DataBaseFragmentContainer).updateViewPager()

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
                        updateSaveButton()
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
                        updateSaveButton()
                    }, 50L)
                }
            }
        }
    }

    private fun initDownloadObservers() {
        currentDownloadStatus = MutableLiveData()
        currentDownloadStatus.observe(viewLifecycleOwner) { downloadStatus ->
            /*when (downloadStatus) {
                is DownloadStatus.Progress -> {}
                is DownloadStatus.WeakProgress -> {}
                else -> (this.parentFragment as DataBaseFragmentContainer).updateViewPager()
            }*/

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
                        "Failed to download the Data: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<ArrayList<Data_IntString>> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    setupView()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }

    companion object {
        fun newInstance(position: Int): DataBaseEditFragment {
            val fragment = DataBaseEditFragment()
            val args = Bundle()
            args.putInt("index", position)
            fragment.arguments = args
            return fragment
        }
    }
}