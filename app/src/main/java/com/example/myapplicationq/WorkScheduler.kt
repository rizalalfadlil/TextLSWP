package com.example.myapplicationq

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val WORK_NAME = "WallpaperWorkerUnique"

    /**
     * Schedules or reschedules the wallpaper periodic work with the given interval.
     * Uses ExistingPeriodicWorkPolicy.UPDATE to update an existing task, or falls back to
     * scheduling a new one if it does not exist.
     */
    fun scheduleWallpaperWork(context: Context, intervalMinutes: Int, forceUpdate: Boolean = false) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val minutes = intervalMinutes.toLong().coerceAtLeast(15) // WorkManager limit is 15 minutes

        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(minutes, TimeUnit.MINUTES)
            .build()

        val policy = if (forceUpdate) {
            ExistingPeriodicWorkPolicy.UPDATE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            policy,
            workRequest
        )
    }
}
