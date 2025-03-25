package com.achelm.offmusicplayer.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.adapters.ArtistAdapter
import com.achelm.offmusicplayer.models.Artist

class ArtistsFragment_ofMusicFragment : Fragment() {

    private lateinit var view: View
    private lateinit var recyclerViewOfArtists: RecyclerView
    private lateinit var adapter: ArtistAdapter
    private val artistList: ArrayList<Artist> = ArrayList()
    private lateinit var noArtistsFoundId: LinearLayout
    private lateinit var totalArtists: TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        view = inflater.inflate(R.layout.fragment_artists_of_fragment_music, container, false)

        recyclerViewOfArtists = view.findViewById(R.id.artistsFragmentOfMusicFrag_RecyclerViewId)
        noArtistsFoundId = view.findViewById(R.id.artistsFragmentOfMusicFrag_NoFoldersFoundId)

        totalArtists = view.findViewById(R.id.artistsFragmentOfMusicFrag_totalArtistsId)

        // Retrieve artists and populate artistList
        retrieveMusicArtists()

        recyclerViewOfArtists.setHasFixedSize(true)
        recyclerViewOfArtists.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArtistAdapter(requireActivity(), artistList)
        recyclerViewOfArtists.adapter = adapter

        // Animation for RecyclerView
        val artistListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_anim))
        artistListAnimFF.delay = 0.2f
        artistListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerViewOfArtists.layoutAnimation = artistListAnimFF

        totalArtists.text = "${resources.getString(R.string.artistsFragmentOfMusicFragment_totalArtists)} ${adapter.itemCount}"

        // Set the visibility of noArtistsFoundId based on whether artistList is empty
        if (artistList.isEmpty()) {
            totalArtists.visibility = View.GONE
            noArtistsFoundId.visibility = View.VISIBLE
            recyclerViewOfArtists.visibility = View.GONE
        } else {
            totalArtists.visibility = View.VISIBLE
            noArtistsFoundId.visibility = View.GONE
            recyclerViewOfArtists.visibility = View.VISIBLE
        }

        return view
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun retrieveMusicArtists() {
        val projection = arrayOf(MediaStore.Audio.Media.ARTIST)
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val cursor = requireContext().contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)

        val artistMap = HashMap<String, Int>()
        cursor?.use {
            while (it.moveToNext()) {
                val artist = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                // Count the number of songs for each artist
                artistMap[artist] = artistMap.getOrDefault(artist, 0) + 1
            }
        }

        artistMap.forEach { (artistName, songCount) ->
            artistList.add(Artist(artistName, songCount.toString()))
        }
    }

    override fun onResume() {
        super.onResume()
        // Notify the adapter that the dataset has changed
        adapter.notifyDataSetChanged()
    }
}