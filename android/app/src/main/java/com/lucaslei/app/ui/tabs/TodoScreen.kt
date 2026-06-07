package com.lucaslei.app.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(vm: MainViewModel) {
    val todos by vm.todos.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var inputDeadline by remember { mutableStateOf("") }
    var inputPriority by remember { mutableStateOf("medium") }

    val priorityColors = mapOf(
        "high" to Color.Red,
        "medium" to Color(0xFFFF9800),
        "low" to Color(0xFF4CAF50)
    )
    val priorityLabels = mapOf("high" to "高", "medium" to "中", "low" to "低")

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("待办") },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = { vm.syncFromCloud() }) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = "同步")
                }
            }
        )

        if (todos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无待办", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todos, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { vm.toggleTodo(item.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.done) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 优先级标签
                                    val pColor = priorityColors[item.priority] ?: Color.Gray
                                    Text(
                                        text = priorityLabels[item.priority] ?: "中",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = pColor
                                    )
                                    if (!item.deadline.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "截止: ${item.deadline}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (!item.deadline.isNullOrEmpty() && !item.done) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { vm.deleteTodo(item.id) }) {
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
            Icon(Icons.Filled.Add, contentDescription = "添加")
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; inputText = ""; inputDeadline = ""; inputPriority = "medium" },
            title = { Text("添加待办") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("待办内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = inputDeadline,
                        onValueChange = { inputDeadline = it },
                        label = { Text("截止日期 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                    // 优先级选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("low" to "低", "medium" to "中", "high" to "高").forEach { (value, label) ->
                            FilterChip(
                                selected = inputPriority == value,
                                onClick = { inputPriority = value },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = (priorityColors[value] ?: Color.Gray).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inputText.isNotBlank()) {
                        vm.addTodo(inputText.trim(), inputDeadline.trim(), inputPriority)
                        inputText = ""
                        inputDeadline = ""
                        inputPriority = "medium"
                        showAdd = false
                    }
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false; inputText = ""; inputDeadline = ""; inputPriority = "medium" }) { Text("取消") }
            }
        )
    }
}
