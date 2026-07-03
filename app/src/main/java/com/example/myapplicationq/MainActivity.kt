package com.example.myapplicationq

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.myapplicationq.ui.theme.MyApplicationqTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private val isLoading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize TextProvider with application context for DataStore access
        TextProvider.init(this)

        // Schedule periodic work based on user interval preference
        lifecycleScope.launch {
            val repo = SettingsRepository.getInstance(applicationContext)
            val interval = repo.getIntervalMinutes().first() ?: 120
            WorkScheduler.scheduleWallpaperWork(applicationContext, interval, forceUpdate = false)
        }

        setContent {
            MyApplicationqTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var currentText by remember { mutableStateOf("") }
                var intervalText by remember { mutableStateOf("2 jam") }
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                // Refresh state when returning to home or screen initializes
                LaunchedEffect(currentScreen) {
                    currentText = TextProvider.getTimeBasedText(currentHour)
                    val repo = SettingsRepository.getInstance(applicationContext)
                    val minutes = repo.getIntervalMinutes().first() ?: 120
                    intervalText = if (minutes >= 60) {
                        val hours = minutes / 60
                        val rem = minutes % 60
                        if (rem == 0) "$hours jam" else "$hours jam $rem menit"
                    } else {
                        "$minutes menit"
                    }
                }

                if (currentScreen == "home") {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        WallpaperChangerApp(
                            currentText = currentText,
                            intervalText = intervalText,
                            isLoading = isLoading.value,
                            onTriggerUpdate = {
                                triggerImmediateUpdate {
                                    currentText = TextProvider.getTimeBasedText(currentHour)
                                }
                            },
                            onOpenSettings = { currentScreen = "settings" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    SettingsScreen(
                        onNavigateBack = { currentScreen = "home" },
                        onSettingsSaved = {
                            currentScreen = "home"
                            triggerImmediateUpdate {
                                currentText = TextProvider.getTimeBasedText(currentHour)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun triggerImmediateUpdate(onComplete: (() -> Unit)? = null) {
        isLoading.value = true
        val workManager = WorkManager.getInstance(applicationContext)
        val oneTimeRequest = OneTimeWorkRequestBuilder<WallpaperWorker>().build()
        workManager.enqueue(oneTimeRequest)

        workManager.getWorkInfoByIdLiveData(oneTimeRequest.id).observe(this) { workInfo ->
            if (workInfo != null) {
                if (workInfo.state.isFinished) {
                    isLoading.value = false
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(this, "Wallpaper layar kunci berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        onComplete?.invoke()
                    } else {
                        Toast.makeText(this, "Gagal memperbarui wallpaper.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
@Composable
fun WallpaperChangerApp(
    currentText: String,
    intervalText: String,
    isLoading: Boolean,
    onTriggerUpdate: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFormattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentFormattedDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Section with Settings button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp)) // Center alignment spacing helper
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Text Wallpaper",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "diperbarui setiap $intervalText",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Lock Screen Preview
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(400.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(2.dp, Color(0xFF2C2C35), RoundedCornerShape(32.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Date
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentFormattedTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentFormattedDate,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Seeded random styling based on the current text hash code for stable visual styles per text
                val seed = currentText.hashCode().toLong()
                val random = remember(seed) { java.util.Random(seed) }

                val fontFamilies = remember(seed) {
                    arrayOf(FontFamily.Serif, FontFamily.SansSerif, FontFamily.Monospace)
                }
                val chosenFontFamily = remember(seed) {
                    fontFamilies[random.nextInt(fontFamilies.size)]
                }

                val isBold = remember(seed) { random.nextBoolean() }
                val isItalic = remember(seed) { random.nextBoolean() }
                val isUnderline = remember(seed) { random.nextBoolean() }

                val chosenFontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                val chosenFontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                val chosenTextDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None

                // Parse single quoted words to apply Gold color highlight and Bold weight, removing quotes from output
                val annotatedText = remember(currentText) {
                    buildAnnotatedString {
                        val regex = Regex("'(.*?)'")
                        var lastIndex = 0
                        val matches = regex.findAll(currentText)
                        for (match in matches) {
                            val start = match.range.first
                            val end = match.range.last + 1
                            
                            // Normal preceding text
                            append(currentText.substring(lastIndex, start))
                            
                            // Highlight text inside single quotes (omit quotes from display)
                            val innerText = currentText.substring(start + 1, end - 1)
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFFFD700), // Gold
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(innerText)
                            }
                            
                            lastIndex = end
                        }
                        if (lastIndex < currentText.length) {
                            append(currentText.substring(lastIndex))
                        }
                    }
                }

                // Dynamic Wallpaper Text
                Text(
                    text = annotatedText,
                    fontSize = 14.sp,
                    fontFamily = chosenFontFamily,
                    fontWeight = chosenFontWeight,
                    fontStyle = chosenFontStyle,
                    textDecoration = chosenTextDecoration,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Action Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {

            // Trigger Button
            Button(
                onClick = onTriggerUpdate,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ubah Wallpaper Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}