package com.achelm.musicplayer.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.musicplayer.LanguageManager
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.adapters.FolderAdapter
import com.achelm.musicplayer.models.Folder
import java.io.File

class FoldersFragment_ofMusicFragment : Fragment() {

    private lateinit var view: View
    private lateinit var recyclerViewOfFolders: RecyclerView
    private lateinit var adapter: FolderAdapter
    private val folderList: ArrayList<Folder> = ArrayList()
    private lateinit var noFoldersFoundId: LinearLayout
    private lateinit var totalFolders: TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        view = inflater.inflate(R.layout.fragment_folders_of_fragment_music , container , false)

        recyclerViewOfFolders = view.findViewById(R.id.foldersFragmentOfMusicFrag_RecyclerViewId)
        noFoldersFoundId = view.findViewById(R.id.foldersFragmentOfMusicFrag_NoFoldersFoundId)

        totalFolders = view.findViewById(R.id.foldersFragmentOfMusicFrag_totalFoldersId)

        // Retrieve folders and populate folderList
        retrieveMusicFolders()

        recyclerViewOfFolders.setHasFixedSize(true)
        var layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViewOfFolders.layoutManager = layoutManager
        adapter = FolderAdapter(requireActivity(), folderList)
        recyclerViewOfFolders.adapter = adapter

        // Animation for RecyclerView
        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerViewOfFolders.layoutAnimation = songListAnimFF

        totalFolders.text  = "${resources.getString(R.string.foldersFragmentOfFragmentMusic_totalFolders)} ${adapter.itemCount}"

        // Set the visibility of noFoldersFoundId based on whether folderList is empty
        if (folderList.isEmpty()) {
            totalFolders.visibility = View.GONE
            noFoldersFoundId.visibility = View.VISIBLE
            recyclerViewOfFolders.visibility = View.GONE
        } else {
            totalFolders.visibility = View.VISIBLE
            noFoldersFoundId.visibility = View.GONE
            recyclerViewOfFolders.visibility = View.VISIBLE
        }

        return view
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun retrieveMusicFolders() {
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val cursor = requireContext().contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null)

        val folderMap = HashMap<String, Int>()
        cursor?.use {
            while (it.moveToNext()) {
                val filePath = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))
                val parentFolder = File(filePath).parentFile
                if (parentFolder != null && parentFolder.isDirectory) {
                    // Get the folder path
                    val folderPath = parentFolder.absolutePath
                    // Combine folder path and folder name
                    folderMap[folderPath] = folderMap.getOrDefault(folderPath, 0) + 1
                }
            }
        }

        folderMap.forEach { (folderPath, songCount) ->
            val parentFolder = File(folderPath)
            val folderName = parentFolder.name
            folderList.add(Folder(folderPath, folderName, songCount.toString()))
        }
    }

    override fun onResume() {
        super.onResume()
        // Notify the adapter that the dataset has changed
        adapter.notifyDataSetChanged()
    }

}