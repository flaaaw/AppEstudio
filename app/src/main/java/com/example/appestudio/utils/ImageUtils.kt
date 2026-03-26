package com.example.appestudio.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    /**
     * Compresses an image from a Uri into a temporary File.
     * Reduces quality to 70% and scales down if too large.
     */
    fun compressImage(context: Context, uri: Uri, fileName: String = "temp_upload.jpg"): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Scale down if width > 1200
            val scaledBitmap = if (originalBitmap.width > 1200) {
                val ratio = 1200f / originalBitmap.width
                val newHeight = (originalBitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(originalBitmap, 1200, newHeight, true)
            } else {
                originalBitmap
            }

            val outputFile = File(context.cacheDir, fileName)
            val outStream = FileOutputStream(outputFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outStream)
            outStream.flush()
            outStream.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
