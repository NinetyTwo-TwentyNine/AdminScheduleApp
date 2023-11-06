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
import androidx.viewpager2.widget.ViewPager2
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

        binding.fragmentViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateViewPager()
            }
        })
    }

    fun setViewPagerEnabled(b: Boolean) {
        binding.fragmentViewPager2.isUserInputEnabled = b
    }

    fun updateViewPager(b: Boolean? = null) {
        if (b != null) {
            setViewPagerEnabled(b)
            return
        }

        setViewPagerEnabled(when(viewModel.uploadState.value) {
            is UploadStatus.Progress -> false
            is UploadStatus.WeakProgress -> false
            else -> when(viewModel.parametersDownloadState.value) {
                is DownloadStatus.Progress -> false
                is DownloadStatus.WeakProgress -> false
                else -> true
            }
        })
    }
}