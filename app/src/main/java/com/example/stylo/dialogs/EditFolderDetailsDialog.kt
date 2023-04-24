package com.example.stylo.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.example.stylo.R
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.util.ColorStringMap
import com.example.stylo.util.fadeInView
import com.example.stylo.util.fadeOutView
import com.google.android.material.textfield.TextInputLayout

class EditFolderDetailsDialog(
    private val selectedFolder: RoomFolder,
    private val currentFolder: RoomFolder,
    private val onSave: (RoomFolder)->Unit
) : IDialog, DialogFragment() {

    private lateinit var title: TextView
    private lateinit var allNotesTitle: TextView
    private lateinit var selectColorMessage: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var savedMessage: LinearLayout
    private lateinit var checkBoxes: List<CheckBox>
    private var newColor = selectedFolder.color

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.edit_folder_dialog_layout, null)
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
        checkBoxes = listOf(
            view.findViewById(R.id.edit_folder_yellow_checkbox),
            view.findViewById(R.id.edit_folder_pink_checkbox),
            view.findViewById(R.id.edit_folder_blue_checkbox),
            view.findViewById(R.id.edit_folder_grey_checkbox),
            view.findViewById(R.id.edit_folder_green_checkbox),
        )
        title = view.findViewById(R.id.edit_folder_title)
        selectColorMessage = view.findViewById(R.id.edit_folder_select_color_message)
        cancelButton = view.findViewById(R.id.edit_folder_cancel_button)
        saveButton = view.findViewById(R.id.edit_folder_save_button)
        cancelButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        saveButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        textInputLayout = view.findViewById(R.id.edit_folder_edit_text)
        textInputLayout.defaultHintTextColor = ColorStateList.valueOf(
            Color.parseColor(
                ColorStringMap.getColor("Grey")))
        textInputLayout.boxStrokeColor = Color.parseColor(ColorStringMap.getColor(currentFolder.color))
        savedMessage = view.findViewById(R.id.edit_folder_saved_message)
        allNotesTitle = view.findViewById(R.id.edit_folder_all_notes_title)
        textInputLayout.editText?.let { it ->
            it.setText(selectedFolder.name)
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
            val newFolderBuilder = RoomFolderBuilder().clone(selectedFolder)
            textInputLayout.editText?.let {
                newFolderBuilder.setName(it.text.toString())
            }
            newFolderBuilder.setColor(newColor)
            onSave(newFolderBuilder.build())
        }
        setupCheckBoxes()
        if (selectedFolder.uid == 1) {
            textInputLayout.visibility = View.INVISIBLE
            textInputLayout.editText?.visibility = View.INVISIBLE
            textInputLayout.isEnabled = false
            textInputLayout.editText?.isEnabled = false
            allNotesTitle.visibility = View.VISIBLE
        }
    }

    private fun setupCheckBoxes() {
        checkBoxes.forEach { checkBox ->
            checkBox.setOnClickListener {
                when(it.id) {
                    R.id.edit_folder_yellow_checkbox -> {
                        enableCheckBox(checkBox, R.drawable.yellow_color_picker_checked_background)
                        newColor = "Yellow"
                    }
                    R.id.edit_folder_pink_checkbox -> {
                        enableCheckBox(checkBox, R.drawable.pink_color_picker_checked_background)
                        newColor = "Pink"
                    }
                    R.id.edit_folder_blue_checkbox -> {
                        enableCheckBox(checkBox, R.drawable.blue_color_picker_checked_background)
                        newColor = "Blue"
                    }
                    R.id.edit_folder_grey_checkbox -> {
                        enableCheckBox(checkBox, R.drawable.grey_color_picker_checked_background)
                        newColor = "Grey"
                    }
                    R.id.edit_folder_green_checkbox -> {
                        enableCheckBox(checkBox, R.drawable.green_color_picker_checked_background)
                        newColor = "Green"
                    }
                }
            }
        }
        when(currentFolder.color){
            "Yellow"->{ view?.findViewById<CheckBox>(R.id.edit_folder_yellow_checkbox)?.let {
                    enableCheckBox(it, R.drawable.yellow_color_picker_checked_background)
            }}
            "Pink"->{ view?.findViewById<CheckBox>(R.id.edit_folder_pink_checkbox)?.let {
                enableCheckBox(it, R.drawable.pink_color_picker_checked_background)
            }}
            "Blue"->{ view?.findViewById<CheckBox>(R.id.edit_folder_blue_checkbox)?.let {
                enableCheckBox(it, R.drawable.blue_color_picker_checked_background)
            }}
            "Grey"->{ view?.findViewById<CheckBox>(R.id.edit_folder_grey_checkbox)?.let {
                enableCheckBox(it, R.drawable.grey_color_picker_checked_background)
            }}
            "Green"->{ view?.findViewById<CheckBox>(R.id.edit_folder_green_checkbox)?.let {
                enableCheckBox(it, R.drawable.green_color_picker_checked_background)
            }}
        }
    }

    private fun enableCheckBox(checkBox: CheckBox, drawableID: Int) {
        checkBox.isChecked = true
        checkBox.setBackgroundResource(drawableID)
        checkBoxes.filter { it.id != checkBox.id }.forEach {
            it.isChecked = false
            when(it.id) {
                R.id.edit_folder_yellow_checkbox -> it.setBackgroundResource(R.drawable.yellow_color_picker_unchecked_background)
                R.id.edit_folder_pink_checkbox -> it.setBackgroundResource(R.drawable.pink_color_picker_unchecked_background)
                R.id.edit_folder_blue_checkbox -> it.setBackgroundResource(R.drawable.blue_color_picker_unchecked_background)
                R.id.edit_folder_grey_checkbox -> it.setBackgroundResource(R.drawable.grey_color_picker_unchecked_background)
                R.id.edit_folder_green_checkbox -> it.setBackgroundResource(R.drawable.green_color_picker_unchecked_background)
            }
        }
    }


    override fun displayFinishedMessage() {
        fadeOutView(requireContext(), title)
        fadeOutView(requireContext(), selectColorMessage)
        if (selectedFolder.uid == 1) {
            fadeOutView(requireContext(), allNotesTitle)
        } else {
            fadeOutView(requireContext(), textInputLayout)
        }
        checkBoxes.forEach {
            fadeOutView(requireContext(), it)
        }
        fadeOutView(requireContext(), cancelButton)
        fadeOutView(requireContext(), saveButton)
        saveButton.postDelayed({
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