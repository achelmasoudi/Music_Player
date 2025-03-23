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
import com.achelm.musicplayer.R
import com.achelm.musicplayer.activities.MainActivity
import com.achelm.musicplayer.adapters.FolderAdapter
import com.achelm.musicplayer.models.Folder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FoldersFragment_ofMusicFragment : Fragment() {

    private lateinit var view: View
    private lateinit var recyclerViewOfFolders: RecyclerView
    private lateinit var adapter: FolderAdapter
    private val folderList: ArrayList<Folder> = ArrayList()
    private lateinit var noFoldersFoundId: LinearLayout
    private lateinit var totalFolders: TextView

    // Cache for folder list to avoid redundant queries
    companion object {
        private var cachedFolderList: ArrayList<Folder>? = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        view = inflater.inflate(R.layout.fragment_folders_of_fragment_music, container, false)

        recyclerViewOfFolders = view.findViewById(R.id.foldersFragmentOfMusicFrag_RecyclerViewId)
        noFoldersFoundId = view.findViewById(R.id.foldersFragmentOfMusicFrag_NoFoldersFoundId)
        totalFolders = view.findViewById(R.id.foldersFragmentOfMusicFrag_totalFoldersId)

        // Setup RecyclerView
        recyclerViewOfFolders.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViewOfFolders.layoutManager = layoutManager
        adapter = FolderAdapter(requireActivity(), folderList)
        recyclerViewOfFolders.adapter = adapter

        // Animation for RecyclerView
        val songListAnimFF = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_anim))
        songListAnimFF.delay = 0.2f
        songListAnimFF.order = LayoutAnimationController.ORDER_NORMAL
        recyclerViewOfFolders.layoutAnimation = songListAnimFF

        // Load folders asynchronously
        loadFolders()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadFolders() {
        // If cached data exists, use it
        cachedFolderList?.let {
            folderList.clear()
            folderList.addAll(it)
            updateUI()
            return
        }

        // Otherwise, fetch in background
        CoroutineScope(Dispatchers.Main).launch {
            val folders = withContext(Dispatchers.IO) {
                retrieveMusicFolders()
            }
            folderList.clear()
            folderList.addAll(folders)
            cachedFolderList = ArrayList(folders) // Cache the result
            updateUI()
        }
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun retrieveMusicFolders(): ArrayList<Folder> {
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        val folderMap = HashMap<String, Int>()
        cursor?.use {
            while (it.moveToNext()) {
                val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val parentFolder = File(filePath).parentFile
                if (parentFolder != null && parentFolder.isDirectory) {
                    val folderPath = parentFolder.absolutePath
                    folderMap[folderPath] = folderMap.getOrDefault(folderPath, 0) + 1
                }
            }
        }

        val folders = ArrayList<Folder>()
        folderMap.forEach { (folderPath, songCount) ->
            val parentFolder = File(folderPath)
            val folderName = parentFolder.name
            folders.add(Folder(folderPath, folderName, songCount.toString()))
        }
        return folders
    }

    private fun updateUI() {
        totalFolders.text = "${resources.getString(R.string.foldersFragmentOfFragmentMusic_totalFolders)} ${adapter.itemCount}"
        if (folderList.isEmpty()) {
            totalFolders.visibility = View.GONE
            noFoldersFoundId.visibility = View.VISIBLE
            recyclerViewOfFolders.visibility = View.GONE
        } else {
            totalFolders.visibility = View.VISIBLE
            noFoldersFoundId.visibility = View.GONE
            recyclerViewOfFolders.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // Only refresh if cache is invalid (e.g., media changed)
        // For now, just update UI with current data
        updateUI()
    }
}