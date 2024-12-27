package com.achelm.musicplayer.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.fragments.FavouriteFragment
import com.achelm.musicplayer.fragments.MusicFragment
import com.achelm.musicplayer.fragments.PlaylistFragment
import com.achelm.musicplayer.models.Music
import com.achelm.musicplayer.models.MusicPlaylist
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigView: BottomNavigationView
    private lateinit var nowPlayingFragmentContainer: FragmentContainerView

    companion object {
        // PERMISSION request constant
        var themeIndex: Int = 0
        val currentTheme_activity = arrayOf( R.style.Base_Theme_MusicPlayer , R.style.greenTheme, R.style.blueTheme , R.style.purpleTheme , R.style.red2Theme , R.style.copperTheme , R.style.orangeTheme , R.style.goldTheme , R.style.pinkTheme)
        val currentTheme = arrayOf( R.color.redColor_ofApp , R.color.greenColor_ofApp, R.color.blueColor_ofApp , R.color.purpleColor_ofApp , R.color.red2Color_ofApp , R.color.copperColor_ofApp , R.color.orangeColor_ofApp , R.color.goldColor_ofApp , R.color.pinkColor_ofApp)
        val currentTheme_light = arrayOf( R.color.redLightColor_ofApp , R.color.greenLightColor_ofApp, R.color.blueLightColor_ofApp  , R.color.purpleLightColor_ofApp , R.color.red2LightColor_ofApp , R.color.copperLightColor_ofApp , R.color.orangeLightColor_ofApp , R.color.goldLightColor_ofApp , R.color.pinkLightColor_ofApp)
        val currentTheme_playerActivity = arrayOf( R.color.color_of_playerActivity , R.color.greenColor_of_playerActivity, R.color.blueColor_of_playerActivity , R.color.purpleColor_of_playerActivity , R.color.red2Color_of_playerActivity , R.color.copperColor_of_playerActivity , R.color.orangeColor_of_playerActivity , R.color.goldColor_of_playerActivity , R.color.pinkColor_of_playerActivity)
        val currentTheme_backGIcon = arrayOf( R.color.color_backgroundIcon , R.color.greenColor_backgroundIcon, R.color.blueColor_backgroundIcon , R.color.purpleColor_backgroundIcon  , R.color.red2Color_backgroundIcon , R.color.copperColor_backgroundIcon , R.color.orangeColor_backgroundIcon , R.color.goldColor_backgroundIcon , R.color.pinkColor_backgroundIcon)
        val currentTheme_musicIcon = mutableListOf<Drawable?>()
        val currentTheme_artistIcon = mutableListOf<Drawable?>()
        val currentTheme_favouriteIcon = mutableListOf<Drawable?>()

        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language
        LanguageManager.loadLocale(this)

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)

        setTheme(currentTheme_activity[themeIndex])

        updateIcon_ofMusic()

        setContentView(R.layout.activity_main)

        bottomNavigView = findViewById(R.id.MainActivity_BottomNavigationViewId)
        nowPlayingFragmentContainer = findViewById(R.id.mainActivity_nowPlayingFragmentId)

        // Request the Storage Permission
        if (checkPermission()) {
            // Permission Already Granted
            retrieveAndDisplaySongs()
            loadFavouriteSongs()
        }
        else {
            // Permission was not Granted
            requestPermission()
        }

        //Default Fragment is (HOME fragment)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_containerId, MusicFragment()).addToBackStack(null).commit()

        bottomNavigView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when(item.itemId) {
                R.id.BottomNav_MusicId -> selectedFragment = MusicFragment()
                R.id.BottomNav_FavoriteId -> selectedFragment = FavouriteFragment()
                R.id.BottomNav_LibraryId -> selectedFragment = PlaylistFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_containerId, selectedFragment!!).addToBackStack(null).commit()
            updateBottomNavigationItemColors()
            true
        }

        if(PlayerActivity.musicService != null) nowPlayingFragmentContainer.visibility = View.VISIBLE

        // For Adds
        adView()

        // Set current theme
        updateTheme()
        updateBottomNavigationItemColors()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or Above
            try {
                Log.d(TAG , "RequestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package" , this.packageName , null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception) {
                Log.e(TAG, "RequestPermission: " , e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else {
            // Android is below 11 (R)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)

        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG , "StorageActivityResultLauncher: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or Above
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG , "StorageActivityResultLauncher: ")
                retrieveAndDisplaySongs()
                loadFavouriteSongs()
            }
            else {
                Log.d(TAG , "StorageActivityResultLauncher: ")
                Toast.makeText(this, "Manage external storage permission is denied...", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            // Android is below 11 (R)
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or Above
            Environment.isExternalStorageManager()
        }
        else {
            // Android is below 11 (R)
            val write = ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this , Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {

                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (write && read) {
                    // External Storage Permission Granted
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    // Retrieve and display songs after permission is granted
                    retrieveAndDisplaySongs()
                    loadFavouriteSongs()
                }
                else {
                    // External Storage Permission Denied
                    Toast.makeText(this, "Manage external storage permission is denied...", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun retrieveAndDisplaySongs() {
        // Add HomeFragment when permission is granted
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_containerId, MusicFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadFavouriteSongs() {
        //for retrieving favourites data using shared preferences
        FavouriteFragment.favouriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
        val jsonString = editor.getString("FavouriteSongs", null)
        val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
        if(jsonString != null){
            val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavouriteFragment.favouriteSongs.addAll(data)
        }
        PlaylistFragment.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        if(jsonStringPlaylist != null) {
            val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
            PlaylistFragment.musicPlaylist = dataPlaylist
        }
    }

    private fun updateTheme() {
        window.statusBarColor = ContextCompat.getColor( this ,  currentTheme[themeIndex] )
    }

    private fun updateBottomNavigationItemColors() {
        val selectedColor = ContextCompat.getColor(this, currentTheme[themeIndex]) // Get the selected theme color

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_selected)
            ),
            intArrayOf(
                selectedColor, // Color for selected state
                ContextCompat.getColor(this, R.color.dark_secondColorOfApp) // Color for unselected state
            )
        )

        // Set the ColorStateList to the itemIconTint and itemTextColor of BottomNavigationView
        bottomNavigView.itemIconTintList = colorStateList
        bottomNavigView.itemTextColor = colorStateList
    }

    private fun updateIcon_ofMusic() {
        // Create tinted versions of the music icon and add them to the array ( Image )
        val redTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.redColor_ofApp)
        val greenTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.greenColor_ofApp)
        val blueTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.blueColor_ofApp)
        val purpleTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.purpleColor_ofApp)
        val red2TintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.red2Color_ofApp)
        val copperTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.copperColor_ofApp)
        val orangeTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.orangeColor_ofApp)
        val goldTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.goldColor_ofApp)
        val pinkTintedIcon = tintDrawable(R.drawable.music_icon_for_song, R.color.pinkColor_ofApp)
        currentTheme_musicIcon.apply {
            add(redTintedIcon)
            add(greenTintedIcon)
            add(blueTintedIcon)
            add(purpleTintedIcon)
            add(red2TintedIcon)
            add(copperTintedIcon)
            add(orangeTintedIcon)
            add(goldTintedIcon)
            add(pinkTintedIcon)
        }

        // For Artist icon
        val redTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.redColor_ofApp)
        val greenTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.greenColor_ofApp)
        val blueTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.blueColor_ofApp)
        val purpleTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.purpleColor_ofApp)
        val red2TintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.red2Color_ofApp)
        val copperTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.copperColor_ofApp)
        val orangeTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.orangeColor_ofApp)
        val goldTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.goldColor_ofApp)
        val pinkTintedArtistIcon = tintDrawable(R.drawable.artist_icon, R.color.pinkColor_ofApp)
        currentTheme_artistIcon.apply {
            add(redTintedArtistIcon)
            add(greenTintedArtistIcon)
            add(blueTintedArtistIcon)
            add(purpleTintedArtistIcon)
            add(red2TintedArtistIcon)
            add(copperTintedArtistIcon)
            add(orangeTintedArtistIcon)
            add(goldTintedArtistIcon)
            add(pinkTintedArtistIcon)
        }

        // For Favourite icon
        val redTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.redColor_ofApp)
        val greenTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.greenColor_ofApp)
        val blueTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.blueColor_ofApp)
        val purpleTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.purpleColor_ofApp)
        val red2TintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.red2Color_ofApp)
        val copperTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.copperColor_ofApp)
        val orangeTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.orangeColor_ofApp)
        val goldTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.goldColor_ofApp)
        val pinkTintedFavouriteIcon = tintDrawable(R.drawable.selected_favorite_icon, R.color.pinkColor_ofApp)
        currentTheme_favouriteIcon.apply {
            add(redTintedFavouriteIcon)
            add(greenTintedFavouriteIcon)
            add(blueTintedFavouriteIcon)
            add(purpleTintedFavouriteIcon)
            add(red2TintedFavouriteIcon)
            add(copperTintedFavouriteIcon)
            add(orangeTintedFavouriteIcon)
            add(goldTintedFavouriteIcon)
            add(pinkTintedFavouriteIcon)
        }
    }

    private fun tintDrawable(drawableResId: Int, colorResId: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(this, drawableResId)?.mutate()
        drawable?.let {
            val color = ContextCompat.getColor(this, colorResId)
            it.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        return drawable
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.mainActivity_adViewId)

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
        //for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteFragment.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistFragment.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()

        // Set current theme
        updateTheme()
        updateBottomNavigationItemColors()

        setTheme(currentTheme_activity[themeIndex])
    }
}