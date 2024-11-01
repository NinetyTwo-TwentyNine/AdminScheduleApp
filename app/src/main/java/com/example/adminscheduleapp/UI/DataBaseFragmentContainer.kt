package com.example.adminscheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.adminscheduleapp.adapters.AdminDBEditorAdapter
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.adminscheduleapp.databinding.FragmentDataBaseContainerBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
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
}