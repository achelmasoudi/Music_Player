package com.achelm.musicplayer.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.adapters.MusicAdapter
import com.achelm.musicplayer.fragments.PlaylistFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.achelm.musicplayer.models.checkPlaylist
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar

class PlaylistDetailsActivity : AppCompatActivity() {

    companion object{
        var currentPlaylistPos: Int = -1
    }

    private lateinit var toolbar: Toolbar

    private lateinit var root: RelativeLayout
    private lateinit var adapter: MusicAdapter
    private lateinit var playlistDetailsRecyclerView: RecyclerView
    private lateinit var shuffleBtn: CardView
    private lateinit var playlistImage: ShapeableImageView
    private lateinit var totalSongs: TextView
    private lateinit var createdOn: TextView
    private lateinit var addBtn: CardView
    private lateinit var removeAllBtn: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_playlist_details)

        // Initialize variable
        root = findViewById(R.id.playlistDetailsActivity_relativeLayout_rootId)
        playlistDetailsRecyclerView = findViewById(R.id.playlistDetailsActivity_recyclerViewId)
        shuffleBtn = findViewById(R.id.playlistDetailsActivity_shuffleBtnId)
        playlistImage = findViewById(R.id.playlistDetailsActivity_playlistImageId)
        totalSongs = findViewById(R.id.playlistDetailsActivity_totalSongsId)
        createdOn = findViewById(R.id.playlistDetailsActivity_createdOnId)
        addBtn = findViewById(R.id.playlistDetailsActivity_addBtnId)
        removeAllBtn = findViewById(R.id.playlistDetailsActivity_removeAllBtnId)
        toolbar = findViewById(R.id.playlistDetailsActivity_toolBarId)

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        currentPlaylistPos = intent.extras?.get("index") as Int

        try{
            PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].playlist =
                checkPlaylist(playlist = PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].playlist)
        }
        catch(e: Exception){}

        playlistDetailsRecyclerView.setItemViewCacheSize(10)
        playlistDetailsRecyclerView.setHasFixedSize(true)
        playlistDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        playlistDetailsRecyclerView.adapter = adapter

        shuffleBtn.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }

        addBtn.setOnClickListener {
            startActivity(Intent(this, SelectionOfSongsActivity::class.java))
        }

        removeAllBtn.setOnClickListener {
            if (adapter.itemCount == 0) {
                // Show Snackbar message
                Snackbar.make(root, resources.getString(R.string.playlistDetailsActivity_Nosongsinplaylist), Snackbar.LENGTH_SHORT).show()
            }
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle(resources.getString(R.string.playlistDetailsActivity_remove))
                    .setMessage(resources.getString(R.string.playlistDetailsActivity_Doyouwanttoremoveallsongsfromplaylist))
                    .setPositiveButton(resources.getString(R.string.playlistDetailsActivity_yes)){ dialog, _ ->
                        PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                        adapter.refreshPlaylist()
                        dialog.dismiss()

                        // Items of adapter = 0
                        playlistImage.setImageDrawable(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex])
                        shuffleBtn.visibility = View.GONE
                        totalSongs.text = "${resources.getString(R.string.playlistDetailsActivity_totalSongs)} ${adapter.itemCount}"
                    }
                    .setNegativeButton(resources.getString(R.string.playlistDetailsActivity_no)){dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()

            }
        }

        // For Ads
        adView()

        // Set current theme
        updateTheme()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        toolbar.title = PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].name

        totalSongs.text = "${resources.getString(R.string.playlistDetailsActivity_totalSongs)} ${adapter.itemCount}"

        createdOn.text = "${PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].createdOn}"

        if(adapter.itemCount > 0) {
            Glide.with(this)
                .load(PlaylistFragment.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder( MainActivity.currentTheme_musicIcon[MainActivity.themeIndex] ).centerCrop())
                .into(playlistImage)
            shuffleBtn.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()

        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistFragment.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()

        updateTheme()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.playlistDetailsActivity_adViewId)

        MobileAds.initialize(this , OnInitializationCompleteListener {})

        val adView = AdView(this)
        adView.setAdSize(AdSize.FULL_BANNER)
        adView.adUnitId = AD_UNIT_ID

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        pAdView.loadAd(adRequest)
    }

    private fun updateTheme() {
        toolbar.setBackgroundColor( ContextCompat.getColor(this , MainActivity.currentTheme[MainActivity.themeIndex]) )

        shuffleBtn.setCardBackgroundColor( ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]) )

        // Default theme of music icon if ( adapter items = 0 )
        playlistImage.setImageDrawable(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex])
    }
}