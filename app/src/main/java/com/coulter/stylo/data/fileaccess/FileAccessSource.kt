package com.coulter.stylo.data.fileaccess

import android.content.Context

/**
 * @throws IOException
 */
@Suppress("KDocUnresolvedReference")
class FileAccessSource (private val context: Context) {
    fun getAllFilesNames() : Array<String> {
        return context.fileList()
    }

    fun deleteFile(fileName: String) {
        context.deleteFile(fileName)
    }

    fun saveFile(fileName: String, contents: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(contents.toByteArray())
        }
    }

    fun getFileContents(fileName: String) : String {
        return context.openFileInput(fileName).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some$text"
            }
        }
    }
}