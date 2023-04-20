package com.example.stylo.list

import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote
import com.example.stylo.util.ColorStringMap
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoteListAdapter(
    private val dataset: Array<RoomNote>,
    private val folder: RoomFolder,
    private val onClickNote: (Int)->Unit,
    private val onClickNoteEditDetails: (RoomNote, View)->Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteListRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_list_card_item, parent, false)
        return NoteListRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteListRecyclerViewHolder, position: Int) {
        holder.title.text = dataset[position].title
        holder.content.text = Html.fromHtml(dataset[position].content, Html.FROM_HTML_MODE_COMPACT)
        holder.fab.setOnClickListener{onClickNoteEditDetails(dataset[position], holder.fab)}
        holder.fab.setColorFilter(Color.parseColor(ColorStringMap.getColor(folder.color)))
        holder.view.setOnClickListener {onClickNote(dataset[position].uid)}
    }

    override fun getItemCount(): Int = dataset.size


    class NoteListRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val content: TextView
        val fab: FloatingActionButton
        val view: View
        init {
            title = view.findViewById(R.id.cardViewTitle)
            content = view.findViewById(R.id.cardViewContent)
            fab = view.findViewById(R.id.cardViewFAB)
            this.view = view
        }

    }
}