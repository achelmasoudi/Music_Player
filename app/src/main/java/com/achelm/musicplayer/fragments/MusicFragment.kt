package com.achelm.musicplayer.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.ViewPagerAdapter
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.activities.SearchActivity
import com.achelm.musicplayer.activities.SettingsActivity
import com.google.android.material.tabs.TabLayout

class MusicFragment : Fragment() {

    private lateinit var fView: View

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        fView = inflater.inflate(R.layout.fragment_music , container , false)

        // Initialize variables
        tabLayout = fView.findViewById(R.id.musicFragment_tablayoutId)
        viewPager2 = fView.findViewById(R.id.musicFragment_viewPagerId)
        toolbar = fView.findViewById(R.id.musicFragment_toolBarId)

        // Set search in Toolbar
        toolbar.inflateMenu(R.menu.menu_off)

        // Handle menu item clicks
        itemsOfToolbar()

        setTabLayoutWithViewPager()

        // Set current theme
        updateTheme()

        return fView
    }

    private fun setTabLayoutWithViewPager() {
        var fragmentManager: FragmentManager = activity?.supportFragmentManager!!
        viewPagerAdapter = ViewPagerAdapter(fragmentManager , activity?.lifecycle!!)

        viewPager2.adapter = viewPagerAdapter

        tabLayout.addTab(tabLayout.newTab().setText( resources.getString(R.string.musicFragment_Songs) ))
        tabLayout.addTab(tabLayout.newTab().setText( resources.getString(R.string.musicFragment_Folders) ))
        tabLayout.addTab(tabLayout.newTab().setText( resources.getString(R.string.musicFragment_Artists) ))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager2.currentItem = tab!!.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }

    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.searchViewId -> {
                    // Handle search item click
                    var intent = Intent(requireContext() , SearchActivity::class.java)
                    requireContext().startActivity(intent)
                    true
                }
                R.id.settingsId -> {
                    var intent = Intent(requireContext() , SettingsActivity::class.java)
                    requireContext().startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateTheme()
    }

    private fun updateTheme() {
        toolbar.background = ColorDrawable(ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]))
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]))
        tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.dark_secondColorOfApp), // Unselected tab text color
            ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex])) // Selected tab text color
    }
}