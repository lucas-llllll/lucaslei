package com.lucaslei.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lucaslei.app.ui.LucasleiTheme
import com.lucaslei.app.ui.tabs.*

sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Recipes : Tab("recipes", "菜谱", Icons.Filled.Restaurant)
    object Purchase : Tab("purchase", "采购", Icons.Filled.ShoppingCart)
    object Weight : Tab("weight", "体重", Icons.Filled.MonitorWeight)
    object Todo : Tab("todo", "待办", Icons.Filled.Checklist)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LucasleiApp() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application)
    )
    LucasleiTheme {
        val tabs = listOf(Tab.Recipes, Tab.Purchase, Tab.Weight, Tab.Todo)
        var selectedTab by remember { mutableIntStateOf(0) }
        val syncStatus by viewModel.syncStatus.collectAsState()
        val syncProgress by viewModel.syncProgress.collectAsState()

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> RecipesScreen(viewModel)
                    1 -> PurchaseScreen(viewModel)
                    2 -> WeightScreen(viewModel)
                    3 -> TodoScreen(viewModel)
                }

                // Sync progress overlay
                if (syncProgress.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = syncProgress,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}