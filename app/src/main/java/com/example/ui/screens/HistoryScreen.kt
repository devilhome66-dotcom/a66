package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PadRecord
import com.example.ui.viewmodel.PadViewModel
import com.example.util.DateUtils

@Composable
fun HistoryScreen(
    viewModel: PadViewModel,
    onNavigateToEdit: (PadRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFloorFilter by remember { mutableStateOf(0) } // 0: All, 1: 1F, 2: 2F, 3: 3F
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<PadRecord?>(null) }

    val filteredRecords = remember(allRecords, searchQuery, selectedFloorFilter) {
        allRecords.filter { record ->
            val matchSearch = searchQuery.isEmpty() ||
                    record.dateString.contains(searchQuery) ||
                    record.notes.contains(searchQuery, ignoreCase = true) ||
                    record.padType.contains(searchQuery, ignoreCase = true)

            val matchFloor = when (selectedFloorFilter) {
                1 -> record.floor1Count > 0
                2 -> record.floor2Count > 0
                3 -> record.floor3Count > 0
                else -> true
            }

            matchSearch && matchFloor
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                placeholder = { Text("搜尋日期、備註或類型...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "清除搜尋")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // 2. Floor Filter Chips & Demo Data Tools
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "全部樓層",
                        1 to "1樓有補充",
                        2 to "2樓有補充",
                        3 to "3樓有補充"
                    ).forEach { (floorId, label) ->
                        FilterChip(
                            selected = selectedFloorFilter == floorId,
                            onClick = { selectedFloorFilter = floorId },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_floor_$floorId")
                        )
                    }
                }

                // Data quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.seedDemoData() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("seed_demo_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("載入25天示範數據", fontSize = 12.sp)
                    }

                    if (allRecords.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Header Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "歷史紀錄清單",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "符合筆數：${filteredRecords.size} / 總共 ${allRecords.size} 筆",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. Records List
        if (filteredRecords.isEmpty()) {
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
                            text = if (allRecords.isEmpty()) "目前尚無任何補充紀錄" else "找不到符合條件的紀錄",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (allRecords.isEmpty()) "點擊上方「載入25天示範數據」可快速體驗統計功能" else "請嘗試調整關鍵字或樓層篩選",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                MonthlyRecordItem(
                    record = record,
                    onEdit = { onNavigateToEdit(record) },
                    onDelete = { recordToDelete = record }
                )
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("確認清空所有數據？") },
            text = { Text("這將刪除所有歷史每日登記紀錄，確認繼續？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確認清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Single Record Delete Dialog
    if (recordToDelete != null) {
        val r = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("確認刪除此筆紀錄？") },
            text = {
                Text("確定要刪除 ${DateUtils.formatDisplayDate(r.dateString)} 的補充紀錄（共 ${r.totalCount} 片）嗎？")
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
