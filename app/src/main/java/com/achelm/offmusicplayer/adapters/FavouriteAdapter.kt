package com.achelm.offmusicplayer.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import com.achelm.offmusicplayer.models.Music
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.activities.PlayNextActivity
import com.achelm.offmusicplayer.activities.PlayerActivity
import com.achelm.offmusicplayer.models.formatDuration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class FavouriteAdapter(private val context: Context, private var musicList: ArrayList<Music>, val playNext: Boolean = false) : RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>() {

    class MyViewHolder(i: View) : RecyclerView.ViewHolder(i) {
        val button: CardView = i.findViewById(R.id.favouriteSongsCardItem_buttonId)
        val image: ImageView = i.findViewById(R.id.favouriteSongsCardItem_songImageId)
        val songName: TextView = i.findViewById(R.id.favouriteSongsCardItem_songNameId)
        val songImageBackG: CardView = i.findViewById(R.id.favouriteSongsCardItem_songImage_backgroundId)
        val artistName: TextView = i.findViewById(R.id.favouriteSongsCardItem_artistNameId)
        val songDuration: TextView = i.findViewById(R.id.favouriteSongsCardItem_songDurationId)
        val root: RelativeLayout = i.findViewById(R.id.favouriteSongsCardItem_relativeLayoutId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.favourite_songs_card_item , parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentSong: Music = musicList[position]
        holder.songName.text = currentSong.title
        holder.artistName.text = "${currentSong.artist} - ${currentSong.album}"
        holder.songDuration.text = formatDuration(currentSong.duration)

        // Set current theme
        updateTheme(holder.songImageBackG , musicList[position].artUri , holder.image)

        // When play next music is clicked
        if(playNext){
            holder.button.setOnClickListener {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("index", position)
                intent.putExtra("class", "PlayNext")
                ContextCompat.startActivity(context, intent, null)
            }

            holder.button.setOnLongClickListener {
                var bottomSheetView: View = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout_of_more_in_music_fragment,
                    (context as Activity).findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_container))

                var bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

                // Initialize
                var playNextBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_addToPlayNext_buttonId)
                var aboutSongBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_aboutSong_buttonId)
                var deleteBtn: LinearLayout? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_deleteSong_buttonId)
                var deleteTxtView: TextView? = bottomSheetDialog.findViewById(R.id.bottomSheetLayoutOfMore_inMusicFragment_deleteTxtVId)

                playNextBtn!!.visibility = View.GONE
                aboutSongBtn!!.visibility = View.GONE
                deleteTxtView!!.text = context.resources.getString(R.string.favouriteAdapter_Remove_from_playNext)
                deleteBtn!!.setOnClickListener {
                    if (position == PlayerActivity.songPosition)
                        Snackbar.make( context , holder.root, context.resources.getString(R.string.favouriteAdapter_Cant_remove_currently_playing_Song), Snackbar.LENGTH_SHORT).show()
                    else {
                        if(PlayerActivity.songPosition < position && PlayerActivity.songPosition != 0) --PlayerActivity.songPosition
                        PlayNextActivity.playNextList.removeAt(position)
                        PlayerActivity.musicListPA.removeAt(position)
                        notifyItemRemoved(position)
                    }

                    bottomSheetDialog.dismiss()
                }

                return@setOnLongClickListener true
            }
        }

        else {
            holder.button.setOnClickListener {
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("index", position)
                intent.putExtra("class", "FavouriteAdapter")
                ContextCompat.startActivity(context, intent, null)
            }
        }
    }

    override fun getItemCount(): Int = musicList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFavourites(newList: ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateTheme(imageBackG: CardView , artUri: String , songImage: ImageView) {
        Glide.with(context).load(artUri).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop()).into(songImage)

        imageBackG.setCardBackgroundColor(ContextCompat.getColor( context ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))
    }

}