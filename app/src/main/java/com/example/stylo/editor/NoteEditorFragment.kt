package com.example.stylo.editor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stylo.MainActivity
import com.example.stylo.MainApplication
import com.example.stylo.R
import com.example.stylo.data.RoomNote
import jp.wasabeef.richeditor.RichEditor
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class NoteEditorFragment : Fragment(R.layout.fragment_note_editor) {
    private val viewModel: NoteEditorViewModel by viewModels {
        NoteEditorViewModelFactory(RoomNote(), ((activity as MainActivity).application as MainApplication).notesRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
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
        initEditor(view)
    }

    private fun onNewState(newState: NoteEditorViewState) {
        when (newState) {
            is NoteEditorViewState.ShowClickHereMessage -> showClickHereState()
            is NoteEditorViewState.ShowFullEditorView -> showFullEditorState()
            is NoteEditorViewState.ShowSavePrompt -> showSaveDialogState()
        }
    }

    private fun showClickHereState() {
    }

    private fun showFullEditorState() {
    }

    private fun showSaveDialogState() {
    }


    private fun initButtons(layout: View) {
        val editor = layout.findViewById<RichEditor>(R.id.editor)
        layout.findViewById<Button>(R.id.italic).setOnClickListener { editor.setItalic() }
        layout.findViewById<Button>(R.id.bold).setOnClickListener { editor.setBold() }
        layout.findViewById<Button>(R.id.underline).setOnClickListener { editor.setUnderline() }
        layout.findViewById<Button>(R.id.color).setOnClickListener { editor.setTextColor(Color.GREEN) }
    }

    private fun initEditor(layout: View) {
        layout.findViewById<RichEditor>(R.id.editor).setOnTextChangeListener {
            viewModel.onTextChanged(it)
        }

        //TODO try this
        // layout.findViewById<RichEditor>(R.id.editor).focusEditor()
    }
}