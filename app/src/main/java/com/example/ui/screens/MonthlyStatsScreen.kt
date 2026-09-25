package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.PadRecord
import com.example.ui.components.DailyTrendBarChart
import com.example.ui.components.FloorDistributionBar
import com.example.ui.components.KpiMetricCard
import com.example.ui.theme.Floor1Color
import com.example.ui.theme.Floor2Color
import com.example.ui.theme.Floor3Color
import com.example.ui.viewmodel.PadViewModel
import com.example.util.DateUtils

@Composable
fun MonthlyStatsScreen(
    viewModel: PadViewModel,
    onNavigateToEdit: (PadRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthRecords by viewModel.monthRecords.collectAsStateWithLifecycle()
    val stats by viewModel.monthStats.collectAsStateWithLifecycle()

    var recordToDelete by remember { mutableStateOf<PadRecord?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Month Navigation Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_nav_card"),
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
                        onClick = { viewModel.stepMonth(-1) },
                        modifier = Modifier.testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "上個月",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateUtils.formatMonthDisplay(selectedMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "每月消耗與補充統計總結",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.stepMonth(1) },
                        modifier = Modifier.testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "下個月",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. High-level KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "本月消耗總計",
                    value = "${stats.totalAll}",
                    unit = "片",
                    subtitle = "共 ${stats.daysRecorded} 天登記",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "每日平均補充",
                    value = String.format(java.util.Locale.getDefault(), "%.1f", stats.dailyAverage),
                    unit = "片/日",
                    subtitle = "全樓層日平均",
                    accentColor = Floor2Color,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Floor Distribution Bar
        item {
            FloorDistributionBar(stats = stats)
        }

        // 4. Floor Detail Cards (1F, 2F, 3F)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "各樓層月度消耗數據",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FloorSummaryRow(
                        floor = "1樓",
                        count = stats.totalFloor1,
                        percent = stats.percentFloor1,
                        color = Floor1Color,
                        desc = "大廳與無障礙/女性化妝室"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    FloorSummaryRow(
                        floor = "2樓",
                        count = stats.totalFloor2,
                        percent = stats.percentFloor2,
                        color = Floor2Color,
                        desc = "辦公區與休憩空間化妝室"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    FloorSummaryRow(
                        floor = "3樓",
                        count = stats.totalFloor3,
                        percent = stats.percentFloor3,
                        color = Floor3Color,
                        desc = "高樓層多功能室化妝室"
                    )
                }
            }
        }

        // 5. Monthly Trend Chart
        if (monthRecords.isNotEmpty()) {
            item {
                DailyTrendBarChart(records = monthRecords)
            }
        }

        // 6. Section Title for Daily Records
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "當月每日紀錄明細",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "共 ${monthRecords.size} 筆",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 7. Daily Records List
        if (monthRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "本月份尚無補充登記紀錄",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "請切換到「今日登記」新增紀錄，或於「歷史紀錄」載入示範數據",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(monthRecords.sortedByDescending { it.epochDay }, key = { it.id }) { record ->
                MonthlyRecordItem(
                    record = record,
                    onEdit = { onNavigateToEdit(record) },
                    onDelete = { recordToDelete = record }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (recordToDelete != null) {
        val r = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("確認刪除紀錄？") },
            text = {
                Text("確定要刪除 ${DateUtils.formatDisplayDate(r.dateString)} 的補充紀錄（共 ${r.totalCount} 片）嗎？此操作無法還原。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(r)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("刪除")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun FloorSummaryRow(
    floor: String,
    count: Int,
    percent: Float,
    color: Color,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = floor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$count 片",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "${String.format(java.util.Locale.getDefault(), "%.1f", percent * 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthlyRecordItem(
    record: PadRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.formatDisplayDate(record.dateString),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.padType,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1F: ${record.floor1Count}片",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Floor1Color
                    )
                    Text(
                        text = "2F: ${record.floor2Count}片",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Floor2Color
                    )
                    Text(
                        text = "3F: ${record.floor3Count}片",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Floor3Color
                    )
                    Text(
                        text = "計: ${record.totalCount}片",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (record.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "備註：${record.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改紀錄",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除紀錄",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
