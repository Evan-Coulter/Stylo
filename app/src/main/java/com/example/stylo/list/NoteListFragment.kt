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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stylo.MainActivity
import com.example.stylo.MainApplication
import com.example.stylo.R
import com.example.stylo.data.RoomNote
import com.example.stylo.editor.NoteEditorViewModel
import com.example.stylo.editor.NoteEditorViewState
import kotlinx.coroutines.launch

class NoteListFragment : Fragment() {
    private val viewModel: NoteListViewModel by viewModels {
        NoteListViewModelFactory((requireActivity().application as MainApplication).notesRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    onNewState(it)
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTextViews(view)
        initButtons(view)
        initRecyclerView(view)
    }

    private fun onNewState(newState : NoteListViewState) {
        when (newState) {
            is NoteListViewState.ShowBasicListState -> showBasicListState(newState.list)
            is NoteListViewState.ShowNoNotesSavedYetPrompt -> showEmptyListState()
            is NoteListViewState.ShowFoldersTray -> showFoldersTrayDialog()
            is NoteListViewState.ShowRenameNoteDialog -> showRenameNoteDiaog()
            is NoteListViewState.ShowEditFolderNameOrColorDialog -> showEditFolderDialog()
        }
    }

    private fun showBasicListState(list: List<RoomNote>) {}
    private fun showEmptyListState() {}
    private fun showFoldersTrayDialog() {}
    private fun showRenameNoteDiaog() {}
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
    }

    private fun initRecyclerView(view: View) {

    }
}