package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PadRecord
import com.example.ui.theme.Floor1Color
import com.example.ui.theme.Floor2Color
import com.example.ui.theme.Floor3Color
import com.example.ui.viewmodel.SummaryStats

@Composable
fun FloorDistributionBar(
    stats: SummaryStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "樓層消耗分佈比例",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "總計 ${stats.totalAll} 片",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment horizontal bar
            if (stats.totalAll > 0) {
                val p1 by animateFloatAsState(targetValue = stats.percentFloor1, label = "p1")
                val p2 by animateFloatAsState(targetValue = stats.percentFloor2, label = "p2")
                val p3 by animateFloatAsState(targetValue = stats.percentFloor3, label = "p3")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (p1 > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(p1)
                                    .fillMaxHeight()
                                    .background(Floor1Color)
                            )
                        }
                        if (p2 > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(p2)
                                    .fillMaxHeight()
                                    .background(Floor2Color)
                            )
                        }
                        if (p3 > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(p3)
                                    .fillMaxHeight()
                                    .background(Floor3Color)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "尚無補充紀錄",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legends with count & percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(
                    label = "1樓",
                    count = stats.totalFloor1,
                    percent = stats.percentFloor1 * 100,
                    color = Floor1Color
                )
                LegendItem(
                    label = "2樓",
                    count = stats.totalFloor2,
                    percent = stats.percentFloor2 * 100,
                    color = Floor2Color
                )
                LegendItem(
                    label = "3樓",
                    count = stats.totalFloor3,
                    percent = stats.percentFloor3 * 100,
                    color = Floor3Color
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    count: Int,
    percent: Float,
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
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${count}片 (${String.format(java.util.Locale.getDefault(), "%.0f", percent)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DailyTrendBarChart(
    records: List<PadRecord>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "每日補充量趨勢",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "按樓層堆疊顯示每日消耗補充數",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val maxTotal = records.maxOfOrNull { it.totalCount }?.coerceAtLeast(10) ?: 10
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Display in chronological order for trend
                val sorted = records.sortedBy { it.epochDay }
                sorted.forEach { record ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Total count above bar
                        Text(
                            text = "${record.totalCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Stacked Bar
                        val barHeightFraction = (record.totalCount.toFloat() / maxTotal).coerceIn(0.05f, 1f)
                        val totalBarHeightDp = (110 * barHeightFraction).dp

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(totalBarHeightDp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (record.floor3Count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(record.floor3Count.toFloat())
                                            .background(Floor3Color)
                                    )
                                }
                                if (record.floor2Count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(record.floor2Count.toFloat())
                                            .background(Floor2Color)
                                    )
                                }
                                if (record.floor1Count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(record.floor1Count.toFloat())
                                            .background(Floor1Color)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Date label (e.g. "09/18")
                        val shortDate = if (record.dateString.length >= 10) {
                            record.dateString.substring(5)
                        } else record.dateString

                        Text(
                            text = shortDate,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
