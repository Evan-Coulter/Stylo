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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coulter.stylo.R
import com.coulter.stylo.data.model.RoomFolder
import com.coulter.stylo.data.model.RoomNote
import com.coulter.stylo.util.ColorStringMap
import com.coulter.stylo.util.fadeInView
import com.coulter.stylo.util.fadeOutView

class ChangeNoteFoldersDialog(
    private val note: RoomNote,
    private val currentFolder: RoomFolder,
    private val currentFolders: List<RoomFolder>,
    private val allFolders: List<RoomFolder>,
    private val onSave: (List<RoomFolder>)->Unit
) : IDialog, DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var title: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var savedMessage: View
    private lateinit var noteName: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.change_folder_membership_dialog_layout, null)
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
        recyclerView = view.findViewById(R.id.change_folder_membership_list)
        title = view.findViewById(R.id.change_folder_membership_title)
        saveButton = view.findViewById(R.id.change_folder_membership_save_button)
        cancelButton = view.findViewById(R.id.change_folder_membership_cancel_button)
        savedMessage = view.findViewById(R.id.change_folder_membership_saved_message)
        cancelButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        saveButton.setTextColor(Color.parseColor(ColorStringMap.getColor(currentFolder.color)))
        noteName = view.findViewById(R.id.change_folder_membership_note_name)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ChangeNoteFoldersDialogListAdapter(
            currentFolders.toTypedArray(),
            currentFolder,
            allFolders.toTypedArray()
        )
        recyclerView.adapter = adapter
        noteName.text = note.title
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val checkedFolderPositions = mutableListOf<Int>()
            for (i in 0..adapter.itemCount) {
                recyclerView.findViewHolderForAdapterPosition(i)?.let {
                    val viewHolder = it as ChangeNoteFoldersDialogListAdapter.ViewHolder
                    if (viewHolder.checkBox.isChecked) {
                        checkedFolderPositions.add(viewHolder.adapterPosition)
                    }
                }
            }
            val newFolders = allFolders.filterIndexed { i, _ -> i in checkedFolderPositions }
            onSave(newFolders)
        }
    }

    override fun displayFinishedMessage() {
        fadeOutView(requireContext(), title)
        fadeOutView(requireContext(), noteName)
        fadeOutView(requireContext(), cancelButton)
        fadeOutView(requireContext(), saveButton)
        fadeOutView(requireContext(), recyclerView)
        recyclerView.postDelayed({
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