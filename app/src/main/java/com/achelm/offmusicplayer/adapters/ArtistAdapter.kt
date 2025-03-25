package com.achelm.offmusicplayer.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.activities.SongsOfArtistActivity
import com.achelm.offmusicplayer.models.Artist

class ArtistAdapter(var activity: Activity, private var artistList: ArrayList<Artist>) : RecyclerView.Adapter<ArtistAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.artist_card_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = artistList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val artist = artistList[position]
        holder.artistName.text = artist.artistName
        holder.numberOfArtistSongs.text = "${artist.numberOfArtistSongs} ${activity.resources.getString(R.string.artistAdapter_songs)}"

        holder.artistCard.setOnClickListener {
            val intent = Intent(activity, SongsOfArtistActivity::class.java)
            intent.putExtra("ARTIST_NAME", artist.artistName)
            activity.startActivity(intent)
        }

        updateTheme(holder.backgroundImage , holder.artistIcon)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistCard: CardView = itemView.findViewById(R.id.artistCardItem_ButtonId)
        val artistName: TextView = itemView.findViewById(R.id.artistCardItem_NameOfFolderId)
        val numberOfArtistSongs: TextView = itemView.findViewById(R.id.artistCardItem_NumberOfItemsId)
        val backgroundImage: CardView = itemView.findViewById(R.id.artistCardItem_backGround_CardViewId)
        val artistIcon: ImageView = itemView.findViewById(R.id.artistCardItem_IconId)
    }

    fun updateTheme(imageBackG: CardView , artistIcon: ImageView) {

        val artistImg: Drawable = MainActivity.currentTheme_artistIcon[MainActivity.themeIndex]!!
        artistIcon.setImageDrawable(artistImg)

        imageBackG.setCardBackgroundColor(ContextCompat.getColor( activity ,  MainActivity.currentTheme_backGIcon[MainActivity.themeIndex] ))

    }

}