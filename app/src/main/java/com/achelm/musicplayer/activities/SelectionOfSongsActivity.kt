package com.achelm.musicplayer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.adapters.MusicAdapter
import com.achelm.musicplayer.R
import com.achelm.musicplayer.fragments.SongsFragment_ofMusicFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class SelectionOfSongsActivity : AppCompatActivity() {

    private lateinit var adapter: MusicAdapter
    private lateinit var selectionRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var arrowBack: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_selection_of_songs)

        // Initialize variables
        selectionRecyclerView = findViewById(R.id.selectionActivity_recyclerViewId)
        searchView = findViewById(R.id.selectionActivity_searchViewId)
        arrowBack = findViewById(R.id.selectionActivity_arrowBackId)

        selectionRecyclerView.setItemViewCacheSize(30)
        selectionRecyclerView.setHasFixedSize(true)
        selectionRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, SongsFragment_ofMusicFragment.MusicListMA, selectionActivity = true)
        selectionRecyclerView.adapter = adapter

        arrowBack.setOnClickListener { finish() }

        // Focus on the search view when the activity starts
        searchView.requestFocus()

        //for search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                SongsFragment_ofMusicFragment.musicListSearch = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in SongsFragment_ofMusicFragment.MusicListMA)
                        if(song.title.lowercase().contains(userInput))
                            SongsFragment_ofMusicFragment.musicListSearch.add(song)
                    SongsFragment_ofMusicFragment.search = true
                    adapter.updateMusicList(searchList = SongsFragment_ofMusicFragment.musicListSearch)
                }
                return true
            }
        })

        // For Adds
        adView()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.selectionActivity_adViewId)

        MobileAds.initialize(this , OnInitializationCompleteListener {})

        val adView = AdView(this)
        adView.setAdSize(AdSize.FULL_BANNER)
        adView.adUnitId = AD_UNIT_ID

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        pAdView.loadAd(adRequest)
    }

}