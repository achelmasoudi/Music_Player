package com.achelm.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.ApplicationClass
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.adapters.MusicAdapter
import com.achelm.musicplayer.fragments.SongsFragment_ofMusicFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.snackbar.Snackbar

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: MusicAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var arrowBack: CardView
    private lateinit var searchOnYoutubeBtn: CardView
    private lateinit var root: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_search)

        // Initialize variables
        recyclerView = findViewById(R.id.searchActivity_recyclerViewId)
        searchView = findViewById(R.id.searchActivity_searchViewId)
        arrowBack = findViewById(R.id.searchActivity_arrowBackId)
        searchOnYoutubeBtn = findViewById(R.id.searchActivity_searchOnYoutube_BtnId)
        root = findViewById(R.id.searchActivity_rootId)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, SongsFragment_ofMusicFragment.MusicListMA, searchActivity = true)
        recyclerView.adapter = adapter

        arrowBack.setOnClickListener { finish() }

        // Focus on the search view when the activity starts
        searchView.requestFocus()

        // Hide RecyclerView initially
        recyclerView.isVisible = false

        //for search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {

                recyclerView.isVisible = newText!!.isNotBlank()

                SongsFragment_ofMusicFragment.musicListSearch = ArrayList()
                    val userInput = newText.lowercase()
                    for (song in SongsFragment_ofMusicFragment.MusicListMA)
                        if(song.title.lowercase().contains(userInput))
                            SongsFragment_ofMusicFragment.musicListSearch.add(song)
                    SongsFragment_ofMusicFragment.search = true
                    adapter.updateMusicList(searchList = SongsFragment_ofMusicFragment.musicListSearch)

                return true
            }
        })

        // Set click listener for YouTube search button
        searchOnYoutubeBtn.setOnClickListener {
            if (searchView.query.toString().isNotBlank()) {
                // Perform search action
                val query = searchView.query.toString()
                val intent = Intent(Intent.ACTION_SEARCH)
                intent.setPackage("com.google.android.youtube")
                intent.putExtra("query", query)
                startActivity(intent)
            }
            else {
                // Show toast if SearchView is empty
                Snackbar.make( this , root, resources.getString(R.string.searchActivity_EnterasongtosearchonYouTube), Snackbar.LENGTH_SHORT).show()
            }
        }

        // For Adds
        adView()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.searchActivity_adViewId)

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