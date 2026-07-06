package com.rizalalfadlil.textwall

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object WorkScheduler {
    private const val WORK_NAME = "WallpaperWorkerUnique"

    /**
     * Triggers a one-time wallpaper update instantly.
     */
    fun triggerOneTimeUpdate(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val workRequest = OneTimeWorkRequestBuilder<WallpaperWorker>().build()
        workManager.enqueue(workRequest)
    }

    /**
     * Cancels any active periodic work to clean up older implementations.
     */
    fun cancelPeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WORK_NAME)
    }
}
