// ViewPagerAdapter.kt
package com.example.tsiisware

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapterUsers(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateUserFragment()
            1 -> DeleteUserFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}