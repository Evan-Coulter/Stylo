package com.example.stylo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.MainApplication
import com.example.stylo.R
import com.example.stylo.data.model.RoomNote
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoteListFragment : Fragment() {
    private val viewModel: NoteListViewModel by viewModels {
        NoteListViewModelFactory((requireActivity().application as MainApplication).notesRepository, activity?.application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        viewModel.uiState.observe(viewLifecycleOwner) {
            onNewState(it)
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        viewModel._eventListener.value = NoteListEvent.PageLoaded
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTextViews(view)
        initButtons(view)
        initRecyclerView(view)
    }

    private fun onNewState(newState : NoteListViewState) {
        when (newState) {
            is NoteListViewState.ShowBasicListState -> showBasicListState(newState.notes)
            is NoteListViewState.ShowFoldersTray -> showFoldersTrayDialog()
            is NoteListViewState.ShowEditNoteDetailsOptions -> showEditNoteDetailsOptions()
            is NoteListViewState.ShowEditFolderDialog -> showEditFolderDialog()
            is NoteListViewState.LoadingState -> Toast.makeText(context, "Loading TODO", Toast.LENGTH_SHORT).show()
            is NoteListViewState.OpenNoteEditor -> TODO()
            is NoteListViewState.ShowCreateFolderDialog -> TODO()
            is NoteListViewState.ShowCreateFolderErrorMessage -> TODO()
            is NoteListViewState.ShowCreateFolderSuccessMessage -> TODO()
            is NoteListViewState.ShowEditFolderErrorMessage -> TODO()
            is NoteListViewState.ShowEditFolderSuccessMessage -> TODO()
            is NoteListViewState.ShowEmptySearchResult -> TODO()
            is NoteListViewState.ShowHelpDialog -> TODO()
            is NoteListViewState.ShowLogoEffect -> TODO()
            is NoteListViewState.ShowRenameNoteErrorMessage -> TODO()
            is NoteListViewState.ShowRenameNoteSuccessMessage -> TODO()
            is NoteListViewState.ShowSearchBar -> TODO()
        }
    }

    private fun showBasicListState(list: List<RoomNote>) {
        val recyclerView: RecyclerView = requireView().findViewById(R.id.list)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = NoteListAdapter(
            list.toTypedArray(),
            onClickNote = { id -> Toast.makeText(context, "$id note clicked", Toast.LENGTH_SHORT).show() },
            onClickNoteEditDetails = { id -> Toast.makeText(context, "$id note edit details button clicked", Toast.LENGTH_SHORT).show() }
        )
        recyclerView.adapter = adapter
    }

    private fun showFoldersTrayDialog() {}
    private fun showEditNoteDetailsOptions() {}
    private fun showEditFolderDialog() {}


    private fun initTextViews(view: View) {
        view.findViewById<TextView>(R.id.title).text = "All Notes"
    }

    private fun initButtons(view: View) {
        view.findViewById<ImageButton>(R.id.logo).setOnClickListener { Toast.makeText(context, "Logo clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.helpButton).setOnClickListener { Toast.makeText(context, "Help clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.folder).setOnClickListener { Toast.makeText(context, "Folder clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.search).setOnClickListener { Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.list_card_switch).setOnClickListener { Toast.makeText(context, "Item type clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            Toast.makeText(context, "Switch to editor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRecyclerView(view: View) {

    }
}