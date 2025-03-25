package com.achelm.offmusicplayer.fragments

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelm.offmusicplayer.models.MusicPlaylist
import com.achelm.offmusicplayer.models.Playlist
import com.achelm.offmusicplayer.adapters.PlaylistAdapter
import com.achelm.offmusicplayer.R
import com.achelm.offmusicplayer.activities.MainActivity
import com.achelm.offmusicplayer.activities.SearchActivity
import com.achelm.offmusicplayer.activities.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.ArrayList

class PlaylistFragment : Fragment() {

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    private lateinit var fView: View
    private lateinit var root: LinearLayout
    private lateinit var adapter: PlaylistAdapter
    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var addPlaylistBtn: CardView
    private lateinit var instruction: LinearLayout
    private lateinit var toolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().setTheme(MainActivity.currentTheme_activity[MainActivity.themeIndex])

        fView = inflater.inflate(R.layout.fragment_playlist , container , false)

        // Initialize variables
        root = fView.findViewById(R.id.playlistFragment_linearLayoutRootId)
        playlistRecyclerView = fView.findViewById(R.id.playlistFragment_recyclerViewId)
        addPlaylistBtn = fView.findViewById(R.id.playlistFragment_addPlaylistBtnId)
        instruction = fView.findViewById(R.id.playlistFragment_instructionId)
        toolbar = fView.findViewById(R.id.playlistFragment_toolBarId)

        // Set search in Toolbar
        toolbar.inflateMenu(R.menu.menu_off)

        // Handle menu item clicks
        itemsOfToolbar()

        playlistRecyclerView.setHasFixedSize(true)
        playlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlaylistAdapter(requireActivity(), playlistList = musicPlaylist.ref , instruction)
        playlistRecyclerView.adapter = adapter

        addPlaylistBtn.setOnClickListener { customAlertDialog() }

        updateInstructionVisibility()

        // Set current theme
        updateTheme()

        return fView
    }

    private fun updateInstructionVisibility() {
        if (adapter.itemCount == 0) {
            instruction.visibility = View.VISIBLE
        }
        else {
            instruction.visibility = View.GONE
        }
    }

    private fun customAlertDialog(){
        var builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        var view: View = requireActivity().layoutInflater.inflate(R.layout.alert_dialog_of_add_playlist , null)

        view.background = ContextCompat.getDrawable(requireContext() , R.drawable.background_of_alert_dialog)

        // Initialize the variables
        var playlistNameEditTxt : TextInputLayout = view.findViewById(R.id.customAlertDialogOf_addPlaylist_playlistName_textInputLayoutId)
        var saveBtn: CardView = view.findViewById(R.id.customAlertDialogOf_addPlaylist_saveButtonId)
        var cancelBtn: CardView = view.findViewById(R.id.customAlertDialogOf_addPlaylist_cancelButtonId)
        var playlistIcon: ImageView = view.findViewById(R.id.customAlertDialogOf_addPlaylist_ImageViewId)
        builder.setView(view)
        var dialog: AlertDialog = builder.create()
        // Set the background of the entire dialog to transparent
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        // Set theme
        playlistIcon.setColorFilter(ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]), PorterDuff.Mode.SRC_IN)
        saveBtn.setCardBackgroundColor( ContextCompat.getColor(requireContext() , MainActivity.currentTheme[MainActivity.themeIndex]) )
        val colorStateList = ColorStateList (
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
            intArrayOf(
                ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]), // Color when focused
                ContextCompat.getColor(requireContext(), android.R.color.white) // Default color
            )
        )
        playlistNameEditTxt.setBoxStrokeColorStateList(colorStateList)

        // for Saving the playlist
        saveBtn.setOnClickListener {
            var playlistName: String = playlistNameEditTxt.editText!!.text.toString()

            if(playlistName.isEmpty() && playlistName.isNotBlank()) {
                Snackbar.make(root, resources.getString(R.string.playlistFragment_Please_enter_your_playlist_name), Snackbar.LENGTH_SHORT).show()
            }
            else {

                addPlaylist(playlistName)

                // Hide the keyboard
                val inputMethodManager: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(saveBtn.windowToken, 0)

                // Dismiss the dialog
                dialog.dismiss()
            }
        }

        // for Canceling
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

    }
    private fun addPlaylist(name: String){
        var playlistExists = false
        for(i in musicPlaylist.ref) {
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if (playlistExists)
            Snackbar.make(root, resources.getString(R.string.playlistFragment_Playlist_exist), Snackbar.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            tempPlaylist.createdOn = sdf.format(calendar.time)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
            updateInstructionVisibility()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        updateInstructionVisibility()

        updateTheme()

    }

    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.searchViewId -> {
                    // Handle search item click
                    var intent = Intent(requireContext() , SearchActivity::class.java)
                    requireContext().startActivity(intent)
                    true
                }
                R.id.settingsId -> {
                    var intent = Intent(requireContext() , SettingsActivity::class.java)
                    requireContext().startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateTheme() {
        toolbar.setBackgroundColor( ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )
        addPlaylistBtn.setCardBackgroundColor( ContextCompat.getColor(requireContext(), MainActivity.currentTheme[MainActivity.themeIndex]) )
    }
}