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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.Recipe

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
            )
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
                    RecipeCard(recipe = recipe,
                        onClick = { showDetail = recipe },
                        onToggleFavorite = { vm.toggleRecipeFavorite(recipe.id) },
                        onDelete = { vm.deleteRecipe(recipe.id) }
                    )
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
        AddRecipeDialog(onDismiss = { showAdd = false }, onAdd = { name, cat, ing, steps ->
            vm.addRecipe(name, cat, ing, steps)
            showAdd = false
        })
    }

    showDetail?.let { recipe ->
        RecipeDetailDialog(recipe = recipe, onDismiss = { showDetail = null })
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit, onToggleFavorite: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recipe.name, style = MaterialTheme.typography.titleMedium)
                if (recipe.category.isNotBlank()) {
                    Text(text = recipe.category, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (recipe.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (recipe.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddRecipeDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                if (name.isNotBlank()) onAdd(name.trim(), category.trim(), ingredients.trim(), steps.trim())
            }) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun RecipeDetailDialog(recipe: Recipe, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (recipe.category.isNotBlank()) {
                    Text("分类: ${recipe.category}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                if (recipe.ingredients.isNotBlank()) {
                    Divider()
                    Text("食材", style = MaterialTheme.typography.titleSmall)
                    Text(recipe.ingredients, style = MaterialTheme.typography.bodyMedium)
                }
                if (recipe.steps.isNotBlank()) {
                    Divider()
                    Text("步骤", style = MaterialTheme.typography.titleSmall)
                    Text(recipe.steps, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
