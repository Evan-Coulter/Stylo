package com.example.stylo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stylo.MainApplication
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote
import com.example.stylo.dialogs.ChangeNoteFoldersDialog
import com.example.stylo.dialogs.DeleteNoteDialog
import com.example.stylo.dialogs.IDialog
import com.example.stylo.dialogs.RenameNoteDialog
import com.example.stylo.editor.NoteEditorFragment
import com.example.stylo.util.fadeInView
import com.example.stylo.util.fadeOutView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class NoteListFragment : Fragment() {
    private var dialogFragment: IDialog? = null
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
    }

    private fun onNewState(newState : NoteListViewState) {
        when (newState) {
            is NoteListViewState.ShowBasicListState -> showBasicListState(newState.notes, newState.folder, newState.isListView)
            is NoteListViewState.ShowFoldersTray -> showFoldersTray(newState.folders)
            is NoteListViewState.ShowEditNoteDetailsOptions -> {/*Do nothing, is already handled in on click listeners*/}
            is NoteListViewState.ShowEditFolderDialog -> TODO()
            is NoteListViewState.LoadingState -> Toast.makeText(context, "Loading TODO", Toast.LENGTH_SHORT).show()
            is NoteListViewState.OpenNoteEditor -> openNoteEditor(newState.note)
            is NoteListViewState.ShowCreateFolderDialog -> TODO()
            is NoteListViewState.ShowCreateFolderErrorMessage -> TODO()
            is NoteListViewState.ShowCreateFolderSuccessMessage -> TODO()
            is NoteListViewState.ShowEditFolderErrorMessage -> TODO()
            is NoteListViewState.ShowEditFolderSuccessMessage -> TODO()
            is NoteListViewState.ShowEmptySearchResult -> TODO()
            is NoteListViewState.ShowHelpDialog -> TODO()
            is NoteListViewState.ShowLogoEffect -> TODO()
            is NoteListViewState.ShowRenameNoteErrorMessage -> dialogFragment?.iDismiss()
            is NoteListViewState.ShowEditNoteDetailsSuccessMessage -> displayFinishedMessageInDialog()
            is NoteListViewState.ShowChangeNoteFolderMembershipDialog -> displayChangeFolderMembershipDialog(newState.note, newState.currentFolders, newState.allFolders)
            is NoteListViewState.ShowDeleteNoteDialog -> displayDeleteNoteDialog(newState.note)
            is NoteListViewState.ShowRenameNoteDialog -> displayRenameNoteDialog(newState.note)
        }
    }

    private fun initTextViews(view: View) {
        view.findViewById<TextView>(R.id.title).text = "All Notes"
    }

    private fun initButtons(view: View) {
        view.findViewById<ImageButton>(R.id.logo).setOnClickListener { Toast.makeText(context, "Logo clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.helpButton).setOnClickListener { Toast.makeText(context, "Help clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.folder).setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        }
        view.findViewById<ImageButton>(R.id.search).setOnClickListener { Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<ImageButton>(R.id.list_card_switch).setOnClickListener { Toast.makeText(context, "Item type clicked", Toast.LENGTH_SHORT).show() }
        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        }
        view.findViewById<View>(R.id.note_list_fragment).setOnClickListener {
            val folderTray: View = view.findViewById(R.id.folder_tray)
            if (folderTray.isVisible) {
                fadeOutView(requireContext(), folderTray)
                folderTray.visibility = View.GONE
            }
        }
    }

    private fun showBasicListState(list: List<RoomNote>, folder: RoomFolder, isListView: Boolean) {
        view?.findViewById<TextView>(R.id.title)?.text = folder.name
        val recyclerView: RecyclerView = requireView().findViewById(R.id.list)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = NoteListAdapter(
            list.toTypedArray(),
            onClickNote = { id -> viewModel._eventListener.value = NoteListEvent.NoteClicked(id) },
            onClickNoteEditDetails = { note, rootView -> openEditNoteDetailsOptions(note, rootView) }
        )
        recyclerView.adapter = adapter
    }

    private fun showFoldersTray(list: List<RoomFolder>) {
        val folderTray = view?.findViewById<View>(R.id.folder_tray)
        folderTray?.let{
            it.setOnClickListener { /*do nothing*/ }
            view?.findViewById<ImageView>(R.id.close_folder_tray_button)?.setOnClickListener { _ ->
                fadeOutView(requireContext(), it)
                it.visibility = View.GONE
            }
            fadeInView(requireContext(), it)
            val recyclerView: RecyclerView = requireView().findViewById(R.id.folder_list)
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = FolderTrayRecyclerViewAdapter(
                list.toTypedArray(),
                onClickFolder = { id ->
                    viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(id)
                    fadeOutView(requireContext(), folderTray)
                },
                onLongPressFolder = { id -> Toast.makeText(context, "$id folder long pressed", Toast.LENGTH_SHORT).show() }
            )
            recyclerView.adapter = adapter
        }
    }

    private fun openNoteEditor(note: RoomNote) {
        val transaction = parentFragmentManager.beginTransaction()
        val editor = NoteEditorFragment(note)
        transaction.replace(R.id.fragmentContainer, editor).addToBackStack("tag").commit()
    }

    private fun openEditNoteDetailsOptions(note: RoomNote, rootView: View) {
        val popup = PopupMenu(context, rootView)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open_note_editor -> {
                    viewModel._eventListener.value = NoteListEvent.NoteClicked(note.uid)
                    popup.dismiss()
                    return@setOnMenuItemClickListener true
                }
                R.id.rename_note -> {
                    popup.dismiss()
                    viewModel._eventListener.value = NoteListEvent.RenameNoteButtonClicked(note.uid)
                    return@setOnMenuItemClickListener true
                }
                R.id.change_folders -> {
                    popup.dismiss()
                    viewModel._eventListener.value = NoteListEvent.ChangeNoteFolderMembershipButtonClicked(note.uid)
                    return@setOnMenuItemClickListener true
                }
                R.id.delete_note -> {
                    popup.dismiss()
                    viewModel._eventListener.value = NoteListEvent.DeleteNoteButtonClicked(note.uid)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.edit_note_details_options, popup.menu)
        popup.show()
    }

    private fun displayRenameNoteDialog(note: RoomNote) {
        context?.let {
            dialogFragment = RenameNoteDialog(note) { newNote ->
                viewModel._eventListener.value =
                    NoteListEvent.AttemptToRenameNote(newNote)
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "rename_note_dialog_tag")
        }
    }

    private fun displayChangeFolderMembershipDialog(
        note: RoomNote,
        currentFolders: List<RoomFolder>,
        allFolders: List<RoomFolder>
    ) {
        context?.let {
            dialogFragment = ChangeNoteFoldersDialog(note, currentFolders, allFolders) { folders ->
                viewModel._eventListener.value = NoteListEvent.AttemptToChangeNoteFolderMembership(note.uid, folders.map {it.uid})
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "change_note_folders_dialog_tag")
        }
    }

    private fun displayDeleteNoteDialog(note: RoomNote) {
        context?.let {
            dialogFragment = DeleteNoteDialog(note) { noteID: Int ->
                viewModel._eventListener.value = NoteListEvent.AttemptToDeleteNote(noteID)
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "delete_note_dialog_tag")
        }
    }

    private fun displayFinishedMessageInDialog() {
        dialogFragment?.let {
            if (it.iIsAdded() && !it.iIsRemoving()) {
                it.displayFinishedMessage()
            }
        }
        viewModel._eventListener.value = NoteListEvent.PageLoaded
    }

    override fun onDestroy() {
        if (dialogFragment!=null) {
            dialogFragment = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel._eventListener.value = NoteListEvent.PageLoaded
    }
}