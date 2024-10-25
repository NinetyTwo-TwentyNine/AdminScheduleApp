package com.example.adminscheduleapp.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.adminscheduleapp.UI.DataBaseEditFragment

class AdminDBEditorAdapter(fragment: Fragment, private val paramsArray: List<String>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return paramsArray.size
    }

    override fun createFragment(position: Int): Fragment {
        return DataBaseEditFragment.newInstance(position)
    }
}