package com.example.myapplicationq

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSettingsSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = SettingsRepository.getInstance(context)

    // Boundaries
    var morningStart by remember { mutableStateOf(TextProvider.DEFAULT_MORNING_START.toString()) }
    var morningEnd by remember { mutableStateOf(TextProvider.DEFAULT_MORNING_END.toString()) }
    var nightStart by remember { mutableStateOf(TextProvider.DEFAULT_NIGHT_START.toString()) }

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

        morningQuotes = repo.getMorningQuotes().first()?.toList() ?: TextProvider.DEFAULT_MORNING.toList()
        nightQuotes = repo.getNightQuotes().first()?.toList() ?: TextProvider.DEFAULT_NIGHT.toList()
        defaultQuotes = repo.getDefaultQuotes().first()?.toList() ?: TextProvider.DEFAULT_DEFAULT.toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallpaper Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val mStart = morningStart.toIntOrNull()
                            val mEnd = morningEnd.toIntOrNull()
                            val nStart = nightStart.toIntOrNull()

                            if (mStart == null || mStart !in 0..23 ||
                                mEnd == null || mEnd !in 0..23 ||
                                nStart == null || nStart !in 0..23
                            ) {
                                Toast.makeText(context, "Hours must be a number between 0 and 23!", Toast.LENGTH_LONG).show()
                                return@IconButton
                            }

                            scope.launch {
                                repo.setMorningStart(mStart)
                                repo.setMorningEnd(mEnd)
                                repo.setNightStart(nStart)

                                repo.setMorningQuotes(morningQuotes)
                                repo.setNightQuotes(nightQuotes)
                                repo.setDefaultQuotes(defaultQuotes)

                                // Trigger an immediate update of the wallpaper with the new settings/quotes
                                WorkScheduler.triggerOneTimeUpdate(context)

                                Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                                onSettingsSaved()
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save")

                    }
                }

            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Section 1: Jam Batasan (Format 24 Jam)
            Text(
                text = "Time Boundaries",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val minRangeHours = 3 // Minimum 2-hour gap between nightStart and morningEnd

            val nStart = nightStart.toIntOrNull() ?: 20
            val mEnd = morningEnd.toIntOrNull() ?: 8
            val convertedLeft = if (nStart < 12) nStart + 24 else nStart
            val convertedRight = if (mEnd < 12) mEnd + 24 else mEnd

            val sliderStart = convertedLeft.toFloat().coerceIn(13f, 35f - minRangeHours)
            val sliderEnd = maxOf(convertedLeft + minRangeHours, convertedRight).toFloat().coerceIn(13f + minRangeHours, 35f)

            RangeSlider(
                value = sliderStart..sliderEnd,
                valueRange = 13f..35f,
                onValueChange = { range ->
                    var newStart = range.start.toInt()
                    var newEnd = range.endInclusive.toInt()

                    // Enforce minimum gap between thumbs
                    if (newEnd - newStart < minRangeHours) {
                        // Determine which thumb moved and adjust the other
                        val oldStart = convertedLeft
                        if (newStart != oldStart) {
                            // Left thumb moved → push right
                            newEnd = (newStart + minRangeHours).coerceAtMost(35)
                            // If right hit the wall, push left back
                            newStart = (newEnd - minRangeHours).coerceAtLeast(13)
                        } else {
                            // Right thumb moved → push left
                            newStart = (newEnd - minRangeHours).coerceAtLeast(13)
                            // If left hit the wall, push right back
                            newEnd = (newStart + minRangeHours).coerceAtMost(35)
                        }
                    }

                    nightStart = (newStart % 24).toString()
                    morningEnd = (newEnd % 24).toString()

                    // Clamp morningStart to stay within the new valid boundary
                    val mStartCurrent = morningStart.toIntOrNull() ?: 3
                    val convertedMStart = if (mStartCurrent < 12) mStartCurrent + 24 else mStartCurrent
                    val clampedMStart = convertedMStart.coerceIn(newStart + 1, newEnd - 1)
                    morningStart = (clampedMStart % 24).toString()
                },
            )

            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Night Starts at $nightStart:00")
                Text(text = "Morning Ends at $morningEnd:00",)
            }
            val mStart = morningStart.toIntOrNull() ?: 3
            val convertedMorningStart = if (mStart < 12) mStart + 24 else mStart

            // Compute safe bounds for the morningStart slider (at least 1 hour inside the range)
            val morningSliderMin = sliderStart + 1f
            val morningSliderMax = sliderEnd - 1f
            // Only render slider if there's a valid range (at least 2-hour gap guarantees this)
            val sliderMorningStart = convertedMorningStart.toFloat().coerceIn(morningSliderMin, morningSliderMax)

            Slider(
                value = sliderMorningStart,
                valueRange = morningSliderMin..morningSliderMax,
                onValueChange = { newValue ->
                    morningStart = (newValue.toInt() % 24).toString()
                }
            )
            Text(text = "Morning Starts at $morningStart:00")
            Spacer(modifier = Modifier.height(20.dp))
            // Section 2: Pemicu Pembaruan (Trigger)
            Text(
                text = "Current Time",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1:00",color = MaterialTheme.colorScheme.primary)
                Text(text = "23:00", color = MaterialTheme.colorScheme.primary)
        }
            LinearProgressIndicator(
                progress = currentHour.toFloat() / 23f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentTime = TextProvider.getCurrentTimeName(currentHour)
                Text(text = "Morning", color = if (currentTime != "MORNING") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                Text(text = "Daytime", color = if (currentTime != "DEFAULT") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                Text(text = "Night", color = if (currentTime != "NIGHT") MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Edit Kata-Kata (Quotes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quote Lists",
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
                        Toast.makeText(context, "Category reset to default!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 13.sp)
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
                    Text("Morning", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; newQuoteText = "" }) {
                    Text("Night", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2; newQuoteText = "" }) {
                    Text("Daytime", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
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
                    placeholder = { Text("Add new quote...") },
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
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
                    text = "No quotes yet. Please add one above.",
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
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button: Save

        }
    }
}
