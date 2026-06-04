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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.PurchaseItem

@Composable
fun PurchaseScreen(vm: MainViewModel) {
    val purchases by vm.purchases.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    val categories = listOf("全部", "厨房", "卫浴", "客厅", "卧室", "阳台", "其他")
    var selectedCategory by remember { mutableStateOf("全部") }

    val filtered = if (selectedCategory == "全部") purchases
    else purchases.filter { it.category == selectedCategory }

    val boughtCount = purchases.count { it.bought }
    val totalCount = purchases.size

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("采购") },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Progress
        if (totalCount > 0) {
            Card(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("进度: $boughtCount/$totalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    val progress = boughtCount.toFloat() / totalCount
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.width(100.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Category filter
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = { Text(cat) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无采购项目", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    PurchaseCard(item = item,
                        onToggle = { vm.togglePurchaseBought(item.id) },
                        onDelete = { vm.deletePurchase(item.id) }
                    )
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
        AddPurchaseDialog(onDismiss = { showAdd = false }, onAdd = { name, cat, spec, price, note ->
            vm.addPurchase(name, cat, spec, price, note)
            showAdd = false
        })
    }
}

@Composable
fun PurchaseCard(item: PurchaseItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.bought) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.bought, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (item.bought) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.bought) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                if (item.spec.isNotBlank()) {
                    Text(text = item.spec, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                Row {
                    if (item.category.isNotBlank()) {
                        Text(text = item.category, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (item.price.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "¥${item.price}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddPurchaseDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val cats = listOf("厨房", "卫浴", "客厅", "卧室", "阳台", "其他")
    var selectedCat by remember { mutableStateOf(cats[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加采购项") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(value = selectedCat, onValueChange = {},
                        label = { Text("分类") }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true)
                }
                OutlinedTextField(value = spec, onValueChange = { spec = it },
                    label = { Text("规格/型号") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("预算价格") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it },
                    label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onAdd(name.trim(), selectedCat, spec.trim(), price.trim(), note.trim())
            }) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
