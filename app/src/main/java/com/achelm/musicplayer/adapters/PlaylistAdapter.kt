package com.achelm.musicplayer.adapters

import android.app.Activity
import android.content.Intent
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
import com.achelm.musicplayer.models.Playlist
import com.achelm.musicplayer.activities.PlaylistDetailsActivity
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.fragments.PlaylistFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlaylistAdapter(private val activity: Activity, private var playlistList: ArrayList<Playlist> , private val instructionView: LinearLayout) :
    RecyclerView.Adapter<PlaylistAdapter.MyViewHolder>() {

    class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {
        val button: CardView = iv.findViewById(R.id.playlistCardItem_buttonId)
        val playlistImage: ImageView = iv.findViewById(R.id.playlistCardItem_playlistImageId)
        val playlistName: TextView = iv.findViewById(R.id.playlistCardItem_playlistNameId)
        val songImageBackG: CardView = iv.findViewById(R.id.playlistCardItem_songImage_backgroundId)
        val root: RelativeLayout = iv.findViewById(R.id.playlistCardItem_relativeLayoutRootId)
        val playlistMoreBtn: CardView = iv.findViewById(R.id.playlistCardItem_playlistMoreBtnId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(activity).inflate(R.layout.playlist_card_item , parent , false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.playlistName.text = playlistList[position].name
        holder.playlistName.isSelected = true

        // Default theme of icon and background
        holder.playlistImage.setImageDrawable(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex])
        holder.songImageBackG.setCardBackgroundColor(ContextCompat.getColor( activity ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))

        holder.playlistMoreBtn.setOnClickListener {
            showDialogOfDeleteEdit(holder.root , holder.adapterPosition)
        }

        holder.button.setOnClickListener {
            val intent = Intent(activity, PlaylistDetailsActivity::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(activity, intent, null)
        }

        if(PlaylistFragment.musicPlaylist.ref[position].playlist.size > 0) {
            // Set current theme
            updateTheme(holder.songImageBackG , PlaylistFragment.musicPlaylist.ref[position].playlist[0].artUri , holder.playlistImage)
        }
    }

    override fun getItemCount(): Int = playlistList.size

    private fun showDialogOfDeleteEdit(root: RelativeLayout ,  position: Int) {
        var bottomSheetView: View = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_layout_of_more_in_playlist,
            activity.findViewById(R.id.bottomSheetLayoutOfMore_inPlaylist_container))

        var bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialogTheme)

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        // Delete song
        var deleteBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inPlaylist_deletePlaylist_buttonId)
        deleteBtn!!.setOnClickListener {
            deletePlaylistDialog(bottomSheetDialog , position)
        }
    }

    private fun deletePlaylistDialog(bottomSheetDialog: BottomSheetDialog , position: Int) {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.setTitle(playlistList[position].name)
            .setMessage(activity.resources.getString(R.string.playlistAdapter_Do_you_want_to_delete_playlist))
            .setPositiveButton(activity.resources.getString(R.string.playlistAdapter_Yes)) { dialog, _ ->
                PlaylistFragment.musicPlaylist.ref.removeAt(position)
                refreshPlaylist()
                dialog.dismiss()
            }
            .setNegativeButton(activity.resources.getString(R.string.playlistAdapter_No)) {dialog, _ ->
                dialog.dismiss()
            }

        val customDialog = builder.create()
        customDialog.show()

        bottomSheetDialog.dismiss()
    }

    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.clear()
        playlistList.addAll(PlaylistFragment.musicPlaylist.ref)
        notifyDataSetChanged()
        if (playlistList.isEmpty()) {
            instructionView.visibility = View.VISIBLE
        } else {
            instructionView.visibility = View.GONE
        }
    }

    fun updateTheme(imageBackG: CardView , artUri: String , playlistImage: ImageView) {
        Glide.with(activity).load(artUri).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop()).into(playlistImage)

        imageBackG.setCardBackgroundColor(ContextCompat.getColor( activity ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))
    }
}