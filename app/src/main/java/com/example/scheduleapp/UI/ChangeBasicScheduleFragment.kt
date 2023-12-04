package com.example.scheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleBase
import com.example.scheduleapp.databinding.FragmentChangeBasicScheduleBinding
import com.example.scheduleapp.utils.Utils.getPossibleId
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel

class ChangeBasicScheduleFragment() : Fragment() {
    private lateinit var binding: FragmentChangeBasicScheduleBinding
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatScheduleBase>>

    private lateinit var addButtonCheck: (ArrayList<Int>)->Unit
    private lateinit var saveButtonCheck: ()->Unit


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeBasicScheduleBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (scheduleViewModel.getSavedBaseSchedule() == null) {
            initDownloadObservers()
            mainViewModel.downloadBaseSchedule(currentDownloadStatus)
        } else {
            setupView()
        }
    }

    private fun setupFunctions() {
        saveButtonCheck = {
            mainViewModel.performTimerEvent({
                //updateSaveButton()
            }, 50L)
        }

        addButtonCheck = {array ->
            mainViewModel.performTimerEvent({
                binding.addButton.isEnabled = array.isEmpty()
                //updateSaveButton()
            }, 50L)
        }
    }

    private fun setupRecyclerView() {
        setupFunctions()
        dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(addButtonCheck, saveButtonCheck, 1)

        val currentRecyclerList: ArrayList<Data_IntString> = arrayListOf()
        for (i in 0 until scheduleViewModel.getSavedBaseSchedule()!!.scheduleName.size) {
            currentRecyclerList.add(Data_IntString(i, scheduleViewModel.getSavedBaseSchedule()!!.scheduleName[i].title))
        }
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            changeBasicSchedule.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }

    private fun setupView() {
        binding.addButton.isEnabled = true
        binding.addButton.setOnClickListener {
            val currentRecyclerList = ArrayList(dbEditorRecyclerViewAdapter.differ.currentList)
            val new_id = getPossibleId(currentRecyclerList)
            currentRecyclerList.add(0, Data_IntString(new_id, "Базовое расписание $new_id"))

            //dbEditorRecyclerViewAdapter.notifyItemChanged(0)
            dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)

            mainViewModel.performTimerEvent(
                {binding.changeBasicSchedule.scrollToPosition(0)},
                50L)
        }

        setupRecyclerView()
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
                is DownloadStatus.Success<FlatScheduleBase> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    scheduleViewModel.saveBaseSchedule(mainViewModel.getBaseSchedule())
                    setupView()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }
}