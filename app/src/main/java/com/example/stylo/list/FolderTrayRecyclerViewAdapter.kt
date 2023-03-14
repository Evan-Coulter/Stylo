package com.example.stylo.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder

class FolderTrayRecyclerViewAdapter(
    private val folders: Array<RoomFolder>,
    private val onClickFolder: (Int)->Unit,
    private val onLongPressFolder: (Int)->Unit
) : RecyclerView.Adapter<FolderTrayRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_tray_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.folderButton.setOnClickListener {
            onClickFolder(folders[position].uid)
        }
        holder.folderButton.setOnLongClickListener {
            onLongPressFolder(folders[position].uid)
            true
        }
        holder.folderButton.text = folders[position].name
        //holder.folderButton.setBackgroundColor()
    }

    override fun getItemCount(): Int = folders.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderButton = view.findViewById<Button>(R.id.folder_tray_button)
    }
}