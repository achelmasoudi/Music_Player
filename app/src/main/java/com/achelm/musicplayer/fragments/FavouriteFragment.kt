package com.achelm.musicplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.activities.PlayerActivity
import com.achelm.musicplayer.activities.SearchActivity
import com.achelm.musicplayer.activities.SettingsActivity
import com.achelm.musicplayer.adapters.FavouriteAdapter
import com.achelm.musicplayer.models.Music
import com.achelm.musicplayer.models.checkPlaylist
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FavouriteFragment : Fragment() {

    companion object{
        var favouriteSongs: ArrayList<Music> = ArrayList()
        var favouritesChanged: Boolean = false
    }

    private lateinit var fView: View
    private lateinit var adapter: FavouriteAdapter
    private lateinit var favouriteRecyclerView: RecyclerView
    private lateinit var shuffleBtn: CardView
    private lateinit var instruction: LinearLayout
    private lateinit var toolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        fView = inflater.inflate(R.layout.fragment_favourite , container , false)

        // Initialize variables
        favouriteRecyclerView = fView.findViewById(R.id.favouriteFragment_recyclerViewId)
        shuffleBtn = fView.findViewById(R.id.favouriteFragment_shuffleBtnId)
        instruction = fView.findViewById(R.id.favouriteFragment_instructionId)
        toolbar = fView.findViewById(R.id.favouriteFragment_toolBarId)

        // Set search in Toolbar
        toolbar.inflateMenu(R.menu.menu_off)

        // Handle menu item clicks
        itemsOfToolbar()

        favouriteSongs = checkPlaylist(favouriteSongs)

        favouriteRecyclerView.setHasFixedSize(true)
        favouriteRecyclerView.setItemViewCacheSize(13)
        favouriteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavouriteAdapter(requireContext(), favouriteSongs)
        favouriteRecyclerView.adapter = adapter

        favouritesChanged = false

        shuffleBtn.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteShuffle")
            startActivity(intent)
        }

        updateInstructionVisibility()

        // Set current theme
        updateTheme()

        return fView
    }

    private fun updateInstructionVisibility() {
        if (favouriteSongs.isEmpty()) {
            shuffleBtn.visibility = View.GONE
            instruction.visibility = View.VISIBLE
        }
        else {
            instruction.visibility = View.GONE
            if (favouriteSongs.size > 1) {
                shuffleBtn.visibility = View.VISIBLE
            }
            else {
                shuffleBtn.visibility = View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if(favouritesChanged) {
            adapter.updateFavourites(favouriteSongs)
            favouritesChanged = false
            updateInstructionVisibility()
        }

        adapter.notifyDataSetChanged()
        updateTheme()

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

    private fun updateTheme() {
        toolbar.setBackgroundColor( ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )
        shuffleBtn.setCardBackgroundColor( ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )
    }
}