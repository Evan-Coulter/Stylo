package com.example.stylo.dialogs

interface IDialog {
    fun displaySavedMessage()
    fun iDismiss()
    fun iIsAdded(): Boolean
    fun iIsRemoving(): Boolean
}