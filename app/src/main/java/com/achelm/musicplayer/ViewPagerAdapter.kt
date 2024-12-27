package com.achelm.musicplayer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.achelm.musicplayer.fragments.ArtistsFragment_ofMusicFragment
import com.achelm.musicplayer.fragments.FoldersFragment_ofMusicFragment
import com.achelm.musicplayer.fragments.SongsFragment_ofMusicFragment

class ViewPagerAdapter(var fragmentManager: FragmentManager, var lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager , lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        if(position == 1) {
            //the second TabItem
            return FoldersFragment_ofMusicFragment()
        }

        if(position == 2) {
            //the second TabItem
            return ArtistsFragment_ofMusicFragment()
        }

        //the first TabItem
        return SongsFragment_ofMusicFragment()
    }
}