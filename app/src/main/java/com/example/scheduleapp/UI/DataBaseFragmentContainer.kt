package com.example.scheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.PagerAdapter
import com.example.scheduleapp.adapters.AdminDBEditorAdapter
import com.example.scheduleapp.adapters.MainScreenAdapter
import com.example.scheduleapp.adapters.MainScreenAdapter.Companion.PAGE_COUNT
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.data.FlatScheduleDetailed
import com.example.scheduleapp.data.UploadStatus
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

        setupViewPager2()
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

    fun setViewPagerEnabled(b: Boolean) {
        binding.fragmentViewPager2.isUserInputEnabled = b
    }

    /*
    private fun initObservers() {
        binding.fragmentViewPager2.isUserInputEnabled = false

        viewModel.parametersDownloadState.observe(viewLifecycleOwner) { downloadStatus ->
            Log.d("VIEW_PAGER_CHECKER", "")
            Log.d("VIEW_PAGER_CHECKER", binding.fragmentViewPager2.isUserInputEnabled.toString())
            binding.fragmentViewPager2.isUserInputEnabled = when (downloadStatus) {
                is DownloadStatus.Progress -> false
                is DownloadStatus.WeakProgress -> false
                else -> when(viewModel.uploadState.value) {
                    is UploadStatus.Progress -> false
                    is UploadStatus.WeakProgress -> false
                    else -> true
                }
            }
        }
        viewModel.uploadState.observe(viewLifecycleOwner) { uploadStatus ->
            Log.d("VIEW_PAGER_CHECKER", "")
            Log.d("VIEW_PAGER_CHECKER", binding.fragmentViewPager2.isUserInputEnabled.toString())
            binding.fragmentViewPager2.isUserInputEnabled = when (uploadStatus) {
                is UploadStatus.Progress -> false
                is UploadStatus.WeakProgress -> false
                else -> when(viewModel.parametersDownloadState.value) {
                    is DownloadStatus.Progress -> false
                    is DownloadStatus.WeakProgress -> false
                    else -> true
                }
            }
        }
    }
    */
}