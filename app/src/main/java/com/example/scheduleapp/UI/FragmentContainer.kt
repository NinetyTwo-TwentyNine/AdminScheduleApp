package com.example.scheduleapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.scheduleapp.R
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_CABINET_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_GROUP_LIST
import com.example.scheduleapp.data.Date
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.databinding.FragmentContainerBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentContainer : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var mainScreenAdapter: MainScreenAdapter
    private lateinit var binding: FragmentContainerBinding
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatScheduleDetailed>>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContainerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        viewModel.downloadSchedule(currentDownloadStatus)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewPager2() {
        val groupArray = viewModel.getParametersList(APP_BD_PATHS_GROUP_LIST)

        mainScreenAdapter = MainScreenAdapter(this, groupArray.size)
        binding.fragmentViewPager2.adapter = mainScreenAdapter
        binding.fragmentViewPager2.currentItem = 0

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager2) { tab, position ->
            tab.text = position.toString()
        }.attach()

        for (i in 0 until groupArray.size) {
            binding.tabLayout.getTabAt(i)?.text = groupArray[i]
        }
        binding.dayName.text = viewModel.getDayToTab(viewModel.getChosenDate())
    }

    private fun initObservers() {
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
                    setupViewPager2()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }
}