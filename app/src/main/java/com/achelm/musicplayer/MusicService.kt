package com.achelm.musicplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.achelm.musicplayer.activities.PlayerActivity
import com.achelm.musicplayer.fragments.NowPlayingFragment
import com.achelm.musicplayer.models.formatDuration
import com.achelm.musicplayer.models.getImgArt

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager

    companion object {
        lateinit var runnable: Runnable
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    override fun onBind(intent: Intent?): IBinder {
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int) {
        if (PlayerActivity.musicListPA.isEmpty() || PlayerActivity.songPosition !in PlayerActivity.musicListPA.indices) {
            Log.w("MusicService", "Invalid music list or position, stopping service")
            stopSelf()
            return
        }

        // ---- Change: Open PlayerActivity instead of MainActivity ----
        val intent = Intent(baseContext, PlayerActivity::class.java).apply {
            putExtra("index", PlayerActivity.songPosition) // Pass current song position
            putExtra("class", "MusicService") // Identifier for source
        }
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)
        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)
        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)
        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)
        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        // ---- Fix: Use audioUri for embedded art, fallback to albumArtUri ----
        val song = PlayerActivity.musicListPA[PlayerActivity.songPosition]
        val imgArt = getImgArt(this, song.audioUri) // Try audio file first
        var image: Bitmap? = if (imgArt != null) {
            try {
                BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
            } catch (e: Exception) {
                Log.e("MusicService", "Failed to decode embedded art: $e")
                null
            }
        } else {
            null
        }

        // If no embedded art, try loading from albumArtUri
        if (image == null && song.artUri.isNotEmpty()) {
            try {
                contentResolver.openInputStream(android.net.Uri.parse(song.artUri))?.use { input ->
                    image = BitmapFactory.decodeStream(input)
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Failed to load album art from URI: ${song.artUri}", e)
            }
        }

        // Final fallback to default icon
        if (image == null) {
            image = BitmapFactory.decodeResource(resources, R.drawable.music_icon_for_song)
        }

        // Building notification
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.selected_music_icon)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_song_icon, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_song_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.close_song_icon, "Exit", exitPendingIntent)
            .setAutoCancel(false)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaPlayer != null) {
            val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                    .build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            )
            mediaSession.setCallback(object: MediaSessionCompat.Callback() {
                //called when headphones buttons are pressed
                //currently only pause or play music on button click
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    if(PlayerActivity.isPlaying){
                        //pause music
                        PlayerActivity.playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
                        NowPlayingFragment.playPauseBtn.setImageResource(R.drawable.play_icon)
                        PlayerActivity.isPlaying = false
                        PlayerActivity.animationEffects.resumeAnimation()
                        mediaPlayer!!.pause()
                        showNotification(R.drawable.play_icon)
                    }else{
                        //play music
                        PlayerActivity.playPauseIcon.setImageResource(R.drawable.pause_icon_for_player)
                        NowPlayingFragment.playPauseBtn.setImageResource(R.drawable.pause_icon)
                        PlayerActivity.isPlaying = true
                        PlayerActivity.animationEffects.pauseAnimation()
                        mediaPlayer!!.start()
                        showNotification(R.drawable.pause_icon)
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicListPA.isEmpty() || PlayerActivity.songPosition !in PlayerActivity.musicListPA.indices) {
                Log.e("MusicService", "Invalid music list or position: size=${PlayerActivity.musicListPA.size}, position=${PlayerActivity.songPosition}")
                stopSelf()
                return
            }

            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            val uri = android.net.Uri.parse(PlayerActivity.musicListPA[PlayerActivity.songPosition].audioUri)
            Log.d("MusicService", "Setting data source to audio URI: $uri")
            mediaPlayer!!.setDataSource(this, uri)
            mediaPlayer!!.prepare()
            PlayerActivity.playPauseIcon.setImageResource(R.drawable.pause_icon_for_player)
            showNotification(R.drawable.pause_icon)
            PlayerActivity.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.seekBar.progress = 0
            PlayerActivity.seekBar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                PlayerActivity.loudnessEnhancer = LoudnessEnhancer(mediaPlayer!!.audioSessionId)
                PlayerActivity.loudnessEnhancer.enabled = true
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error creating media player for URI: ${PlayerActivity.musicListPA[PlayerActivity.songPosition].audioUri}", e)
            stopSelf()
        }
    }

    fun seekBarSetup() {
        if (mediaPlayer == null) return
        runnable = Runnable {
            PlayerActivity.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0 && mediaPlayer != null) {
            PlayerActivity.playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
            NowPlayingFragment.playPauseBtn.setImageResource(R.drawable.play_icon)
            PlayerActivity.isPlaying = false
            mediaPlayer!!.pause()
            showNotification(R.drawable.play_icon)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // ---- New Method: Pause and dismiss notification ----
    fun pauseAndDismiss() {
        if (mediaPlayer != null && PlayerActivity.isPlaying) {
            mediaPlayer!!.pause()
            PlayerActivity.isPlaying = false
            PlayerActivity.playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
            NowPlayingFragment.playPauseBtn.setImageResource(R.drawable.play_icon)
            PlayerActivity.animationEffects.pauseAnimation()
        }
        stopForeground(true) // Removes notification but keeps service alive
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
    }
}