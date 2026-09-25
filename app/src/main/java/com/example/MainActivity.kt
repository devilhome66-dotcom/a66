package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DailyLogScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MonthlyStatsScreen
import com.example.ui.screens.RangeStatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PadViewModel
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector) {
    DAILY("每日登記", Icons.Default.EditCalendar),
    MONTHLY("月度統計", Icons.Default.BarChart),
    RANGE("區間查詢", Icons.Default.DateRange),
    HISTORY("歷史明細", Icons.Default.History)
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PadViewModel = viewModel()
                var currentTab by remember { mutableStateOf(AppTab.DAILY) }
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // Collect ViewModel snackbar messages
                LaunchedEffect(Unit) {
                    viewModel.saveMessage.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Handle back press to return to first tab
                BackHandler(enabled = currentTab != AppTab.DAILY) {
                    currentTab = AppTab.DAILY
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        AppTab.DAILY -> "每日衛生棉補充登記"
                                        AppTab.MONTHLY -> "每月消耗總數統計"
                                        AppTab.RANGE -> "自訂區間消耗統計"
                                        AppTab.HISTORY -> "衛生棉補充明細總覽"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            AppTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    when (currentTab) {
                        AppTab.DAILY -> DailyLogScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                        AppTab.MONTHLY -> MonthlyStatsScreen(
                            viewModel = viewModel,
                            onNavigateToEdit = { record ->
                                viewModel.editRecord(record)
                                currentTab = AppTab.DAILY
                            },
                            modifier = screenModifier
                        )
                        AppTab.RANGE -> RangeStatsScreen(
                            viewModel = viewModel,
                            onNavigateToEdit = { record ->
                                viewModel.editRecord(record)
                                currentTab = AppTab.DAILY
                            },
                            onShowSnackbar = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            modifier = screenModifier
                        )
                        AppTab.HISTORY -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToEdit = { record ->
                                viewModel.editRecord(record)
                                currentTab = AppTab.DAILY
                            },
                            modifier = screenModifier
                        )
                    }
                }
            }
        }
    }
}
