package com.example.stylo.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.R
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder

class NoteListAdapter(
    private val dataset: Array<RoomNote> = listOf(RoomNoteBuilder().setTitle("hello").setContent("world").build()).toTypedArray(),
    private val onClickNote: (Int)->Unit,
    private val onClickNoteEditDetails: (Int)->Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteListRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_list_card_item, parent, false)
        return NoteListRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteListRecyclerViewHolder, position: Int) {
        holder.title.text = dataset[position].title
        holder.content.text = dataset[position].title
        holder.fab.setOnClickListener{onClickNoteEditDetails(dataset[position].uid)}
        holder.view.setOnClickListener {onClickNote(dataset[position].uid)}
    }

    override fun getItemCount(): Int = dataset.size


    class NoteListRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val content: TextView
        val fab: Button
        val view: View
        init {
            title = view.findViewById(R.id.cardViewTitle)
            content = view.findViewById(R.id.cardViewContent)
            fab = view.findViewById(R.id.cardViewFAB)
            this.view = view
        }

    }
}