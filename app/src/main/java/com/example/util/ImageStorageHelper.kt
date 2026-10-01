package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "captured_sketches")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = "sketch_${UUID.randomUUID()}.jpg"
        val file = File(dir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }

    suspend fun saveUriToInternalStorage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "captured_sketches")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = "sketch_${UUID.randomUUID()}.jpg"
        val file = File(dir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    }

    suspend fun enhanceSketchBitmap(original: Bitmap, contrastFactor: Float = 1.4f): Bitmap = withContext(Dispatchers.Default) {
        val width = original.width
        val height = original.height
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)

        val paint = Paint()
        // Convert to high-contrast ink lineart matrix
        val cm = ColorMatrix().apply {
            setSaturation(0.2f) // reduce notebook color noise
            // Enhance contrast: scale color channels and adjust brightness
            val scale = contrastFactor
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
            postConcat(ColorMatrix(contrastMatrix))
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(original, 0f, 0f, paint)

        enhanced
    }
}
