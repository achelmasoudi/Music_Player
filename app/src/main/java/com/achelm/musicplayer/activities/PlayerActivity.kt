package com.achelm.musicplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.achelm.musicplayer.ApplicationClass.Companion.AD_UNIT_ID
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.MusicService
import com.achelm.musicplayer.R
import com.achelm.musicplayer.fragments.FavouriteFragment
import com.achelm.musicplayer.fragments.NowPlayingFragment
import com.achelm.musicplayer.fragments.PlaylistFragment
import com.achelm.musicplayer.fragments.SongsFragment_ofMusicFragment
import com.achelm.musicplayer.models.Music
import com.achelm.musicplayer.models.exitApplication
import com.achelm.musicplayer.models.favouriteChecker
import com.achelm.musicplayer.models.formatDuration
import com.achelm.musicplayer.models.getImgArt
import com.achelm.musicplayer.models.setSongPosition
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lukelorusso.verticalseekbar.VerticalSeekBar
import de.hdodenhof.circleimageview.CircleImageView

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying:Boolean = false
        var musicService: MusicService? = null
        var repeat: Boolean = false
        var min5: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var _1hour: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
        lateinit var loudnessEnhancer: LoudnessEnhancer

        lateinit var arrowBack: CardView

        lateinit var favouriteBtn: ImageView
        lateinit var favouriteBtnCardView: CardView

        lateinit var animationEffects: LottieAnimationView
        lateinit var currentSongImage: CircleImageView

        lateinit var currentSongName: TextView
        lateinit var artistName: TextView

        lateinit var previousSongBtn: CardView
        lateinit var playPauseIcon: ImageView
        lateinit var playPauseBtn: CardView
        lateinit var nextSongBtn: CardView

        lateinit var seekBar: SeekBar
        lateinit var tvSeekBarStart: TextView
        lateinit var tvSeekBarEnd: TextView
    }

    lateinit var moreBtn: CardView
    lateinit var repeatIcon: ImageView
    lateinit var repeatBtn: CardView
    lateinit var sleepTimerBtn: CardView
    lateinit var sleepTimerIcon: ImageView
    private lateinit var root: RelativeLayout
    private lateinit var playPauseBackground: RelativeLayout

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Language
        LanguageManager.loadLocale(this)
        setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        setContentView(R.layout.activity_player)

        // Initialize variables
        root = findViewById(R.id.playerActivity_relativeLayoutRootId)
        arrowBack = findViewById(R.id.playerActivity_arrowBackId)
        playPauseBackground = findViewById(R.id.playerActivity_playPauseBackgroundId)

        favouriteBtn = findViewById(R.id.playerActivity_favouriteIconId)
        favouriteBtnCardView = findViewById(R.id.playerActivity_favouriteBtnId)

        animationEffects = findViewById(R.id.playerActivity_lottieAnimationId)
        currentSongImage = findViewById(R.id.playerActivity_currentSongImageId)

        currentSongName = findViewById(R.id.playerActivity_currentSongNameId)
        artistName = findViewById(R.id.playerActivity_songArtistNameId)

        previousSongBtn = findViewById(R.id.playerActivity_previousSongBtnId)
        playPauseIcon = findViewById(R.id.playerActivity_playPauseIconId)
        playPauseBtn = findViewById(R.id.playerActivity_playPauseBtnId)
        nextSongBtn = findViewById(R.id.playerActivity_nextSongBtnId)
        seekBar = findViewById(R.id.playerActivity_seekBarId)
        tvSeekBarStart = findViewById(R.id.playerActivity_tvSeekBarStartId)
        tvSeekBarEnd = findViewById(R.id.playerActivity_tvSeekBarEndId)

        repeatIcon = findViewById(R.id.playerActivity_repeatIconId)
        repeatBtn = findViewById(R.id.playerActivity_repeatBtnId)

        sleepTimerBtn = findViewById(R.id.playerActivity_sleepTimerBtnId)
        sleepTimerIcon = findViewById(R.id.playerActivity_sleepTimerIconId)

        if (intent.data?.scheme.contentEquals("content")) {
            songPosition = 0
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))

            Glide.with(this)
                .load(getImgArt(this , musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music_icon_for_song).centerCrop())
                .into(currentSongImage)

            currentSongName.text = musicListPA[songPosition].title
            artistName.text = musicListPA[songPosition].artist
        }
        else
            initializeLayout()

        arrowBack.setOnClickListener { finish() }

        playPauseBtn.setOnClickListener{
            if (isPlaying)
                pauseMusic()
            else
                playMusic()
        }

        previousSongBtn.setOnClickListener {
            prevNextSong(increment = false)
        }

        nextSongBtn.setOnClickListener {
            prevNextSong(increment = true)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if(isPlaying) R.drawable.pause_icon else R.drawable.play_icon)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        repeatBtn.setOnClickListener {
            if(!repeat) {
                repeat = true
                repeatIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))
            }
            else {
                repeat = false
                repeatIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_secondColorOfApp))
            }
        }

        sleepTimerBtn.setOnClickListener {
            val timer = min5 || min15 || min30 || _1hour
            if (!timer) {
                showBottomSheet_ofSleepTimer()
            }
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle( resources.getString(R.string.playerActivity_stopTimer) )
                    .setMessage( resources.getString(R.string.playerActivity_Doyou_want_tostop_timer) )
                    .setPositiveButton(resources.getString(R.string.playerActivity_Yes)){ _, _ ->
                        min5 = false
                        min15 = false
                        min30 = false
                        _1hour = false
                        sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_secondColorOfApp))
                    }
                    .setNegativeButton(resources.getString(R.string.playerActivity_No)){ dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
            }
        }

        favouriteBtnCardView.setOnClickListener {
            fIndex = favouriteChecker(musicListPA[songPosition].id)
            if (isFavourite) {
                isFavourite = false
                favouriteBtn.setImageResource(R.drawable.unselected_favorite_icon)
                FavouriteFragment.favouriteSongs.removeAt(fIndex)
            }
            else {
                isFavourite = true
                favouriteBtn.setImageDrawable(MainActivity.currentTheme_favouriteIcon[MainActivity.themeIndex])
                FavouriteFragment.favouriteSongs.add(musicListPA[songPosition])
            }
            FavouriteFragment.favouritesChanged = true
        }


        // Share , Equalizer and Booster Buttons
        shareEquaBoosterBtnsProcess()

        // For Adds
        adView()

        // Set current theme
        updateTheme()
    }

    private fun shareEquaBoosterBtnsProcess() {
        // Initialize the variables
        val shareBtn: CardView = findViewById(R.id.playerActivity_shareBtnId)
        val equalizerBtn: CardView =  findViewById(R.id.playerActivity_equalizerBtnId)
        val boosterBtn: CardView =  findViewById(R.id.playerActivity_boosterBtnId)

        shareBtn.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.playerActivity_SharingMusicFile)))
        }

        equalizerBtn.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)

            }
            catch (e: Exception) {
                Toast.makeText(this,  resources.getString(R.string.playerActivity_EqualizerFeature_not_Supported), Toast.LENGTH_SHORT).show()
            }
        }

        boosterBtn.setOnClickListener {
            val customDialogB = LayoutInflater.from(this).inflate(R.layout.audio_booster, root, false)
            val dialogB = MaterialAlertDialogBuilder(this).setView(customDialogB)
                .setOnCancelListener { playMusic() }
                .setPositiveButton(resources.getString(R.string.playerActivity_Ok)) { self, _ ->
                    val verticalBar = customDialogB.findViewById<VerticalSeekBar>(R.id.verticalBarId)
                    loudnessEnhancer.setTargetGain(verticalBar.progress * 100)
                    playMusic()
                    self.dismiss()
                }
                .create()

            dialogB.show()

            val verticalBar = customDialogB.findViewById<VerticalSeekBar>(R.id.verticalBarId)
            val progressText = customDialogB.findViewById<TextView>(R.id.progressTextId)

            verticalBar.progress = loudnessEnhancer.targetGain.toInt() / 100
            progressText.text = "${resources.getString(R.string.playerActivity_AudioBoost)}\n${loudnessEnhancer.targetGain.toInt() / 10} %"
            verticalBar.setOnProgressChangeListener { progress ->
                progressText.text = "${resources.getString(R.string.playerActivity_AudioBoost)}\n${progress * 10} %"
            }
        }

    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)

        when (intent.getStringExtra("class")) {
            "NowPlaying" , "MusicService" -> {
                setLayout()

                if(isPlaying) {
                    playPauseIcon.setImageResource(R.drawable.pause_icon_for_player)
                    animationEffects.resumeAnimation()
                }
                else {
                    playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
                    animationEffects.pauseAnimation()
                }

                tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                seekBar.max = musicService!!.mediaPlayer!!.duration

                setSeekBar()
            }

            "MusicAdapterSearch" ->
                initServiceAndPlaylist(SongsFragment_ofMusicFragment.musicListSearch, shuffle = false)

            "MusicAdapter" ->
                initServiceAndPlaylist(SongsFragment_ofMusicFragment.MusicListMA, shuffle = false)

            "FavouriteAdapter" ->
                initServiceAndPlaylist(FavouriteFragment.favouriteSongs, shuffle = false)

            "MainActivity" ->
                initServiceAndPlaylist(SongsFragment_ofMusicFragment.MusicListMA, shuffle = true)

            "SongsOfFolderActivity" ->
                initServiceAndPlaylist(SongsOfFolderActivity.songListOfFolder, shuffle = true)

            "SongsOfFolderAdapter" ->
                initServiceAndPlaylist(SongsOfFolderActivity.songListOfFolder, shuffle = false)

            "SongsOfFolderAdapterSearch" ->
                initServiceAndPlaylist(SongsOfFolderActivity.musicListSearch, shuffle = false)

            "SongsOfArtistActivity" ->
                initServiceAndPlaylist(SongsOfArtistActivity.songListOfArtist, shuffle = true)

            "SongsOfArtistAdapter" ->
                initServiceAndPlaylist(SongsOfArtistActivity.songListOfArtist, shuffle = false)

            "SongsOfArtistAdapterSearch" ->
                initServiceAndPlaylist(SongsOfArtistActivity.musicListSearch, shuffle = false)

            "FavouriteShuffle"->
                initServiceAndPlaylist(FavouriteFragment.favouriteSongs, shuffle = true)

            "PlaylistDetailsAdapter" ->
                initServiceAndPlaylist(PlaylistFragment.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist, shuffle = false)

            "PlaylistDetailsShuffle" ->
                initServiceAndPlaylist(PlaylistFragment.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist, shuffle = true)

            "PlayNext" ->
                initServiceAndPlaylist(PlayNextActivity.playNextList, shuffle = false, playNext = true)
        }

        if (musicService != null && !isPlaying)
            playMusic()
    }

    private fun setLayout(){
        fIndex = favouriteChecker(musicListPA[songPosition].id)

        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_icon_for_song).centerCrop())
            .into(currentSongImage)

        currentSongName.text = musicListPA[songPosition].title
        artistName.text = musicListPA[songPosition].artist

        // Moving title
        currentSongName.isSelected = true
        animationEffects.resumeAnimation()

        if (repeat)
            repeatIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))

        if ( min5 || min15 || min30 || _1hour)
            sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))

        if (isFavourite)
            favouriteBtn.setImageDrawable(MainActivity.currentTheme_favouriteIcon[MainActivity.themeIndex])

        else
            favouriteBtn.setImageResource(R.drawable.unselected_favorite_icon)

    }

    private fun setSeekBar() {

        MusicService.runnable = Runnable {
            tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            seekBar.progress = musicService!!.mediaPlayer!!.currentPosition

            //increment seekbar with song position
            Handler(Looper.getMainLooper()).postDelayed(MusicService.runnable, 200)
        }

        // start runnable after 0 millisecond
        Handler(Looper.getMainLooper()).postDelayed(MusicService.runnable, 0)
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            val uri = Uri.parse(musicListPA[songPosition].audioUri)
            Log.d("PlayerActivity", "Setting data source to audio URI: $uri")
            musicService!!.mediaPlayer!!.setDataSource(this, uri)
            musicService!!.mediaPlayer!!.prepare()
            tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            seekBar.progress = 0
            seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
            playMusic()
            loudnessEnhancer = LoudnessEnhancer(musicService!!.mediaPlayer!!.audioSessionId)
            loudnessEnhancer.enabled = true
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error creating media player for URI: ${musicListPA[songPosition].audioUri}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun playMusic() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        playPauseIcon.setImageResource(R.drawable.pause_icon_for_player)
        musicService!!.showNotification(R.drawable.pause_icon)

        // Moving title
        currentSongName.isSelected = true

        // play the animations effects
        animationEffects.resumeAnimation()
    }

    private fun pauseMusic() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
        musicService!!.showNotification(R.drawable.play_icon)

        // Moving title
        currentSongName.isSelected = false

        // pause the animations effects
        animationEffects.pauseAnimation()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected (name: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        setLayout()

        //for refreshing now playing image & text on song completion
        NowPlayingFragment.songName.isSelected = true
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
            .into(NowPlayingFragment.songImage)
        NowPlayingFragment.songName.text = musicListPA[songPosition].title
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheet_ofSleepTimer(){
        var bottomSheetView: View = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout_of_sleeptimer,
            findViewById(R.id.bottomSheetLayoutOfSleepTimer_containerId))

        var bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        // Initialize the variables
        val _5minTimer: LinearLayout? = bottomSheetDialog.findViewById(R.id.min_5)
        val _15minTimer: LinearLayout? = bottomSheetDialog.findViewById(R.id.min_15)
        val _30minTimer: LinearLayout? =  bottomSheetDialog.findViewById(R.id.min_30)
        val _1hourTimer: LinearLayout? =  bottomSheetDialog.findViewById(R.id._1hour)

        _5minTimer!!.setOnClickListener {
            Toast.makeText(baseContext,  resources.getString(R.string.playerActivity_Stop_after_5minutes), Toast.LENGTH_SHORT).show()
            sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))
            min5 = true
            Thread { Thread.sleep((5 * 60000).toLong())
                if(min5) exitApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }

        _15minTimer!!.setOnClickListener {
            Toast.makeText(baseContext,  resources.getString(R.string.playerActivity_Stop_after_15minutes), Toast.LENGTH_SHORT).show()
            sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))
            min15 = true
            Thread { Thread.sleep((15 * 60000).toLong())
                if(min15) exitApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }

        _30minTimer!!.setOnClickListener {
            Toast.makeText(baseContext,  resources.getString(R.string.playerActivity_Stop_after_30minutes), Toast.LENGTH_SHORT).show()
            sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))
            min30 = true
            Thread { Thread.sleep((30 * 60000).toLong())
                if(min30) exitApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }

        _1hourTimer!!.setOnClickListener {
            Toast.makeText(baseContext,  resources.getString(R.string.playerActivity_Stop_after_1hour), Toast.LENGTH_SHORT).show()
            sleepTimerIcon.setColorFilter(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex] ))
            _1hour = true
            Thread { Thread.sleep((60 * 60000).toLong())
                if(_1hour) exitApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!

            var unknown = resources.getString(R.string.playerActivity_unknown)

            return Music(id = unknown, title = path.toString(), album = unknown, artist = unknown, duration = duration,
                artUri = unknown, path = path.toString())
        }
        finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(musicListPA[songPosition].id == resources.getString(R.string.playerActivity_unknown) && !isPlaying) exitApplication()
    }
    private fun initServiceAndPlaylist(playlist: ArrayList<Music>, shuffle: Boolean, playNext: Boolean = false){
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        if (shuffle)
            musicListPA.shuffle()

        setLayout()

        if (!playNext)
            PlayNextActivity.playNextList = ArrayList()
    }

    private fun adView() {
        var pAdView: AdView = findViewById(R.id.playerActivity_adViewId)

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
        // Set current theme
        window.statusBarColor = ContextCompat.getColor( this , MainActivity.currentTheme_playerActivity[MainActivity.themeIndex])
        seekBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]) )
        seekBar.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]) )
        currentSongName.setTextColor( ContextCompat.getColor( this , MainActivity.currentTheme[MainActivity.themeIndex]) )

        // Set the updated of gradient background of Player Activity
        val gradientDrawable = root.background as GradientDrawable
        gradientDrawable.colors = intArrayOf (
            ContextCompat.getColor(this, R.color.dark_primaryColor_ofApp),
            Color.parseColor("#C8191C20"),
            ContextCompat.getColor(this, MainActivity.currentTheme_playerActivity[MainActivity.themeIndex] )
        )

        // Set the updated of gradient play-pause Button of Player Activity
        val playPauseDrawable = playPauseBackground.background as GradientDrawable
        playPauseDrawable.colors = intArrayOf (
            ContextCompat.getColor(this, MainActivity.currentTheme[MainActivity.themeIndex]),
            ContextCompat.getColor(this, MainActivity.currentTheme_light[MainActivity.themeIndex] )
        )

        // Set the update of Equalizer Animation based on the current theme
        val animationResource = when (MainActivity.themeIndex) {
            0 -> R.raw.red_equalizer_animation
            1 -> R.raw.green_equalizer_animation
            2 -> R.raw.blue_equalizer_animation
            3 -> R.raw.purple_equalizer_animation
            4 -> R.raw.red2_equalizer_animation
            5 -> R.raw.copper_equalizer_animation
            6 -> R.raw.orange_equalizer_animation
            7 -> R.raw.gold_equalizer_animation
            8 -> R.raw.pink_equalizer_animation
            else -> R.raw.red_equalizer_animation // Default animation resource
        }
        animationEffects.setAnimation(animationResource)
    }
}