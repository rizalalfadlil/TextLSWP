package com.example.myapplicationq

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSettingsSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = SettingsRepository.getInstance(context)

    // Boundaries and interval
    var morningStart by remember { mutableStateOf("6") }
    var morningEnd by remember { mutableStateOf("9") }
    var nightStart by remember { mutableStateOf("21") }
    var intervalMinutes by remember { mutableStateOf("120") }
    val minInterval = 15

    // Quote lists
    var morningQuotes by remember { mutableStateOf(emptyList<String>()) }
    var nightQuotes by remember { mutableStateOf(emptyList<String>()) }
    var defaultQuotes by remember { mutableStateOf(emptyList<String>()) }

    var selectedTab by remember { mutableStateOf(0) }
    var newQuoteText by remember { mutableStateOf("") }

    // Load initial values
    LaunchedEffect(Unit) {
        morningStart = (repo.getMorningStart().first() ?: 6).toString()
        morningEnd = (repo.getMorningEnd().first() ?: 9).toString()
        nightStart = (repo.getNightStart().first() ?: 21).toString()
        intervalMinutes = (repo.getIntervalMinutes().first() ?: 120).toString()

        morningQuotes = repo.getMorningQuotes().first()?.toList() ?: TextProvider.DEFAULT_MORNING.toList()
        nightQuotes = repo.getNightQuotes().first()?.toList() ?: TextProvider.DEFAULT_NIGHT.toList()
        defaultQuotes = repo.getDefaultQuotes().first()?.toList() ?: TextProvider.DEFAULT_DEFAULT.toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Wallpaper", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: Jam Batasan (Format 24 Jam)
            Text(
                text = "Batasan Waktu",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val nStart = nightStart.toIntOrNull() ?: 21
            val mEnd = morningEnd.toIntOrNull() ?: 9
            val convertedLeft = if (nStart < 12) nStart + 24 else nStart
            val convertedRight = if (mEnd < 12) mEnd + 24 else mEnd

            val sliderStart = convertedLeft.toFloat().coerceIn(12f, 36f)
            val sliderEnd = maxOf(convertedLeft, convertedRight).toFloat().coerceIn(12f, 36f)

            RangeSlider(
                value = sliderStart..sliderEnd,
                valueRange = 12f..36f,
                onValueChange = { range ->
                    nightStart = (range.start.toInt() % 24).toString()
                    morningEnd = (range.endInclusive.toInt() % 24).toString()
                },
            )
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Mulai Malam Jam $nightStart")
                Text(text = "Selesai Pagi Jam $morningEnd",)
            }
            val mStart = morningStart.toIntOrNull() ?: 6
            val convertedMorningStart = if (mStart < 12) mStart + 24 else mStart

            // Pastikan startMin < endMax untuk menghindari exception pada Slider
            val startMin = sliderStart.coerceIn(12f, 35f)
            val endMax = maxOf(startMin + 1f, sliderEnd).coerceIn(12f, 36f)
            val sliderMorningStart = convertedMorningStart.toFloat().coerceIn(startMin, endMax)

            Slider(
                value = sliderMorningStart,
                valueRange = startMin..endMax,
                onValueChange = { newValue ->
                    morningStart = (newValue.toInt() % 24).toString()
                }
            )
            Text(text = "Mulai Pagi Jam $morningStart")
            Spacer(modifier = Modifier.height(20.dp))
            // Section 2: Durasi Pembaruan (Interval)
            Text(
                text = "Interval Pembaruan Otomatis",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(

                value = intervalMinutes.toFloat(),
                steps = 24,
                onValueChange = { intervalMinutes = it.toInt().toString() },
                valueRange = minInterval.toFloat()..1440f,
            )
            val intervalText = if (intervalMinutes.toInt() >= 60) {
                val hours = intervalMinutes.toInt() / 60
                val rem = intervalMinutes.toInt() % 60
                if (rem == 0) "$hours jam" else "$hours jam $rem menit"
            } else {
                "$intervalMinutes menit"
            }
            Text(
                text = "Perbarui setiap $intervalText",
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Edit Kata-Kata (Quotes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Kata-Kata / Quotes",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Reset Button
                TextButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> morningQuotes = TextProvider.DEFAULT_MORNING.toList()
                            1 -> nightQuotes = TextProvider.DEFAULT_NIGHT.toList()
                            2 -> defaultQuotes = TextProvider.DEFAULT_DEFAULT.toList()
                        }
                        Toast.makeText(context, "Kategori ini dikembalikan ke default!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kembalikan Default", fontSize = 13.sp)
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; newQuoteText = "" }) {
                    Text("Pagi", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; newQuoteText = "" }) {
                    Text("Malam", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2; newQuoteText = "" }) {
                    Text("Siang", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Quote Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newQuoteText,
                    onValueChange = { newQuoteText = it },
                    placeholder = { Text("Tambah quotes baru...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (newQuoteText.trim().isNotEmpty()) {
                            when (selectedTab) {
                                0 -> morningQuotes = morningQuotes + newQuoteText.trim()
                                1 -> nightQuotes = nightQuotes + newQuoteText.trim()
                                2 -> defaultQuotes = defaultQuotes + newQuoteText.trim()
                            }
                            newQuoteText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LazyColumn replacement for nested scrolling inside verticalScroll
            val currentList = when (selectedTab) {
                0 -> morningQuotes
                1 -> nightQuotes
                else -> defaultQuotes
            }

            if (currentList.isEmpty()) {
                Text(
                    text = "Belum ada kutipan. Silakan tambah di atas.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            } else {
                currentList.forEachIndexed { index, quote ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = quote,
                                onValueChange = { newText ->
                                    val updated = currentList.toMutableList().apply {
                                        this[index] = newText
                                    }
                                    when (selectedTab) {
                                        0 -> morningQuotes = updated
                                        1 -> nightQuotes = updated
                                        2 -> defaultQuotes = updated
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val updated = currentList.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    when (selectedTab) {
                                        0 -> morningQuotes = updated
                                        1 -> nightQuotes = updated
                                        2 -> defaultQuotes = updated
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button: Save
            Button(
                onClick = {
                    val mStart = morningStart.toIntOrNull()
                    val mEnd = morningEnd.toIntOrNull()
                    val nStart = nightStart.toIntOrNull()
                    val interval = intervalMinutes.toIntOrNull()

                    if (mStart == null || mStart !in 0..23 ||
                        mEnd == null || mEnd !in 0..23 ||
                        nStart == null || nStart !in 0..23
                    ) {
                        Toast.makeText(context, "Jam harus berupa angka antara 0 dan 23!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (interval == null || interval < minInterval) {
                        Toast.makeText(context, "Interval pembaruan minimal $minInterval menit!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    scope.launch {
                        repo.setMorningStart(mStart)
                        repo.setMorningEnd(mEnd)
                        repo.setNightStart(nStart)
                        repo.setIntervalMinutes(interval)

                        repo.setMorningQuotes(morningQuotes)
                        repo.setNightQuotes(nightQuotes)
                        repo.setDefaultQuotes(defaultQuotes)

                        // Reschedule background work using WorkScheduler
                        WorkScheduler.scheduleWallpaperWork(context, interval, forceUpdate = true)

                        Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        onSettingsSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Simpan Pengaturan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
