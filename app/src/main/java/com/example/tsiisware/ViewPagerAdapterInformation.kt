package com.example.tsiisware

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapterInformation(
    fragmentActivity: FragmentActivity,
    private val label: String,
    private val category: String,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InformationViewPastFragment.newInstance(label, category)
            1 -> InformationViewCurrentFragment.newInstance(label, category)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}