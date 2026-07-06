package com.rizalalfadlil.textwall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WallpaperTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = SettingsRepository.getInstance(context.applicationContext)
                    val isEnabled = repository.isChangerEnabled().first()
                    if (isEnabled) {
                        Log.i("WallpaperTriggerReceiver", "Device boot completed. Starting WallpaperTriggerService...")
                        val serviceIntent = Intent(context, WallpaperTriggerService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        Log.i("WallpaperTriggerReceiver", "Device boot completed, but changer is disabled. Skipping service startup.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
