package com.example.myapplicationq

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SystemDiagonisticsCard(
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

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Diagnostik & Izin Sistem",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Aplikasi membutuhkan izin berikut agar wallpaper berganti dengan lancar di latar belakang.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

            // Notification permission item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Izin Notifikasi",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isNotificationGranted) "🟢 Aktif (Diizinkan)" else "⚠️ Dinonaktifkan (Wajib untuk Service)",
                        fontSize = 11.sp,
                        color = if (isNotificationGranted) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                if (!isNotificationGranted) {
                    Button(
                        onClick = onRequestNotificationPermission,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Izinkan", fontSize = 11.sp)
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
                        text = "Penghemat Baterai",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isBatteryIgnored) "🟢 Tidak Dibatasi (Lancar)" else "⚠️ Dioptimalkan (Aplikasi Bisa Mati)",
                        fontSize = 11.sp,
                        color = if (isBatteryIgnored) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                if (!isBatteryIgnored) {
                    Button(
                        onClick = onRequestBatteryOptimizationBypass,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Matikan Batasan", fontSize = 11.sp)
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
                    text = "Pengaturan Mulai Otomatis (Xiaomi, Oppo, dll.)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onOpenAppSettings,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Buka Info Aplikasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}