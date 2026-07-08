package com.rizalalfadlil.textwall

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class WallpaperWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Initialize TextProvider with applicationContext to avoid UninitializedPropertyException
            TextProvider.init(applicationContext)

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val textToDisplay = TextProvider.getTimeBasedText(currentHour)

            // Dapatkan ukuran layar dari displayMetrics
            val displayMetrics = applicationContext.resources.displayMetrics
            val width = if (displayMetrics.widthPixels > 0) displayMetrics.widthPixels else 1080
            val height = if (displayMetrics.heightPixels > 0) displayMetrics.heightPixels else 1920

            // Generate dan pasang wallpaper
            val bitmap = TextProvider.createTextBitmap(textToDisplay, width, height)
            try {
                setLockScreenWallpaper(applicationContext, bitmap)
            } finally {
                bitmap.recycle()
            }
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure()
        }
    }

    private fun setLockScreenWallpaper(context: Context, bitmap: Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val tempFile = File(context.cacheDir, "temp_wallpaper.jpg")
        val cacheFile = File(context.filesDir, "last_successful_wallpaper.jpg")

        try {
            // 1. Write the bitmap to a temporary JPEG file (compressed, small size)
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // 2. Try to apply it using setStream
            FileInputStream(tempFile).use { fis ->
                wallpaperManager.setStream(fis, null, false, WallpaperManager.FLAG_LOCK)
            }

            // 3. If successful, copy/rename the temp file to the last successful cache file
            try {
                if (tempFile.exists()) {
                    tempFile.copyTo(cacheFile, overwrite = true)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        } catch (t: Throwable) {
            t.printStackTrace()

            // 4. On failure, try to restore the last successful wallpaper from cacheFile
            if (cacheFile.exists()) {
                try {
                    FileInputStream(cacheFile).use { fis ->
                        wallpaperManager.setStream(fis, null, false, WallpaperManager.FLAG_LOCK)
                    }
                } catch (t2: Throwable) {
                    t2.printStackTrace()
                }
            }
            throw t
        } finally {
            // Clean up temporary file
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}