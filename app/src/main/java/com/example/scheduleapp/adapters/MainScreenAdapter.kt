package com.example.scheduleapp.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.scheduleapp.UI.ScheduleFragment

class MainScreenAdapter(fragment: Fragment, private val itemCount: Int) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun createFragment(position: Int): Fragment {
        return ScheduleFragment.newInstance(position)
    }

    companion object {
        const val PAGE_COUNT = 15
    }
}