package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PadRecord
import com.example.ui.components.DateRangeSelector
import com.example.ui.components.FloorDistributionBar
import com.example.ui.components.KpiMetricCard
import com.example.ui.theme.Floor1Color
import com.example.ui.theme.Floor2Color
import com.example.ui.theme.Floor3Color
import com.example.ui.viewmodel.PadViewModel
import com.example.util.DateUtils

@Composable
fun RangeStatsScreen(
    viewModel: PadViewModel,
    onNavigateToEdit: (PadRecord) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val startEpoch by viewModel.rangeStartEpoch.collectAsStateWithLifecycle()
    val endEpoch by viewModel.rangeEndEpoch.collectAsStateWithLifecycle()
    val activePreset by viewModel.activePreset.collectAsStateWithLifecycle()
    val rangeRecords by viewModel.rangeRecords.collectAsStateWithLifecycle()
    val stats by viewModel.rangeStats.collectAsStateWithLifecycle()

    var recordToDelete by remember { mutableStateOf<PadRecord?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Date Range Picker & Presets
        item {
            DateRangeSelector(
                startEpoch = startEpoch,
                endEpoch = endEpoch,
                activePreset = activePreset,
                onRangeChange = { start, end, preset ->
                    viewModel.setRange(start, end, preset)
                },
                onPresetSelect = { preset ->
                    viewModel.applyPreset(preset)
                }
            )
        }

        // 2. Range KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "區間消耗/補充總計",
                    value = "${stats.totalAll}",
                    unit = "片",
                    subtitle = "區間內共 ${stats.daysRecorded} 天有補充",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "區間日均消耗",
                    value = String.format(java.util.Locale.getDefault(), "%.1f", stats.dailyAverage),
                    unit = "片/日",
                    subtitle = "計算 ${stats.daysInRange} 天日平均",
                    accentColor = Floor3Color,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Floor Distribution
        item {
            FloorDistributionBar(stats = stats)
        }

        // 4. Floor Breakdown Cards
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
                        text = "自訂區間各樓層補充總量",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FloorSummaryRow(
                        floor = "1樓 補充總數",
                        count = stats.totalFloor1,
                        percent = stats.percentFloor1,
                        color = Floor1Color,
                        desc = "大廳與無障礙化妝室"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    FloorSummaryRow(
                        floor = "2樓 補充總數",
                        count = stats.totalFloor2,
                        percent = stats.percentFloor2,
                        color = Floor2Color,
                        desc = "二樓辦公區休憩空間化妝室"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    FloorSummaryRow(
                        floor = "3樓 補充總數",
                        count = stats.totalFloor3,
                        percent = stats.percentFloor3,
                        color = Floor3Color,
                        desc = "三樓多功能室化妝室"
                    )
                }
            }
        }

        // 5. Actions: Copy Report & Share Report
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val report = viewModel.generateRangeReportText()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Pad Report", report)
                        clipboard.setPrimaryClip(clip)
                        onShowSnackbar("已複製區間統計報表文字至剪貼簿！")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_report_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("複製報表摘要", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val report = viewModel.generateRangeReportText()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "衛生棉補充消耗統計報表")
                            putExtra(Intent.EXTRA_TEXT, report)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享補充消耗報表"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_report_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("分享報表", fontSize = 13.sp)
                }
            }
        }

        // 6. Section Title for Range Records
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "區間內每日紀錄明細",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "共 ${rangeRecords.size} 筆紀錄",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 7. Range Records List
        if (rangeRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "此日期區間內尚無登記紀錄",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "可嘗試切換上方快捷區間按鈕或新增登記",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(rangeRecords.sortedByDescending { it.epochDay }, key = { it.id }) { record ->
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
