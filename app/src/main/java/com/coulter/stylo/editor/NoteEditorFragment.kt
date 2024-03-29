package com.coulter.stylo.editor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.coulter.stylo.MainApplication
import com.coulter.stylo.R
import com.coulter.stylo.data.model.RoomFolder
import com.coulter.stylo.data.model.RoomNote
import com.coulter.stylo.util.ColorStringMap
import jp.wasabeef.richeditor.RichEditor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoteEditorFragment(private val note: RoomNote, private val folder: RoomFolder) : Fragment() {
    private lateinit var editor: RichEditor
    private lateinit var italic: ImageButton
    private lateinit var bold: ImageButton
    private lateinit var underline: ImageButton
    private lateinit var strikeThrough: ImageButton
    private lateinit var bullets: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var formatButtonLayout: LinearLayout
    private lateinit var noteTitle: EditText
    private lateinit var lastEditedTime: TextView

    private val viewModel: NoteEditorViewModel by viewModels {
        NoteEditorViewModelFactory(
            note,
            folder,
            (requireActivity().application as MainApplication).notesRepository
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_editor, container, false)
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
        initButtons(view)
        initEditor()
    }

    private fun onNewState(newState: NoteEditorViewState) {
        when (newState) {
            is NoteEditorViewState.ShowBasicEditorScreen -> showFullEditorState(newState.note, newState.folder)
            is NoteEditorViewState.ShowNoteUpdatedState -> showNoteUpdatedState(newState.note)
            is NoteEditorViewState.ShowSetTitleState -> showSetTitleState()
        }
    }

    private fun initButtons(layout: View) {
        noteTitle = layout.findViewById(R.id.title)
        lastEditedTime = layout.findViewById(R.id.last_edited_date)
        editor = layout.findViewById(R.id.editor)
        italic = layout.findViewById<ImageButton>(R.id.italic).also { it.setOnClickListener { editor.setItalic() }}
        bold = layout.findViewById<ImageButton>(R.id.bold).also{ it.setOnClickListener { editor.setBold() }}
        underline = layout.findViewById<ImageButton>(R.id.underline).also{ it.setOnClickListener { editor.setUnderline() }}
        strikeThrough = layout.findViewById<ImageButton>(R.id.strikethrough).also { it.setOnClickListener { editor.setStrikeThrough() } }
        bullets = layout.findViewById<ImageButton>(R.id.bullets).also{ it.setOnClickListener { editor.setBullets() } }
        formatButtonLayout = layout.findViewById<LinearLayout>(R.id.format_buttons).also { it.visibility = View.GONE; it.isEnabled = false }
        backButton = layout.findViewById<ImageButton>(R.id.back_button).also{ it.setOnClickListener {
            activity?.onBackPressed()
        }}
    }


    private fun initEditor() {
        editor.setOnTextChangeListener {
            viewModel._eventListener.value = NoteEditorEvent.NoteContentEdited(it)
        }
        editor.setOnDecorationChangeListener { text, types ->
            formatButtonLayout.let {
                it.visibility = View.VISIBLE
                it.isEnabled = true
            }
        }
        noteTitle.addTextChangedListener {
            viewModel._eventListener.value = NoteEditorEvent.NoteTitleEdited(it.toString())
        }
    }

    private fun showFullEditorState(note: RoomNote, folder: RoomFolder) {
        listOf(bold, underline, italic, backButton, strikeThrough, bullets).forEach {
            it.setColorFilter(Color.parseColor(ColorStringMap.getColor(folder.color)), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        editor.html = note.content
        val format = SimpleDateFormat("MMMM d, yyyy 'at' h:mma", Locale.US)
        lastEditedTime.text = format.format(note.dateLastSaved)
        noteTitle.setText(note.title)
    }

    private fun showNoteUpdatedState(note: RoomNote) {
        lastEditedTime.text = SimpleDateFormat("MMMM d, yyyy 'at' h:mma", Locale.US)
            .format(note.dateLastSaved)
    }

    private fun showSetTitleState() {
        context?.let { Toast.makeText(it, "Save Dialog", Toast.LENGTH_SHORT).show() }
    }


    override fun onResume() {
        super.onResume()
        viewModel._eventListener.value = NoteEditorEvent.EditorLoaded
    }

    override fun onPause() {
        viewModel._eventListener.value = NoteEditorEvent.EditorClosed(editor.html, noteTitle.text.toString())
        super.onPause()
    }
}