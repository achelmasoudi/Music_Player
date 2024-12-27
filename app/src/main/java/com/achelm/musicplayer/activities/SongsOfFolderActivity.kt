package com.achelm.musicplayer.activities

import android.annotation.SuppressLint
import android.content.Context
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
import com.achelm.musicplayer.adapters.SongsOfFolderAdapter
import com.achelm.musicplayer.models.Music
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class SongsOfFolderActivity : AppCompatActivity() {


    companion object{
        lateinit var songListOfFolder: ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
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
    private lateinit var adapter: SongsOfFolderAdapter
    private lateinit var folderPath: String
    private lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_songs_of_folder)

        var bundle = intent.extras
        folderPath = bundle!!.getString("FOLDER_PATH")!!
        val foldername = bundle.getString("FOLDER_NAME")

        // Initialize variables
        shuffleBtn = findViewById(R.id.songsOfFolderActivity_shuffleBtnId)
        playNextBtn = findViewById(R.id.songsOfFolderActivity_playNextBtnId)
        totalSongs = findViewById(R.id.songsOfFolderActivity_totalSongsId)
        sortBtn = findViewById(R.id.songsOfFolderActivity_sortBtnId)
        recyclerView =  findViewById(R.id.songsOfFolderActivity_recyclerViewOfSongsId)
        toolbar = findViewById(R.id.songsOfFolderActivity_toolBarId)

        toolbar.inflateMenu(R.menu.search_view_menu)
        toolbar.title = foldername.toString()

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
                    for (song in songListOfFolder)
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

        // Query songs based on the folder name
        songListOfFolder = MusicUtility.getSongsInFolder(this, folderPath)

        initializeLayout(folderPath)

        shuffleBtn.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "SongsOfFolderActivity")
            startActivity(intent)
        }

        playNextBtn.setOnClickListener {
            startActivity(Intent(this, PlayNextActivity::class.java))
        }

        sortBtn.setOnClickListener {
            val menuList = arrayOf(
                resources.getString(R.string.songsOfFolderActivity_Recently_Added),
                resources.getString(R.string.songsOfFolderActivity_Song_Title),
                resources.getString(R.string.songsOfFolderActivity_File_Size)
            )

            var currentSort = sortOrder
            val builder = MaterialAlertDialogBuilder(this)

            builder.setTitle(resources.getString(R.string.songsOfFolderActivity_Sorting))
                .setPositiveButton( resources.getString(R.string.songsOfFolderActivity_OK )) { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()

                    // Update the music list based on the selected sorting option
                    sortOrder = currentSort
                    songListOfFolder = MusicUtility.getSongsInFolder(this , folderPath)
                    adapter.updateMusicList(songListOfFolder)
                }
                .setSingleChoiceItems(menuList, currentSort){ _,which->
                    currentSort = which
                }
                .setNegativeButton(resources.getString(R.string.songsOfFolderActivity_Cancel)) { dialog,_ ->
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
    private fun initializeLayout(folderPath: String){
        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)

        songListOfFolder = MusicUtility.getSongsInFolder(this , folderPath)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongsOfFolderAdapter(this, songListOfFolder)
        recyclerView.adapter = adapter

        // Animation for RecyclerView
        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(this,R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerView.layoutAnimation = songListAnimFF

        totalSongs.text  = "${resources.getString(R.string.songsOfFolderActivity_totalSongs)} ${adapter.itemCount}"

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        //for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)

        if(sortOrder != sortValue){
            sortOrder = sortValue
            songListOfFolder = MusicUtility.getSongsInFolder( this , folderPath)
            adapter.updateMusicList(songListOfFolder)
        }

        updateTheme()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.songsOfFolderActivity_adViewId)

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


object MusicUtility  {
    @SuppressLint("Range")
    fun getSongsInFolder(context: Context, folderPath: String): ArrayList<Music> {
        val songListOfFolder = ArrayList<Music>()

        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"

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

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            SongsOfFolderActivity.sortingList[SongsOfFolderActivity.sortOrder],
            null
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: context.resources.getString(R.string.songsOfFolderActivity_Unknown)
                val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: context.resources.getString(R.string.songsOfFolderActivity_Unknown)
                val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: context.resources.getString(R.string.songsOfFolderActivity_Unknown)
                val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: context.resources.getString(R.string.songsOfFolderActivity_Unknown)
                val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) ?: context.resources.getString(R.string.songsOfFolderActivity_Unknown)
                val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()

                // Check if the song path belongs to the specified folder
                if (pathC.startsWith(folderPath)) {
                    // To get song image path

                    val music = Music (
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )

                    val file = File(music.path)

                    if(file.exists())
                        songListOfFolder.add(music)
                }
            }
        }

        return songListOfFolder
    }

    @SuppressLint("Range")
    fun getMostRepeatedImageInFolder(context: Context, folderPath: String): String {
        val imageFrequencyMap = mutableMapOf<String, Int>()

        // Query songs based on the folder path
        val selection = "${MediaStore.Audio.Media.DATA} LIKE '$folderPath%' AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)) ?: continue
                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUri = Uri.withAppendedPath(uri, albumId).toString()

                if (imageFrequencyMap.containsKey(artUri)) {
                    val count = imageFrequencyMap[artUri] ?: 0
                    imageFrequencyMap[artUri] = count + 1
                } else {
                    imageFrequencyMap[artUri] = 1
                }
            }
        }

        // Find the image URI with the highest frequency
        var mostRepeatedImageUri = ""
        var maxFrequency = 0
        for ((uri, frequency) in imageFrequencyMap) {
            if (frequency > maxFrequency) {
                maxFrequency = frequency
                mostRepeatedImageUri = uri
            }
        }

        // Find the second most repeated image URI
        var secondMostRepeatedImageUri = ""
        var secondMaxFrequency = 0
        for ((uri, frequency) in imageFrequencyMap) {
            if (frequency > secondMaxFrequency && uri != mostRepeatedImageUri) {
                secondMaxFrequency = frequency
                secondMostRepeatedImageUri = uri
            }
        }

        return secondMostRepeatedImageUri
    }
}
