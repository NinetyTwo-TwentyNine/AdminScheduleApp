package com.example.scheduleapp.UI

import android.R
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.scheduleapp.data.Constants.APP_ADMIN_ID_DELETION_WARNING
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.scheduleapp.data.Constants.APP_ADMIN_SAVE_CHANGES_WARNING
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.data.UploadStatus
import com.example.scheduleapp.databinding.BasicPopupWindowBinding
import com.example.scheduleapp.databinding.FragmentDataBaseEditBinding
import com.example.scheduleapp.utils.Utils
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel


class DataBaseEditFragment: Fragment() {
    private var index: Int? = null
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter
    private lateinit var binding: FragmentDataBaseEditBinding
    private lateinit var popupBinding: BasicPopupWindowBinding
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()

    private lateinit var buttonCheck: (ArrayList<Int>)->Unit
    private lateinit var deleteOneView: (Int)->Unit
    private lateinit var editOneView: (Int, String)->Unit

    private lateinit var currentRecyclerList: ArrayList<Data_IntString>
    private lateinit var uploadRecyclerList: ArrayList<Data_IntString>


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
            mainViewModel.downloadParametersList(mainViewModel.getReferenceByIndex(index!!))
        }
    }

    private fun setupView() {
        binding.addButton.setOnClickListener {
            val currentId = scheduleViewModel.getPossibleId(currentRecyclerList)
            currentRecyclerList.add(Data_IntString(currentId, ""))
            setupRecyclerView()
        }

        currentRecyclerList = mainViewModel.getParametersByIndex(index!!)
        binding.saveButton.setOnClickListener {
            createPopupWindow()
            updateSaveButton(false)
        }

        setupFunctions()
        dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(buttonCheck, deleteOneView, editOneView)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            parametersRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }

    private fun setupFunctions() {
        buttonCheck = {array ->
            binding.addButton.isEnabled = array.isEmpty()
            updateSaveButton()
        }
        deleteOneView = {id ->
            for (e: Data_IntString in currentRecyclerList) {
                if (e.id == id)  {
                    currentRecyclerList.remove(e)
                    break
                }
            }
            setupRecyclerView()
            updateSaveButton()
        }
        editOneView = {id, title ->
            for (e: Data_IntString in currentRecyclerList) {
                if (e.id == id)  {
                    e.title = title
                    break
                }
            }
            //setupRecyclerView()
            updateSaveButton()
        }
    }

    private fun createPopupWindow() {
        popupBinding = BasicPopupWindowBinding.inflate(layoutInflater)
        val popupView: View = popupBinding.root

        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        var text = APP_ADMIN_SAVE_CHANGES_WARNING
        if (scheduleViewModel.compareParametersLists(mainViewModel.getParametersByIndex(index!!), currentRecyclerList).second) {
            text = APP_ADMIN_ID_DELETION_WARNING + "\n" + text
        }

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupBinding.popupText.text = text
        popupBinding.yesButton.setOnClickListener {
            popupWindow.dismiss()
            updateSaveButton(false)
            initUploadObservers()
            uploadRecyclerList = Utils.getDataIntStringArrayDeepCopy(currentRecyclerList)
            mainViewModel.uploadParameters(index!!, uploadRecyclerList)
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

        val unacceptableUploadState = when (mainViewModel.uploadState.value) {
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

        val comparison = scheduleViewModel.compareParametersLists(mainViewModel.getParametersByIndex(index!!), currentRecyclerList)
        binding.saveButton.isEnabled = !comparison.first
    }

    private fun updateViewPager(b: Boolean? = null) {
        if (b != null) {
            (this.parentFragment as DataBaseFragmentContainer).setViewPagerEnabled(b)
            return
        }

        (this.parentFragment as DataBaseFragmentContainer).setViewPagerEnabled(when(mainViewModel.uploadState.value) {
            is UploadStatus.Progress -> false
            is UploadStatus.WeakProgress -> false
            else -> when(mainViewModel.parametersDownloadState.value) {
                is DownloadStatus.Progress -> false
                is DownloadStatus.WeakProgress -> false
                else -> true
            }
        })
    }

    private fun initUploadObservers() {
        mainViewModel.resetUploadState()
        mainViewModel.uploadState.observe(viewLifecycleOwner) { uploadStatus ->

            updateViewPager()
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
                    updateSaveButton()
                }
                is UploadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    mainViewModel.updateParametersByIndex(index!!, uploadRecyclerList)
                    Toast.makeText(
                        activity,
                        "Succeeded in uploading the Data.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateSaveButton()
                }
            }
        }
    }

    private fun initDownloadObservers() {
        mainViewModel.resetDownloadState(true)
        mainViewModel.parametersDownloadState.observe(viewLifecycleOwner) { downloadStatus ->

            updateViewPager()
            when (downloadStatus) {
                is DownloadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                    updateViewPager(true)
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
                    Toast.makeText(
                        activity,
                        "Failed to download Schedule: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<ArrayList<Data_IntString>> -> {
                    binding.progressBar.visibility = View.GONE
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