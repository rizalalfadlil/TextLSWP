package com.rizalalfadlil.textwall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class WallpaperTriggerService : Service() {

    private val CHANNEL_ID = "WallpaperTriggerServiceChannel"
    private val NOTIFICATION_ID = 42

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                Log.i("WallpaperTriggerService", "Screen unlock or Screen On detected. Triggering wallpaper update...")
                WorkScheduler.triggerOneTimeUpdate(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("WallpaperTriggerService", "Foreground service created, registering screen/unlock receivers")
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lockscreen Wallpaper Monitor")
            .setContentText("Monitoring screen unlock events to update quotes.")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("WallpaperTriggerService", "Service started command (START_STICKY)")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("WallpaperTriggerService", "Service destroyed, unregistering receivers")
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Lockscreen Monitor",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notification channel for monitoring screen unlock events"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
