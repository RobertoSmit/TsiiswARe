// ViewPagerAdapter.kt
package com.example.tsiisware

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapterObjects(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreateObjectFragment()
            1 -> CreateObjectFragmentQuiz()
            2 -> DeleteObjectFragment()
            3 -> CreateQRCodeFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}