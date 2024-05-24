package com.seyone22.cook.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

class ImageHelper(private val context: Context) {
    // Function to save bitmap image to internal storage
    fun saveImageToInternalStorage(bitmap: Bitmap, fileName: String): String? {
        val fileOutputStream: FileOutputStream
        return try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
            context.filesDir.absolutePath + "/" + fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
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


    fun loadImageFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
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
