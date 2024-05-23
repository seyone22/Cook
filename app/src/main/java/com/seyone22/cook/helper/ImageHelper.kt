package com.seyone22.cook.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class ImageHelper(private val context: Context) {
    // Function to save bitmap image to internal storage
    fun saveImageToInternalStorage(bitmap: Bitmap, fileName: String) {
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to retrieve bitmap image from internal storage
    fun loadImageFromInternalStorage(fileName: String): Bitmap? {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            return try {
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    // Function to delete image from internal storage
    fun deleteImageFromInternalStorage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
