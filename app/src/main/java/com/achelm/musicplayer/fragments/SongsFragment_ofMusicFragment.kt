package com.achelm.musicplayer.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.activities.PlayNextActivity
import com.achelm.musicplayer.activities.PlayerActivity
import com.achelm.musicplayer.adapters.MusicAdapter
import com.achelm.musicplayer.models.Music
import com.achelm.musicplayer.models.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class SongsFragment_ofMusicFragment : Fragment() {

    companion object{
        lateinit var MusicListMA : ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC")
    }

    private lateinit var fView: View
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var recyclerViewOfSongs: RecyclerView
    private lateinit var shuffleBtn: CardView
    private lateinit var playNextBtn: CardView
    private lateinit var totalSongs: TextView
    private lateinit var sortBtn: CardView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        fView = inflater.inflate(R.layout.fragment_songs_of_fragment_music , container , false)

        // Initialize variables
        recyclerViewOfSongs = fView.findViewById(R.id.songsFragmentOfMusicFrag_recyclerViewOfSongsId)
        shuffleBtn = fView.findViewById(R.id.songsFragmentOfMusicFrag_shuffleBtnId)
        playNextBtn = fView.findViewById(R.id.songsFragmentOfMusicFrag_playNextBtnId)
        totalSongs = fView.findViewById(R.id.songsFragmentOfMusicFrag_totalSongsId)
        sortBtn = fView.findViewById(R.id.songsFragmentOfMusicFrag_sortBtnId)

        initializeLayout()

        shuffleBtn.setOnClickListener {
            val intent = Intent(requireActivity(), PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        playNextBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), PlayNextActivity::class.java))
        }

        sortBtn.setOnClickListener {
            val menuList = arrayOf(
                resources.getString(R.string.songsFragmentOfMusicFragment_Recently_Added),
                resources.getString(R.string.songsFragmentOfMusicFragment_Song_Title),
                resources.getString(R.string.songsFragmentOfMusicFragment_File_Size)
            )

            var currentSort = sortOrder
            val builder = MaterialAlertDialogBuilder(requireContext())

            builder.setTitle(resources.getString(R.string.songsFragmentOfMusicFragment_Sorting))
                .setPositiveButton(resources.getString(R.string.songsFragmentOfMusicFragment_OK)) { _ , _ ->
                    val editor = requireActivity().getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()

                    // Update the music list based on the selected sorting option
                    sortOrder = currentSort
                    MusicListMA = getAllAudio()
                    musicAdapter.updateMusicList(MusicListMA)
                }
                .setSingleChoiceItems(menuList, currentSort){ _ , which ->
                    currentSort = which
                }
                .setNegativeButton(resources.getString(R.string.songsFragmentOfMusicFragment_Cancel)) { dialog , _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
        }

        return fView
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout(){
        search = false
        val sortEditor = requireContext().getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)

        MusicListMA = getAllAudio()

        recyclerViewOfSongs.setHasFixedSize(true)
        recyclerViewOfSongs.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireActivity(), MusicListMA)
        recyclerViewOfSongs.adapter = musicAdapter

        // Animation for RecyclerView
        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerViewOfSongs.layoutAnimation = songListAnimFF

        totalSongs.text  = "${resources.getString(R.string.songsFragmentOfMusicFragment_totalSongs)} ${musicAdapter.itemCount}"

    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): ArrayList<Music> {

        val tempList = ArrayList<Music>()

        // Define the directories where WhatsApp audio files reside
        val whatsappDirectories = listOf (
            "/WhatsApp/Media/WhatsApp Audio",
            "/WhatsApp/.Statuses" // Optional, in case WhatsApp stores temporary audio files here
        )

        // Define system sound directories
        val systemSoundDirectories = listOf (
            "/Ringtones",
            "/Notifications",
            "/Alarms"
        )

        // Construct the selection to exclude WhatsApp and system sound directories
        val selectionBuilder = StringBuilder (
            "${MediaStore.Audio.Media.TITLE} != 0 AND ("
        )
        for (dir in whatsappDirectories + systemSoundDirectories) {
            selectionBuilder.append(
                "${MediaStore.Audio.Media.DATA} NOT LIKE ? AND "
            )
        }
        selectionBuilder.delete (
            selectionBuilder.length - 5,
            selectionBuilder.length
        ) // Remove the last " AND "
        selectionBuilder.append(")")

        // Prepare arguments for excluding WhatsApp and system sound directories
        val excludedDirectories = whatsappDirectories + systemSoundDirectories
        val selectionArgs = Array(excludedDirectories.size) {
            "%${excludedDirectories[it]}%"
        }

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
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionBuilder.toString(),
            selectionArgs,
            sortingList[sortOrder],
            null
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: resources.getString(R.string.songsFragmentOfMusicFragment_Unknown)
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: resources.getString(R.string.songsFragmentOfMusicFragment_Unknown)
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: resources.getString(R.string.songsFragmentOfMusicFragment_Unknown)
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: resources.getString(R.string.songsFragmentOfMusicFragment_Unknown)
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) ?: resources.getString(R.string.songsFragmentOfMusicFragment_Unknown)
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()

                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )

                    val file = File(music.path)

                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            }

            cursor.close()
        }

        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
            exitApplication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        val sortEditor = requireContext().getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)

        if(sortOrder != sortValue){
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }

        // Notify the adapter that the dataset has changed
        musicAdapter.notifyDataSetChanged()
    }

}