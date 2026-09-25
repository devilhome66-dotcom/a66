package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FloorCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PadViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    viewModel: PadViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val floor1Count by viewModel.floor1Count.collectAsStateWithLifecycle()
    val floor2Count by viewModel.floor2Count.collectAsStateWithLifecycle()
    val floor3Count by viewModel.floor3Count.collectAsStateWithLifecycle()
    val padType by viewModel.padType.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isExistingRecord by viewModel.isExistingRecord.collectAsStateWithLifecycle()

    val totalToday = floor1Count + floor2Count + floor3Count
    var showDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Date Navigation Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_nav_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.stepDay(-1) },
                        modifier = Modifier.testTag("prev_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "前一天",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = DateUtils.formatDisplayDate(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DateUtils.getDayOfWeekChinese(selectedDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            if (selectedDate == DateUtils.getTodayString()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "今天",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.testTag("pick_date_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "選擇日期",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.stepDay(1) },
                        modifier = Modifier.testTag("next_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "後一天",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Daily Summary Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_hero_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "當日補充總片數",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$totalToday",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "片",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isExistingRecord) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isExistingRecord) Icons.Default.CheckCircle else Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = if (isExistingRecord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isExistingRecord) "已登記 (可修改)" else "尚未儲存",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExistingRecord) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mini Floor Distribution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FloorCountIndicator("1樓", floor1Count, Floor1Color)
                        FloorCountIndicator("2樓", floor2Count, Floor2Color)
                        FloorCountIndicator("3樓", floor3Count, Floor3Color)
                    }
                }
            }
        }

        // 3. Floor 1 Card
        item {
            FloorCard(
                floorNumber = 1,
                floorName = "1樓 衛生棉補充",
                floorSubtitle = "一樓大廳與無障礙/女性化妝室備品",
                count = floor1Count,
                accentColor = Floor1Color,
                lightBgColor = Floor1LightColor,
                onAdjust = { delta -> viewModel.adjustFloorCount(1, delta) },
                onSetCount = { count -> viewModel.setFloorCount(1, count) }
            )
        }

        // 4. Floor 2 Card
        item {
            FloorCard(
                floorNumber = 2,
                floorName = "2樓 衛生棉補充",
                floorSubtitle = "二樓辦公區/休憩空間女性化妝室備品",
                count = floor2Count,
                accentColor = Floor2Color,
                lightBgColor = Floor2LightColor,
                onAdjust = { delta -> viewModel.adjustFloorCount(2, delta) },
                onSetCount = { count -> viewModel.setFloorCount(2, count) }
            )
        }

        // 5. Floor 3 Card
        item {
            FloorCard(
                floorNumber = 3,
                floorName = "3樓 衛生棉補充",
                floorSubtitle = "三樓多功能室與高樓層化妝室備品",
                count = floor3Count,
                accentColor = Floor3Color,
                lightBgColor = Floor3LightColor,
                onAdjust = { delta -> viewModel.adjustFloorCount(3, delta) },
                onSetCount = { count -> viewModel.setFloorCount(3, count) }
            )
        }

        // 6. Category / Specs & Notes Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("specs_notes_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "補充規格與類型",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val padTypes = listOf("一般型", "日用 (24cm)", "夜用 (28cm+)", "護墊", "綜合備品")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        padTypes.forEach { type ->
                            FilterChip(
                                selected = padType == type,
                                onClick = { viewModel.setPadType(type) },
                                label = { Text(type, fontSize = 12.sp) },
                                modifier = Modifier.testTag("pad_type_$type"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "備註說明 (選填)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.setNotes(it) },
                        placeholder = { Text("例如：例行早上巡檢補滿、今日人潮多特別補貨等") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }
            }
        }

        // 7. Save Action Button
        item {
            Button(
                onClick = { viewModel.saveCurrentRecord() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_record_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExistingRecord) "更新當日補充紀錄" else "儲存今日補充登記",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val currentEpoch = DateUtils.parseDateStringToEpochDay(selectedDate)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentEpoch * 24L * 60L * 60L * 1000L
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val epochDay = millis / (24L * 60L * 60L * 1000L)
                            val dateString = DateUtils.epochDayToDateString(epochDay)
                            viewModel.selectDate(dateString)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FloorCountIndicator(
    floorName: String,
    count: Int,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$floorName：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count 片",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
