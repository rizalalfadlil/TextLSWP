package com.example.myapplicationq

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.work.Worker
import androidx.work.WorkerParameters
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
            setLockScreenWallpaper(applicationContext, bitmap)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun setLockScreenWallpaper(context: Context, bitmap: Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        try {
            // FLAG_LOCK memastikan gambar hanya diaplikasikan ke layar kunci
            wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}