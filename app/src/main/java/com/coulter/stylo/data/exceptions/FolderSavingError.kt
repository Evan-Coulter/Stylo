package com.coulter.stylo.data.exceptions

const val FOLDER_TITLE_ERROR_MESSAGE = "Please set the folder title."
const val FOLDER_COLOR_ERROR_MESSAGE = "Please choose a folder color."
const val FOLDER_ALREADY_EXISTS_MESSAGE = "There is already a saved folder with that name."
class FolderSavingError(val errorMessage: String) : IllegalArgumentException()