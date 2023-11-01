package com.example.scheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scheduleapp.adapters.AdminDBEditorAdapter
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.databinding.FragmentDataBaseContainerBinding
import com.example.scheduleapp.databinding.FragmentDataBaseEditBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.google.android.material.tabs.TabLayoutMediator

class DataBaseFragmentContainer : Fragment() {
    private lateinit var binding: FragmentDataBaseContainerBinding
    private lateinit var dbEditorAdapter: AdminDBEditorAdapter
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataBaseContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        viewModel.downloadSchedule()
    }

    private fun setupViewPager2() {
        dbEditorAdapter = AdminDBEditorAdapter(this, APP_ADMIN_PARAMETERS_LIST)
        binding.fragmentViewPager2.adapter = dbEditorAdapter
        binding.fragmentViewPager2.currentItem = 0

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager2) { tab, position ->
            tab.text = position.toString()
        }.attach()

        for (i in APP_ADMIN_PARAMETERS_LIST.indices) {
            binding.tabLayout.getTabAt(i)?.text = APP_ADMIN_PARAMETERS_LIST[i]
        }
    }

    private fun initObservers() {
        viewModel.resetDownloadState(false)
        viewModel.scheduleDownloadState.observe(viewLifecycleOwner) { downloadStatus ->

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
                    Toast.makeText(
                        activity,
                        "Failed to download Schedule: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<FlatScheduleDetailed> -> {
                    binding.progressBar.visibility = View.GONE
                    setupViewPager2()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }
}