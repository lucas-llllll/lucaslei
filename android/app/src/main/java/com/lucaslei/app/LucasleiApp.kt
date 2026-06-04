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
    val dataStore = remember { DataStore(context) }
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(dataStore))
    LucasleiTheme {
        val tabs = listOf(Tab.Recipes, Tab.Purchase, Tab.Weight, Tab.Todo)
        var selectedTab by remember { mutableIntStateOf(0) }
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = NavigationBarDefaults.Elevation) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
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
            }
        }
    }
}