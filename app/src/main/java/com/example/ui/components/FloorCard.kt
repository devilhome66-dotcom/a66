package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun FloorCard(
    floorNumber: Int,
    floorName: String,
    floorSubtitle: String,
    count: Int,
    accentColor: Color,
    lightBgColor: Color,
    onAdjust: (Int) -> Unit,
    onSetCount: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDirectInputDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floor_${floorNumber}_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Floor tag & subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${floorNumber}F",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = floorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = floorSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick tap to enter direct number
                IconButton(
                    onClick = { showDirectInputDialog = true },
                    modifier = Modifier.testTag("floor_${floorNumber}_edit_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "直接輸入片數",
                        tint = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Stepper Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus 10
                FilledTonalIconButton(
                    onClick = { onAdjust(-10) },
                    enabled = count >= 10,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("floor_${floorNumber}_minus_10")
                ) {
                    Text(
                        "-10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (count >= 10) accentColor else Color.Gray
                    )
                }

                // Minus 1
                FilledTonalIconButton(
                    onClick = { onAdjust(-1) },
                    enabled = count > 0,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("floor_${floorNumber}_minus_1")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "減1片",
                        tint = if (count > 0) accentColor else Color.Gray
                    )
                }

                // Middle Display (Clickable for direct input)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(lightBgColor)
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { showDirectInputDialog = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$count",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "片 (點擊可輸入)",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                    }
                }

                // Plus 1
                FilledTonalIconButton(
                    onClick = { onAdjust(1) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("floor_${floorNumber}_plus_1"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "加1片",
                        tint = accentColor
                    )
                }

                // Plus 10
                FilledTonalIconButton(
                    onClick = { onAdjust(10) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("floor_${floorNumber}_plus_10"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        "+10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5, 10, 20, 30).forEach { preset ->
                    SuggestionChip(
                        onClick = { onAdjust(preset) },
                        label = { Text("+$preset") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("floor_${floorNumber}_chip_$preset"),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
                if (count > 0) {
                    SuggestionChip(
                        onClick = { onSetCount(0) },
                        label = { Text("歸零") },
                        modifier = Modifier.testTag("floor_${floorNumber}_reset"),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            labelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }

    if (showDirectInputDialog) {
        DirectNumberInputDialog(
            floorName = floorName,
            currentValue = count,
            onDismiss = { showDirectInputDialog = false },
            onConfirm = { newCount ->
                onSetCount(newCount)
                showDirectInputDialog = false
            }
        )
    }
}

@Composable
fun DirectNumberInputDialog(
    floorName: String,
    currentValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(if (currentValue > 0) currentValue.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "輸入 $floorName 補充片數")
        },
        text = {
            Column {
                Text(
                    text = "請輸入補充/消耗的衛生棉數量：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 5) {
                            textValue = input
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("0") },
                    suffix = { Text("片") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = textValue.toIntOrNull() ?: 0
                    onConfirm(count)
                },
                modifier = Modifier.testTag("direct_input_confirm")
            ) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
