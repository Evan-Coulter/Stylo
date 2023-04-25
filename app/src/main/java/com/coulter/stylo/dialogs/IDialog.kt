package com.coulter.stylo.dialogs

interface IDialog {
    fun displayFinishedMessage()
    fun iDismiss()
    fun iIsAdded(): Boolean
    fun iIsRemoving(): Boolean
}