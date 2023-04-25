package com.coulter.stylo.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.coulter.stylo.R
import com.coulter.stylo.data.model.RoomFolder
import com.coulter.stylo.util.ColorStringMap
import com.coulter.stylo.util.fadeInView
import com.coulter.stylo.util.fadeOutView

class DeleteFolderDialog(
    private val folder: RoomFolder,
    private val currentFolder: RoomFolder,
    private val onConfirm: (Int)->Unit
) : IDialog, DialogFragment() {

    private lateinit var title: TextView
    private lateinit var folderName: TextView
    private lateinit var warning: TextView
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var deletedMessage: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.delete_folder_dialog_layout, null)
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
        title = view.findViewById(R.id.delete_folder_title)
        folderName = view.findViewById(R.id.delete_folder_folder_name)
        warning = view.findViewById(R.id.delete_folder_warning)
        deleteButton = view.findViewById(R.id.delete_folder_delete_button)
        cancelButton = view.findViewById(R.id.delete_folder_cancel_button)
        deleteButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        cancelButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        deletedMessage = view.findViewById(R.id.delete_folder_message)
        folderName.text = folder.name
        cancelButton.setOnClickListener {
            dismiss()
        }
        deleteButton.setOnClickListener {
            onConfirm(folder.uid)
        }
    }

    override fun displayFinishedMessage() {
        fadeOutView(requireContext(), title)
        fadeOutView(requireContext(), folderName)
        fadeOutView(requireContext(), warning)
        fadeOutView(requireContext(), cancelButton)
        fadeOutView(requireContext(), deleteButton)
        deleteButton.postDelayed({
            fadeInView(requireContext(), deletedMessage)
            deletedMessage.postDelayed({
                dismiss()
            }, 800)
        }, 200)
    }

    override fun iDismiss() = dismiss()
    override fun iIsAdded(): Boolean = isAdded
    override fun iIsRemoving(): Boolean = isRemoving
}