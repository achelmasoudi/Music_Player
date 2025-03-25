package com.achelm.offmusicplayer.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.achelm.offmusicplayer.LanguageManager
import com.achelm.offmusicplayer.MusicService
import com.achelm.offmusicplayer.activities.PlayerActivity
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.achelm.offmusicplayer.models.setSongPosition

class NowPlayingFragment : Fragment() {

    companion object {
        lateinit var nowPlayingFragmentRoot: LinearLayout
        lateinit var songImage: ImageView
        lateinit var songName: TextView
        lateinit var playPauseBtn: ImageView
        lateinit var nextSongBtn: CardView
    }

    private lateinit var fView: View
    private lateinit var playPauseBtnCardV: CardView
    private lateinit var seekBar: SeekBar
    private lateinit var songImageBackG: CardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Set Language
        LanguageManager.loadLocale(requireContext())

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        fView = inflater.inflate(R.layout.fragment_now_playing, container, false)

        // Initialize variables
        nowPlayingFragmentRoot = fView.findViewById(R.id.nowPlayingFragment_linearLayout_rootId)
        seekBar = fView.findViewById(R.id.nowPlayingFragment_seekBarId)
        songImage = fView.findViewById(R.id.nowPlayingFragment_songImageId)
        songName = fView.findViewById(R.id.nowPlayingFragment_songNameId)
        playPauseBtn = fView.findViewById(R.id.nowPlayingFragment_playPauseIconId)
        nextSongBtn = fView.findViewById(R.id.nowPlayingFragment_nextSongBtnId)
        playPauseBtnCardV = fView.findViewById(R.id.nowPlayingFragment_playPauseBtnId)
        songImageBackG = fView.findViewById(R.id.nowPlayingFragment_backgroundId)

        nowPlayingFragmentRoot.visibility = View.GONE

        playPauseBtnCardV.setOnClickListener {
            if(PlayerActivity.isPlaying)
                pauseMusic()
            else
                playMusic()
        }

        nextSongBtn.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(requireContext())
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
                .into(songImage)

            songName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            playMusic()
        }

        nowPlayingFragmentRoot.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            startActivity(intent)
        }

        // Set current theme
        updateTheme()

        return fView
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){

            nowPlayingFragmentRoot.visibility = View.VISIBLE
            songName.isSelected = true
            Glide.with(requireContext())
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
                .into(songImage)
            songName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

            if(PlayerActivity.isPlaying)
                playPauseBtn.setImageResource(R.drawable.pause_icon)
            else
                playPauseBtn.setImageResource(R.drawable.play_icon)


            //set seekbar
            seekBar.progress = 0
            seekBar.max = PlayerActivity.musicService!!.mediaPlayer!!.duration
            seekBarSetup()
        }

        // Set current theme
        updateTheme()

        // Set Language
        LanguageManager.loadLocale(requireContext())
    }

    fun seekBarSetup(){
        MusicService.runnable = Runnable {
            seekBar.progress = PlayerActivity.musicService!!.mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(MusicService.runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(MusicService.runnable, 0)
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        playPauseBtn.setImageResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        playPauseBtn.setImageResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
    }

    private fun updateTheme() {
        // Set progress tint color
        seekBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )
        // Set thumb tint color
        seekBar.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )

        songImageBackG.setCardBackgroundColor(ContextCompat.getColor( requireContext() ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))

    }
}