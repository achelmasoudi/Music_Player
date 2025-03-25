package com.achelm.offmusicplayer.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.offmusicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.offmusicplayer.LanguageManager
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.adapters.FavouriteAdapter
import com.achelm.offmusicplayer.models.Music
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class PlayNextActivity : AppCompatActivity() {

    companion object{
        var playNextList: ArrayList<Music> = ArrayList()
    }

    private lateinit var playNextRecyclerView: RecyclerView
    private lateinit var instructionTextView: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_play_next)

        // Initialize variables
        playNextRecyclerView = findViewById(R.id.playNextActivity_recyclerViewId)
        instructionTextView = findViewById(R.id.playNextActivity_instructionId)
        toolbar = findViewById(R.id.playNextActivity_toolBarId)

        playNextRecyclerView.setHasFixedSize(true)
        playNextRecyclerView.layoutManager = LinearLayoutManager(this)
        playNextRecyclerView.adapter = FavouriteAdapter(this, playNextList, playNext = true)

        if(playNextList.isNotEmpty())
            instructionTextView.visibility = View.GONE

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // For Adds
        adView()

        // Set current theme
        updateTheme()

    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.playNextActivity_adViewId)

        MobileAds.initialize(this , OnInitializationCompleteListener {})

        val adView = AdView(this)
        adView.setAdSize(AdSize.FULL_BANNER)
        adView.adUnitId = AD_UNIT_ID

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        pAdView.loadAd(adRequest)
    }


    override fun onResume() {
        super.onResume()
        updateTheme()
    }

    private fun updateTheme() {
        toolbar.setBackgroundColor( ContextCompat.getColor(this , MainActivity.currentTheme[MainActivity.themeIndex]) )
    }
}