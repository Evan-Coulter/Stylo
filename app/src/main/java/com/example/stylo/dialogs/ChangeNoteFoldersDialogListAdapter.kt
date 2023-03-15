package com.example.stylo.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder

class ChangeNoteFoldersDialogListAdapter(
    private val currentFolders: Array<RoomFolder>,
    val allFolders: Array<RoomFolder>
) : RecyclerView.Adapter<ChangeNoteFoldersDialogListAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.change_folder_membership_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.folderTitle.text = allFolders[position].name
        holder.checkBox.isChecked = allFolders[position].uid in currentFolders.map { it.uid }
    }

    override fun getItemCount(): Int = allFolders.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox = view.findViewById<CheckBox>(R.id.folder_membership_selection_checkbox)
        val folderTitle = view.findViewById<TextView>(R.id.folder_membership_selection_title)
    }
}