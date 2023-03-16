package com.example.stylo.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder
import com.example.stylo.util.ColorStringMap
import com.example.stylo.util.fadeInView
import com.example.stylo.util.fadeOutView
import com.google.android.material.textfield.TextInputLayout


class RenameNoteDialog(
    private val note: RoomNote,
    private val currentFolder: RoomFolder,
    private val onSave: (RoomNote)->Unit
) : IDialog, DialogFragment() {
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var title: TextView
    private lateinit var savedMessage: LinearLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.rename_note_dialog_layout, null)
            initViews(view)
            builder.setView(view)
            val dialog = builder.create()
            val transparent = ColorDrawable(Color.TRANSPARENT)
            val inset = InsetDrawable(transparent, 100)
            dialog.window?.setBackgroundDrawable(inset)
            return@let dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initViews(view: View) {
        title = view.findViewById(R.id.rename_note_title)
        cancelButton = view.findViewById(R.id.rename_note_cancel_button)
        saveButton = view.findViewById(R.id.rename_note_save_button)
        cancelButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        saveButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        textInputLayout = view.findViewById(R.id.rename_note_edit_text)
        textInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor(ColorStringMap.getColor("Grey")))
        textInputLayout.boxStrokeColor = Color.parseColor(ColorStringMap.getColor(currentFolder.color))
        savedMessage = view.findViewById(R.id.rename_note_saved_message)
        textInputLayout.editText?.let { it ->
            it.setText(note.title)
            it.addTextChangedListener { text ->
                if (text.toString().trim().isEmpty()) {
                    textInputLayout.error = "Please input a title"
                    saveButton.isEnabled = false
                } else {
                    textInputLayout.isErrorEnabled = false
                    saveButton.isEnabled = true
                }
            }
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val newNoteBuilder = RoomNoteBuilder().clone(note)
            textInputLayout.editText?.let {
                newNoteBuilder.setTitle(it.text.toString())
            }
            onSave(newNoteBuilder.build())
        }
    }

    override fun displayFinishedMessage() {
        fadeOutView(requireContext(), title)
        fadeOutView(requireContext(), cancelButton)
        fadeOutView(requireContext(), saveButton)
        fadeOutView(requireContext(), textInputLayout)
        textInputLayout.postDelayed({
            fadeInView(requireContext(), savedMessage)
            savedMessage.postDelayed({
                dismiss()
            }, 800)
        }, 200)
    }

    override fun iDismiss() = dismiss()
    override fun iIsAdded(): Boolean = isAdded
    override fun iIsRemoving(): Boolean = isRemoving
}