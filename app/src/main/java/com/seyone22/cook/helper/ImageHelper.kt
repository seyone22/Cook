package com.seyone22.cook.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
            Log.e("ImageHelper", "Error saving image to internal storage", e)
            null
        }
    }

    // Function to retrieve bitmap image from internal storage
    fun loadImageFromInternalStorage(fileName: String): Bitmap? {
        val file = File(context.filesDir, fileName)
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e("ImageHelper", "Error loading image from internal storage", e)
            null
        }
    }

    // Function to retrieve bitmap image from URI
    fun loadImageFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? =
                context.contentResolver.openInputStream(if (uri.isAbsolute) uri else Uri.parse("file://$uri"))
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            Log.e("ImageHelper", "Error loading image from URI", e)
            null
        }
    }

    // Function to delete image from internal storage
    fun deleteImageFromInternalStorage(fileUri: Uri): Boolean {
        val file = File(fileUri.path ?: return false)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
