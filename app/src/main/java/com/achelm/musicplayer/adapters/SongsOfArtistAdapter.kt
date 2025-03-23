package com.achelm.musicplayer.adapters

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.activities.PlayNextActivity
import com.achelm.musicplayer.activities.PlayerActivity
import com.achelm.musicplayer.activities.PlaylistDetailsActivity
import com.achelm.musicplayer.activities.SongsOfFolderActivity
import com.achelm.musicplayer.fragments.PlaylistFragment
import com.achelm.musicplayer.models.formatDuration
import com.achelm.musicplayer.models.Music
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SongsOfArtistAdapter(private val activity: Activity, private var musicList: ArrayList<Music>, private val playlistDetails: Boolean = false,
                           private val selectionActivity: Boolean = false) : RecyclerView.Adapter<SongsOfArtistAdapter.MyViewHolder>() {

    inner class MyViewHolder(i: View) : RecyclerView.ViewHolder(i) {
        val button: CardView = i.findViewById(R.id.musicCardItem_buttonId)
        val songName: TextView = i.findViewById(R.id.musicCardItem_songNameId)
        val album: TextView = i.findViewById(R.id.musicCardItem_songAlbumId)
        val songImage: ImageView = i.findViewById(R.id.musicCardItem_songImageId)
        val songImageBackG: CardView = i.findViewById(R.id.musicCardItem_songImage_backgroundId)
        val moreBtn: CardView = i.findViewById(R.id.musicCardItem_moreBtnId)
        val root: RelativeLayout = i.findViewById(R.id.musicCardItem_relativeLayoutId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.music_card_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.songName.text = musicList[position].title
        holder.album.text = "${musicList[position].artist} - ${musicList[position].album}"

        // Set current theme
        updateTheme(holder.songImageBackG , musicList[position].artUri , holder.songImage)

        //for play next feature
        if (!selectionActivity)
            holder.moreBtn.setOnClickListener {
                var bottomSheetView: View = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_layout_of_more_in_music_fragment,
                    activity.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_container))

                var bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialogTheme)

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

                // Add this song To Play Next
                var playNextBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_addToPlayNext_buttonId)
                playNextBtn!!.setOnClickListener {
                    try {
                        if (PlayNextActivity.playNextList.isEmpty()) {
                            PlayNextActivity.playNextList.add(PlayerActivity.musicListPA[PlayerActivity.songPosition])
                            PlayerActivity.songPosition = 0
                        }

                        PlayNextActivity.playNextList.add(musicList[position])
                        PlayerActivity.musicListPA = ArrayList()
                        PlayerActivity.musicListPA.addAll(PlayNextActivity.playNextList)

                        Snackbar.make(activity, holder.root, activity.resources.getString(R.string.musicAdapter_Added_to_play_next), Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(activity, holder.root, activity.resources.getString(R.string.musicAdapter_Play_the_song_first), 3000).show()
                    }
                    bottomSheetDialog.dismiss()
                }

                // About Song
                var aboutSongBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_aboutSong_buttonId)
                aboutSongBtn!!.setOnClickListener {
                    bottomSheetDialog.dismiss()

                    var bottomSheetOfSongDetails: View = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_layout_of_song_details,
                        activity.findViewById(R.id.bottomSheetLayoutOfSongDetails_container))

                    var bottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialogTheme)

                    bottomSheetDialog.setContentView(bottomSheetOfSongDetails)
                    bottomSheetDialog.show()

                    // Find views
                    var image: ImageView = bottomSheetDialog.findViewById(R.id.songDetails_imageId)!!
                    var titleTxt: TextView = bottomSheetDialog.findViewById(R.id.songDetails_titleId)!!
                    var albumTxt: TextView = bottomSheetDialog.findViewById(R.id.songDetails_albumId)!!
                    var artistTxt: TextView = bottomSheetDialog.findViewById(R.id.songDetails_artistId)!!
                    var durationTxt: TextView = bottomSheetDialog.findViewById(R.id.songDetails_durationId)!!
                    var locationTxt: TextView = bottomSheetDialog.findViewById(R.id.songDetails_locationId)!!

                    // Set data
                    Glide.with(activity)
                        .load(musicList[position].artUri)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop()).into(image)

                    titleTxt.text = musicList[position].title
                    albumTxt.text = musicList[position].album
                    artistTxt.text = musicList[position].artist
                    durationTxt.text = formatDuration(musicList[position].duration)
                    locationTxt.text = musicList[position].path
                }

                // Delete song from device
                var deleteBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_deleteSong_buttonId)
                deleteBtn!!.setOnClickListener {
                    val alertDialog = MaterialAlertDialogBuilder(activity)
                        .setTitle(activity.resources.getString(R.string.musicAdapter_Delete_Song))
                        .setMessage(activity.resources.getString(R.string.musicAdapter_Are_youSure_you_want_to_deleteSong))
                        .setPositiveButton(activity.resources.getString(R.string.musicAdapter_Delete)) { _, _ ->
                            val song = musicList[position]

                            // Delete from device's storage
                            val contentResolver: ContentResolver = activity.contentResolver
                            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            val selection = MediaStore.Audio.Media._ID + "=?"
                            val selectionArgs = arrayOf(song.id)
                            val deletedCount = contentResolver.delete(uri, selection, selectionArgs)

                            if (deletedCount > 0) {
                                // Delete from the app's list
                                musicList.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, itemCount)

                                // I should remove it fron Now Playing Activity too
                                Snackbar.make(activity, holder.root, activity.resources.getString(R.string.musicAdapter_Song_deleted_from_your_device), Snackbar.LENGTH_SHORT).show()
                                bottomSheetDialog.dismiss()
                            } else {
                                Snackbar.make(activity, holder.root, activity.resources.getString(R.string.musicAdapter_Failed_to_deleteSong), Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton(activity.resources.getString(R.string.musicAdapter_Cancel), null)
                        .create()
                    alertDialog.show()
                }
            }

        when {
            playlistDetails -> {
                holder.button.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            // Selection Of Songs for Playlist
            selectionActivity -> {
                holder.button.setOnClickListener {
                    if (addSong(musicList[position]))
                        holder.button.setBackgroundColor(ContextCompat.getColor(activity, MainActivity.currentTheme[MainActivity.themeIndex]))
                    else {
                        holder.button.setBackgroundColor(ContextCompat.getColor(activity, R.color.dark_primaryColor_ofApp))
                    }
                }
            }

            else -> {
                holder.button.setOnClickListener {
                    when {
                        SongsOfFolderActivity.search -> sendIntent(ref = "SongsOfArtistAdapterSearch", pos = position)

                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)

                        else -> sendIntent(ref = "SongsOfArtistAdapter", pos = position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = musicList.size

    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(activity, intent, null)
    }

    private fun addSong(song: Music): Boolean{
        PlaylistFragment.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if(song.id == music.id){
                PlaylistFragment.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistFragment.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun updateTheme(imageBackG: CardView , artUri: String , songImage: ImageView) {
        Glide.with(activity).load(artUri).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop()).into(songImage)

        imageBackG.setCardBackgroundColor(ContextCompat.getColor( activity ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))
    }

}