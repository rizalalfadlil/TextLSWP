package com.rizalalfadlil.textwall

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SystemDiagnosticsCard(
    isNotificationGranted: Boolean,
    isBatteryIgnored: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimizationBypass: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Show only if at least one permission is missing
    if (isNotificationGranted && isBatteryIgnored) {
        return
    }

    var isActionPerformed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Required Permissions",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Restart the application if nothing changes.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            if(isActionPerformed){
                OutlinedButton(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp),
                    onClick = {
                        // Restart the app by scheduling a PendingIntent to relaunch MainActivity
                        val launchIntent = context.packageManager
                            .getLaunchIntentForPackage(context.packageName)
                            ?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                        if (launchIntent != null) {
                            val pendingIntent = PendingIntent.getActivity(
                                context, 0, launchIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            pendingIntent.send()
                        }
                        // Kill the current process to fully restart
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) {
                    Text(text="Restart Now",fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

            // Notification permission item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Notification Permission",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isNotificationGranted) "Completed" else "Need Action",
                        fontSize = 11.sp,
                        color = if (isNotificationGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
                if (!isNotificationGranted) {
                    Button(
                        onClick = { onRequestNotificationPermission(); isActionPerformed = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Allow", fontSize = 11.sp)
                    }
                }
            }

            // Battery optimization item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Disable Battery Optimization",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isBatteryIgnored) "Completed" else "Need Action",
                        fontSize = 11.sp,
                        color = if (isBatteryIgnored) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
                if (!isBatteryIgnored) {
                    Button(
                        onClick = {onRequestBatteryOptimizationBypass(); isActionPerformed = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Disable Limits", fontSize = 11.sp)
                    }
                }
            }

            // Manufacturer specific warning link
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Autostart settings (Xiaomi, Oppo, etc.)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onOpenAppSettings,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Open App Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}