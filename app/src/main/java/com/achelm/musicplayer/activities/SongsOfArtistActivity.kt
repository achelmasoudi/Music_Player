package com.achelm.musicplayer.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.adapters.SongsOfArtistAdapter
import com.achelm.musicplayer.models.Music
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class SongsOfArtistActivity : AppCompatActivity() {


    companion object{
        lateinit var songListOfArtist: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC")
    }

    private lateinit var shuffleBtn: CardView
    private lateinit var playNextBtn: CardView
    private lateinit var totalSongs: TextView
    private lateinit var sortBtn: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongsOfArtistAdapter
    private lateinit var artistName: String
    private lateinit var toolbar: Toolbar


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_songs_of_artist)

        val bundle = intent.extras
        artistName = bundle?.getString("ARTIST_NAME") ?: ""

        // Initialize variables
        shuffleBtn = findViewById(R.id.songsOfArtistActivity_shuffleBtnId)
        playNextBtn = findViewById(R.id.songsOfArtistActivity_playNextBtnId)
        totalSongs = findViewById(R.id.songsOfArtistActivity_totalSongsId)
        sortBtn = findViewById(R.id.songsOfArtistActivity_sortBtnId)
        recyclerView =  findViewById(R.id.songsOfArtistActivity_recyclerViewOfSongsId)
        toolbar = findViewById(R.id.songsOfArtistActivity_toolBarId)

        toolbar.inflateMenu(R.menu.search_view_menu)
        toolbar.title = artistName

        // Get the search menu item from the toolbar
        val searchItem = toolbar.menu.findItem(R.id.searchViewId)
        // Retrieve the SearchView from the search menu item
        val searchView = searchItem.actionView as SearchView

        // For search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in songListOfArtist)
                        if(song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search = true
                    adapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.songsOfArtistActivity_recyclerViewOfSongsId)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Query songs based on the folder name
        songListOfArtist = getSongsOfArtist(artistName)

        initializeLayout(artistName)

        shuffleBtn.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "SongsOfArtistActivity")
            startActivity(intent)
        }

        playNextBtn.setOnClickListener {
            startActivity(Intent(this, PlayNextActivity::class.java))
        }

        sortBtn.setOnClickListener {
            val menuList = arrayOf(
                resources.getString(R.string.songsOfArtistActivity_Recently_Added),
                resources.getString(R.string.songsOfArtistActivity_Song_Title),
                resources.getString(R.string.songsOfArtistActivity_File_Size)
            )

            var currentSort = sortOrder
            val builder = MaterialAlertDialogBuilder(this)

            builder.setTitle(resources.getString(R.string.songsOfArtistActivity_Sorting))
                .setPositiveButton( resources.getString(R.string.songsOfArtistActivity_OK) ) { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()

                    // Update the music list based on the selected sorting option
                    sortOrder = currentSort
                    songListOfArtist = getSongsOfArtist(artistName)
                    adapter.updateMusicList(songListOfArtist)
                }
                .setSingleChoiceItems(menuList, currentSort) { _,which->
                    currentSort = which
                }
                .setNegativeButton(resources.getString(R.string.songsOfArtistActivity_Cancel)) { dialog,_ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()

        }

        // For Adds
        adView()

        // Set current theme
        updateTheme()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout(artistName: String){
        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)

        songListOfArtist = getSongsOfArtist(artistName)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongsOfArtistAdapter(this, songListOfArtist)
        recyclerView.adapter = adapter

        // Animation for RecyclerView
        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(this,R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerView.layoutAnimation = songListAnimFF

        totalSongs.text  = "${resources.getString(R.string.songsOfArtistActivity_totalSongs)} ${adapter.itemCount}"

    }

    @SuppressLint("Range")
    private fun getSongsOfArtist(artistName: String): ArrayList<Music> {
        val songListOfArtist = ArrayList<Music>()

        val selection = "${MediaStore.Audio.Media.ARTIST} = ? AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectionArgs = arrayOf(artistName)

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortingList[sortOrder],
            null
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: resources.getString(R.string.songsOfArtistActivity_Unknown)
                val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: resources.getString(R.string.songsOfArtistActivity_Unknown)
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: resources.getString(R.string.songsOfArtistActivity_Unknown)
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: resources.getString(R.string.songsOfArtistActivity_Unknown)
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) ?: resources.getString(R.string.songsOfArtistActivity_Unknown)
                val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUri = Uri.withAppendedPath(uri, albumId).toString()

                val music = Music(
                    id = id,
                    title = title,
                    album = album,
                    artist = artist,
                    path = path,
                    duration = duration,
                    artUri = artUri
                )

                val file = File(music.path)

                if (file.exists()) {
                    songListOfArtist.add(music)
                }
            }
        }

        return songListOfArtist
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        //for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)

        if(sortOrder != sortValue){
            sortOrder = sortValue
            songListOfArtist = getSongsOfArtist(artistName)
            adapter.updateMusicList(songListOfArtist)
        }

        updateTheme()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.songsOfArtistActivity_adViewId)

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
    }

}