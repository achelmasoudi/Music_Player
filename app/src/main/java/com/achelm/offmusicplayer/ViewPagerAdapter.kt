package com.achelm.offmusicplayer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.achelm.offmusicplayer.fragments.ArtistsFragment_ofMusicFragment
import com.achelm.offmusicplayer.fragments.FoldersFragment_ofMusicFragment
import com.achelm.offmusicplayer.fragments.SongsFragment_ofMusicFragment

class ViewPagerAdapter(var fragmentManager: FragmentManager, var lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager , lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> FoldersFragment_ofMusicFragment() // The second TabItem
            2 -> ArtistsFragment_ofMusicFragment() // The third TabItem
            else -> SongsFragment_ofMusicFragment() // The first TabItem
        }
    }
}