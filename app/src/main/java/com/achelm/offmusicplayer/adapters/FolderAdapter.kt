package com.achelm.offmusicplayer.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.activities.MusicUtility
import com.achelm.offmusicplayer.activities.SongsOfFolderActivity
import com.achelm.offmusicplayer.models.Folder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView

class FolderAdapter(var activity: Activity, private var folderList: ArrayList<Folder>) : RecyclerView.Adapter<FolderAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(activity).inflate(R.layout.folder_card_item , parent , false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = folderList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var folder: Folder = folderList.get(position)
        holder.nameOfolder.text = folder.nameOfFolder
        holder.numberOfItems.text = "${folder.numberOfItems}"

        // Update Theme
        updateTheme(holder.numberBackgound)

        // Load the image of the first song in the folder
        loadFolderImage(folder, holder.folderImage)

        holder.folderBtn.setOnClickListener {
            var intent = Intent(activity , SongsOfFolderActivity::class.java)
            intent.putExtra("FOLDER_PATH" , folder.pathOfFolder)
            intent.putExtra("FOLDER_NAME" , folder.nameOfFolder)
            activity.startActivity(intent)
        }

    }

    inner class MyViewHolder(iv: View): RecyclerView.ViewHolder(iv) {
        var folderBtn: CardView = iv.findViewById(R.id.FolderCardItem_ButtonId)
        var nameOfolder: TextView = iv.findViewById(R.id.FolderCardItem_NameOfFolderId)
        var numberOfItems: TextView = iv.findViewById(R.id.FolderCardItem_NumberOfItemsId)
        var folderImage: CircleImageView = iv.findViewById(R.id.FolderCardItem_folderImageId)
        var numberBackgound: CircleImageView = iv.findViewById(R.id.FolderCardItem_NumberBackgroundId)
    }

    private fun loadFolderImage(folder: Folder, imageView: CircleImageView) {
        val mostRepeatedImageUri = MusicUtility.getMostRepeatedImageInFolder(activity, folder.pathOfFolder)
        if (mostRepeatedImageUri.isNotEmpty()) {
            Glide.with(activity)
                .load(Uri.parse(mostRepeatedImageUri))
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(MainActivity.currentTheme_musicIcon[MainActivity.themeIndex]).centerCrop())
                .into(imageView)
        } else {
            // Set a default image if no songs found in the folder or no image URI available
            imageView.setImageResource(R.drawable.circle_folder_icon)
        }
    }

    fun updateTheme(background: CircleImageView) {
        val color = ContextCompat.getColor( activity ,  MainActivity.currentTheme[MainActivity.themeIndex])
        val circularBitmap = createCircularBitmap(100, color) // Change the size as needed
        background.setImageBitmap(circularBitmap)
    }

    fun createCircularBitmap(size: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return bitmap
    }

}