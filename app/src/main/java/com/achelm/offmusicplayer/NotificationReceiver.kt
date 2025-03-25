package com.achelm.offmusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.activities.PlayerActivity
import com.achelm.offmusicplayer.fragments.NowPlayingFragment
import com.achelm.offmusicplayer.models.favouriteChecker
import com.achelm.offmusicplayer.models.setSongPosition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){
            // Only play next or prev song, when music list contains more than one song
            ApplicationClass.PREVIOUS ->
                if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY ->
                if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT ->
                if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> {
                PlayerActivity.musicService?.pauseAndDismiss()
            }
        }

    }
    private fun playMusic(){
        // NEW: Added null check to prevent crashes
        if (PlayerActivity.musicService?.mediaPlayer == null) return

        PlayerActivity.isPlaying = true
        PlayerActivity.musicService?.mediaPlayer?.start()
        PlayerActivity.musicService?.showNotification(R.drawable.pause_icon)
        PlayerActivity.playPauseIcon.setImageResource(R.drawable.pause_icon_for_player)
        PlayerActivity.animationEffects.resumeAnimation()

        // NEW: Check if NowPlayingFragment is attached before accessing views
        NowPlayingFragment.playPauseBtn?.setImageResource(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        // NEW: Added null check to prevent crashes
        if (PlayerActivity.musicService?.mediaPlayer == null) return

        PlayerActivity.isPlaying = false
        PlayerActivity.musicService?.mediaPlayer?.pause()
        PlayerActivity.musicService?.showNotification(R.drawable.play_icon)
        PlayerActivity.playPauseIcon.setImageResource(R.drawable.play_icon_for_player)
        PlayerActivity.animationEffects.pauseAnimation()

        // NEW: Check if NowPlayingFragment is attached before accessing views
        NowPlayingFragment.playPauseBtn?.setImageResource(R.drawable.play_icon)
    }

    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)

        // NEW: Added bounds checking to prevent crashes
        if (PlayerActivity.songPosition !in PlayerActivity.musicListPA.indices) return

        PlayerActivity.musicService?.createMediaPlayer()

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
            .into(PlayerActivity.currentSongImage)

        PlayerActivity.currentSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        // NEW: Check if NowPlayingFragment is attached before accessing views
        NowPlayingFragment.songImage?.let {
            Glide.with(context)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
                .into(it)
        }

        NowPlayingFragment.songName?.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        playMusic()

        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)

        if (PlayerActivity.isFavourite)
            PlayerActivity.favouriteBtn.setImageDrawable(MainActivity.currentTheme_favouriteIcon[MainActivity.themeIndex])
        else
            PlayerActivity.favouriteBtn.setImageResource(R.drawable.unselected_favorite_icon)
    }
}