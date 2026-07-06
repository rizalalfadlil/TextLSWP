package com.rizalalfadlil.textwall

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.Toast
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.os.PowerManager
import android.content.Context
import android.content.pm.PackageManager

import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
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
import com.rizalalfadlil.textwall.ui.theme.TextWallTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

class MainActivity : ComponentActivity() {
    private val isLoading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize TextProvider with application context for DataStore access
        TextProvider.init(this)

        // Cancel legacy periodic work to clean up older scheduler implementations
        lifecycleScope.launch {
            WorkScheduler.cancelPeriodicWork(applicationContext)
        }

        // Start WallpaperTriggerService Foreground Service to listen for unlock events if enabled
        lifecycleScope.launch {
            val isEnabled = SettingsRepository.getInstance(applicationContext).isChangerEnabled().first()
            if (isEnabled) {
                val serviceIntent = Intent(applicationContext, WallpaperTriggerService::class.java)
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            }
        }

        setContent {
            TextWallTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf("home") }
                var currentText by remember { mutableStateOf("") }
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                // Reactive state for permissions status
                val isNotificationPermissionGranted = remember { mutableStateOf(false) }
                val isBatteryOptimizationIgnored = remember { mutableStateOf(false) }
                val isAccessibilityServiceActive = remember { mutableStateOf(false) }
                var isChangerActive by remember { mutableStateOf(true) }

                // Dynamic permission request launcher for Android 13+
                val notificationLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    isNotificationPermissionGranted.value = isGranted
                }

                // Function to update permission states re-reactively
                val checkSystemPermissions = {
                    isNotificationPermissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    val pm = context.getSystemService(POWER_SERVICE) as PowerManager
                    isBatteryOptimizationIgnored.value = pm.isIgnoringBatteryOptimizations(context.packageName)

                    isAccessibilityServiceActive.value = ScreenOffAccessibilityService.instance != null
                }

                // Refresh state when returning to home or screen initializes
                LaunchedEffect(currentScreen) {
                    currentText = TextProvider.getTimeBasedText(currentHour)
                    checkSystemPermissions()
                    isChangerActive = SettingsRepository.getInstance(context.applicationContext).isChangerEnabled().first()
                }

                val scope = rememberCoroutineScope()

                if (currentScreen == "home") {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        WallpaperChangerApp(
                            currentText = currentText,
                            isChangerActive = isChangerActive,
                            isNotificationGranted = isNotificationPermissionGranted.value,
                            isBatteryIgnored = isBatteryOptimizationIgnored.value,
                            isAccessibilityServiceActive = isAccessibilityServiceActive.value,
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onRequestBatteryOptimizationBypass = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "Cannot open battery settings", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onRequestAccessibilityService = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            onOpenAppSettings = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open app settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onToggleChanger = {
                                val nextState = !isChangerActive
                                scope.launch {
                                    SettingsRepository.getInstance(context.applicationContext).setChangerEnabled(nextState)
                                    val serviceIntent = Intent(context, WallpaperTriggerService::class.java)
                                    if (nextState) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        ContextCompat.startForegroundService(context, serviceIntent)
                                        Toast.makeText(context, "Wallpaper Changer enabled", Toast.LENGTH_SHORT).show()
                                    } else {
                                        context.stopService(serviceIntent)
                                        Toast.makeText(context, "Wallpaper Changer disabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onTurnOffScreen = {
                                // Use Accessibility Service to lock screen
                                if (ScreenOffAccessibilityService.instance != null) {
                                    ScreenOffAccessibilityService.instance?.performGlobalAction(
                                        AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                                } else {
                                    // Prompt user to enable the accessibility service
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
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
                                checkSystemPermissions()
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
                        Toast.makeText(this, "Lockscreen Updated!", Toast.LENGTH_SHORT).show()
                        onComplete?.invoke()
                    } else {
                        Toast.makeText(this, "Failed to Update Lockscreen!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
@Composable
fun WallpaperChangerApp(
    currentText: String,
    isChangerActive: Boolean,
    isNotificationGranted: Boolean,
    isBatteryIgnored: Boolean,
    isAccessibilityServiceActive: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimizationBypass: () -> Unit,
    onRequestAccessibilityService: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onToggleChanger: () -> Unit,
    onTurnOffScreen: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFormattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentFormattedDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Section with Settings button
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),

        ) {
            Text(
                text = "TextWall",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Start,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Dynamic text wallpapers with time-specific quotes to keep you focused and rested.",
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }

        // Lock Screen Preview
        if(isNotificationGranted && isBatteryIgnored && isAccessibilityServiceActive){
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,

            ) {
                Text(
                    text = "preview",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
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


                        val localCtx = LocalContext.current
                        val primaryColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dynamicDarkColorScheme(localCtx).primary
                        } else {
                            Color(0xFFD0BCFF)
                        }
                        // Parse square bracketed words to apply Dark Primary color highlight and Bold weight, removing brackets from output
                        val annotatedText = remember(currentText, primaryColor) {
                            buildAnnotatedString {
                                val regex = Regex("\\[(.*?)\\]")
                                var lastIndex = 0
                                val matches = regex.findAll(currentText)
                                for (match in matches) {
                                    val start = match.range.first
                                    val end = match.range.last + 1

                                    // Normal preceding text
                                    append(currentText.substring(lastIndex, start))

                                    // Highlight text inside square brackets (omit brackets from display)
                                    val innerText = currentText.substring(start + 1, end - 1)
                                    withStyle(
                                        style = SpanStyle(
                                            color = primaryColor,
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
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SystemDiagnosticsCard(
            isNotificationGranted = isNotificationGranted,
            isBatteryIgnored = isBatteryIgnored,
            isAccessibilityServiceActive = isAccessibilityServiceActive,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onRequestBatteryOptimizationBypass = onRequestBatteryOptimizationBypass,
            onRequestAccessibilityService = onRequestAccessibilityService,
            onOpenAppSettings = onOpenAppSettings
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Action Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {

            // Trigger Button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isChangerActive){
                    FilledTonalButton(
                        onClick = onToggleChanger,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Refresh",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Disable",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Button(
                        onClick = onToggleChanger,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Refresh",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = onTurnOffScreen,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Refresh",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screen Off",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
