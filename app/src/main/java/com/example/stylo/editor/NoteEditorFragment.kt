package com.example.stylo.editor

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
import kotlinx.coroutines.launch

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
    }

    private fun onNewState(newState: NoteEditorViewState) {
        when (newState) {
            is NoteEditorViewState.ShowClickHereMessage -> showClickHereState()
            is NoteEditorViewState.ShowFullEditorView -> showFullEditorState()
            is NoteEditorViewState.ShowSavePrompt -> showSaveDialogState()
        }
    }

    private fun showClickHereState() {
        view?.findViewById<TextView>(R.id.textView)?.text = "Click here state."
    }

    private fun showFullEditorState() {
        view?.findViewById<TextView>(R.id.textView)?.text = "Full editor state."
    }

    private fun showSaveDialogState() {
        view?.findViewById<TextView>(R.id.textView)?.text = "Save dialog state."
    }


    private fun initButtons(layout: View) {
        layout.findViewById<Button>(R.id.button1).setOnClickListener {
            viewModel.onEditorClicked()
        }
        layout.findViewById<Button>(R.id.button2).setOnClickListener {
            viewModel.onSaveClicked("hello")
        }
        layout.findViewById<Button>(R.id.button3).setOnClickListener {
            viewModel.onStart()
        }

    }
}