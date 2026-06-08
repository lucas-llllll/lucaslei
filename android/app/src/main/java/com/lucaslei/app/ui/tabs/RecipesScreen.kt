package com.lucaslei.app.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(vm: MainViewModel) {
    val recipes by vm.recipes.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf<Recipe?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("菜谱") },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = { vm.uploadToCloud() }) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = "上传")
                }
            }
        )

        if (recipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无菜谱", color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击右下角 + 添加", color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showDetail = recipe },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = recipe.name, style = MaterialTheme.typography.titleMedium)
                                if (!recipe.category.isNullOrBlank()) {
                                    Text(text = recipe.category ?: "", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { vm.toggleRecipeFavorite(recipe.id) }) {
                                Icon(
                                    if (recipe.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "收藏",
                                    tint = if (recipe.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = { vm.deleteRecipe(recipe.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.padding(16.dp).align(Alignment.End),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "添加菜谱")
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var ingredients by remember { mutableStateOf("") }
        var steps by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("添加菜谱") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("菜名") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it },
                        label = { Text("分类") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = ingredients, onValueChange = { ingredients = it },
                        label = { Text("食材") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = steps, onValueChange = { steps = it },
                        label = { Text("步骤") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        vm.addRecipe(name.trim(), category.trim(), ingredients.trim(), steps.trim())
                        showAdd = false
                    }
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("取消") }
            }
        )
    }

    showDetail?.let { recipe ->
        AlertDialog(
            onDismissRequest = { showDetail = null },
            title = { Text(recipe.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!recipe.category.isNullOrBlank()) {
                        Text("分类: ${recipe.category ?: ""}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (!recipe.ingredients.isNullOrBlank()) {
                        Divider()
                        Text("食材", style = MaterialTheme.typography.titleSmall)
                        Text(recipe.ingredients ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!recipe.steps.isNullOrBlank()) {
                        Divider()
                        Text("步骤", style = MaterialTheme.typography.titleSmall)
                        Text(recipe.steps ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = null }) { Text("关闭") }
            }
        )
    }
}