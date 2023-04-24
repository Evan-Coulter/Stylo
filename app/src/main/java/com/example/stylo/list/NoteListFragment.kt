package com.example.stylo.list

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.example.stylo.dialogs.*
import com.example.stylo.editor.NoteEditorFragment
import com.example.stylo.util.ColorStringMap
import com.example.stylo.util.Presets
import com.example.stylo.util.fadeInView
import com.example.stylo.util.fadeOutView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.dionsegijn.konfetti.xml.KonfettiView


class NoteListFragment : Fragment() {
    private var dialogFragment: IDialog? = null
    private val viewModel: NoteListViewModel by viewModels {
        NoteListViewModelFactory((requireActivity().application as MainApplication).notesRepository, activity?.application)
    }

    private lateinit var folderButton: ImageButton
    private lateinit var editFolderDetailsButton: ImageButton
    private lateinit var logoButton: ImageButton
    private lateinit var listCardSwitchButton: ImageButton
    private lateinit var noteList: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var folderTray: View
    private lateinit var title: TextView
    private lateinit var emptyListTitle: TextView
    private lateinit var emptyListHint: TextView
    private lateinit var confetti: KonfettiView

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
            is NoteListViewState.ShowFoldersTray -> showFoldersTray(newState.folders, newState.currentFolder)
            is NoteListViewState.ShowEditNoteDetailsOptions -> {/*Do nothing, is already handled in on click listeners*/}
            is NoteListViewState.ShowEditFolderDialog -> showEditFolderDialog(newState.folder)
            is NoteListViewState.LoadingState -> showLoadingState()
            is NoteListViewState.OpenNoteEditor -> openNoteEditor(newState.note, newState.folder)
            is NoteListViewState.ShowCreateFolderDialog -> openAddNewFolderDialog()
            is NoteListViewState.ShowCreateFolderErrorMessage -> dialogFragment?.iDismiss()
            is NoteListViewState.ShowCreateFolderSuccessMessage -> displayFinishedMessageInDialog()
            is NoteListViewState.ShowEditFolderErrorMessage -> throw NotImplementedError(newState.errorMessage)
            is NoteListViewState.ShowEditFolderSuccessMessage -> displayFinishedMessageInDialog()
            is NoteListViewState.ShowLogoEffect -> showLogoClickEffect()
            is NoteListViewState.ShowRenameNoteErrorMessage -> dialogFragment?.iDismiss()
            is NoteListViewState.ShowEditNoteDetailsSuccessMessage -> displayFinishedMessageInDialog()
            is NoteListViewState.ShowChangeNoteFolderMembershipDialog -> displayChangeFolderMembershipDialog(newState.note, newState.currentFolders, newState.allFolders)
            is NoteListViewState.ShowDeleteNoteDialog -> displayDeleteNoteDialog(newState.note)
            is NoteListViewState.ShowRenameNoteDialog -> displayRenameNoteDialog(newState.note)
            is NoteListViewState.OpenDeleteFolderDialog -> showDeleteFolderDialog(newState.folder)
            is NoteListViewState.ShowEmptySearchResult -> viewModel._eventListener.value = NoteListEvent.PageLoaded
            is NoteListViewState.ShowHelpDialog -> viewModel._eventListener.value = NoteListEvent.PageLoaded
            is NoteListViewState.ShowSearchBar -> viewModel._eventListener.value = NoteListEvent.PageLoaded
        }
    }


    private fun initTextViews(view: View) {
        view.findViewById<TextView>(R.id.title).text = getString(R.string.all_notes_text_string)
    }

    private fun initButtons(view: View) {
        confetti = view.findViewById(R.id.confetti)
        editFolderDetailsButton = view.findViewById(R.id.edit_folder_details_button)
        editFolderDetailsButton.setOnClickListener { openEditFolderDetailsOptions(viewModel.folder, editFolderDetailsButton) }
        logoButton = view.findViewById(R.id.logoButton)
        logoButton.setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.LogoButtonClicked
        }
        title = view.findViewById(R.id.title)
        noteList = view.findViewById(R.id.list)
        emptyListTitle = view.findViewById(R.id.no_notes_are_saved_title)
        emptyListHint = view.findViewById(R.id.no_notes_are_saved_hint)
        folderTray = view.findViewById(R.id.folder_tray)
        folderButton = view.findViewById(R.id.folder)
        folderButton.setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        }
        listCardSwitchButton = view.findViewById(R.id.list_card_switch)
        listCardSwitchButton.setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.CardListViewSwitchClicked
        }
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        }
        view.findViewById<View>(R.id.note_list_fragment).setOnClickListener {
            if (folderTray.isVisible) {
                fadeOutView(requireContext(), folderTray)
                folderTray.visibility = View.GONE
            }
        }
    }

    private fun showBasicListState(list: List<RoomNote>, folder: RoomFolder, isListView: Boolean) {
        title.text = folder.name
        if (list.isEmpty()) {
            emptyListTitle.visibility = View.VISIBLE
            emptyListHint.visibility = View.VISIBLE
        } else {
            emptyListTitle.visibility = View.GONE
            emptyListHint.visibility = View.GONE
        }
        noteList.layoutManager = if (isListView) LinearLayoutManager(context) else GridLayoutManager(context, 2)
        val adapter = NoteListAdapter(
            list.toTypedArray(),
            folder,
            onClickNote = { id -> viewModel._eventListener.value = NoteListEvent.NoteClicked(id) },
            onClickNoteEditDetails = { note, rootView -> openEditNoteDetailsOptions(note, rootView) },
            isListView
        )
        noteList.adapter = adapter
        if (!noteList.isVisible) {
            fadeInView(requireContext(), noteList, 500)
        }
        fab.setColorFilter(Color.WHITE)
        fab.rippleColor = Color.parseColor(ColorStringMap.getLightColor(folder.color))
        fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(ColorStringMap.getColor(folder.color)))
        editFolderDetailsButton.setColorFilter(Color.parseColor(ColorStringMap.getColor(folder.color)), android.graphics.PorterDuff.Mode.SRC_IN)
        folderButton.setColorFilter(Color.parseColor(ColorStringMap.getColor(folder.color)), android.graphics.PorterDuff.Mode.SRC_IN)
        listCardSwitchButton.setColorFilter(Color.parseColor(ColorStringMap.getColor(folder.color)), android.graphics.PorterDuff.Mode.SRC_IN)
        listCardSwitchButton.rotation = if (isListView) -90f else 0f
    }

    private fun showFoldersTray(list: List<RoomFolder>, currentFolder: RoomFolder) {
        folderTray.backgroundTintList = ColorStateList.valueOf((Color.parseColor(ColorStringMap.getLightColor(currentFolder.color))))
        folderTray.setOnClickListener { /*do nothing*/ }
        view?.findViewById<ImageView>(R.id.close_folder_tray_button)?.setOnClickListener { _ ->
            fadeOutView(requireContext(), folderTray)
            folderTray.visibility = View.GONE
        }
        view?.findViewById<Button>(R.id.folder_tray_add_new_folder_button)?.setOnClickListener {
            viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked
        }
        fadeInView(requireContext(), folderTray)
        val recyclerView: RecyclerView = requireView().findViewById(R.id.folder_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = FolderTrayRecyclerViewAdapter(
            list.toTypedArray(),
            currentFolder,
            onClickFolder = { id ->
                viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(id)
                fadeOutView(requireContext(), folderTray)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun openNoteEditor(note: RoomNote, folder: RoomFolder) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
        val editor = NoteEditorFragment(note, folder)
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

    private fun openEditFolderDetailsOptions(folder: RoomFolder, rootView: View) {
        val popup = PopupMenu(context, rootView)
        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.open_edit_folder_details_button -> {
                    viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(folder.uid)
                    popup.dismiss()
                    return@setOnMenuItemClickListener true
                }
                R.id.delete_folder_button -> {
                    viewModel._eventListener.value = NoteListEvent.DeleteFolderButtonClicked(folder.uid)
                    popup.dismiss()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.edit_folder_details_options, popup.menu)
        popup.menu.findItem(R.id.delete_folder_button).isVisible = folder.uid != 1
        popup.show()

    }

    private fun displayRenameNoteDialog(note: RoomNote) {
        context?.let {
            dialogFragment = RenameNoteDialog(note, viewModel.folder) { newNote ->
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
            dialogFragment = ChangeNoteFoldersDialog(note, viewModel.folder, currentFolders, allFolders) { folders ->
                viewModel._eventListener.value = NoteListEvent.AttemptToChangeNoteFolderMembership(note.uid, folders.map {it.uid})
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "change_note_folders_dialog_tag")
        }
    }

    private fun displayDeleteNoteDialog(note: RoomNote) {
        context?.let {
            dialogFragment = DeleteNoteDialog(note, viewModel.folder) { noteID: Int ->
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
        if (folderTray.isVisible) {
            fadeOutView(requireContext(), folderTray)
        }

        viewModel._eventListener.value = NoteListEvent.PageLoaded
    }

    private fun showLoadingState() {
        fadeOutView(requireContext(), noteList)
    }

    private fun showEditFolderDialog(selectedFolder: RoomFolder) {
        context?.let {
            dialogFragment = EditFolderDetailsDialog(selectedFolder, viewModel.folder) { folder: RoomFolder ->
                viewModel._eventListener.value = NoteListEvent.AttemptToEditFolder(folder)
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "edit_folder_details_dialog_tag")
        }
    }

    private fun openAddNewFolderDialog() {
        context?.let {
            dialogFragment = CreateNewFolderDialog(currentFolder = viewModel.folder) { newFolder: RoomFolder ->
                viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(newFolder)
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "create_folder_details_dialog_tag")
        }
    }

    private fun showDeleteFolderDialog(folder: RoomFolder) {
        context?.let {
            dialogFragment = DeleteFolderDialog(folder, currentFolder = viewModel.folder) { folderID: Int ->
                viewModel._eventListener.value = NoteListEvent.AttemptToDeleteFolder(folderID)
            }
            (dialogFragment as DialogFragment).show(parentFragmentManager, "delete_folder_dialog_tag")
        }
    }

    private fun showLogoClickEffect() {
        confetti.start(Presets.rain())
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