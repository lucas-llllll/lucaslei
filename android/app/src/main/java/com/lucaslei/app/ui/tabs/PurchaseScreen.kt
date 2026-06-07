package com.lucaslei.app.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.PurchaseItem

val PURCHASE_CATEGORIES = listOf(
    "水电", "瓷砖", "木工", "油漆", "卫浴", "灯具", "门窗",
    "防水", "暖通", "地板", "厨房", "吊顶", "墙面", "家电", "软装", "辅料", "五金", "其他"
)

val STATUS_LIST = listOf("待下单", "已下单", "已到货", "已安装", "已退换")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(vm: MainViewModel) {
    val purchases by vm.purchases.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf<PurchaseItem?>(null) }
    var selectedCategory by remember { mutableStateOf("全部") }
    var selectedStatus by remember { mutableStateOf("全部") }

    val filtered = purchases
        .filter { selectedCategory == "全部" || it.cat == selectedCategory }
        .filter { selectedStatus == "全部" || it.status == selectedStatus }

    val statusCounts = purchases.groupBy { it.status }.mapValues { it.value.size }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("采购") },
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

        // 状态概览
        if (purchases.isNotEmpty()) {
            Card(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("共 ${purchases.size} 项", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    STATUS_LIST.forEach { s ->
                        val count = statusCounts[s] ?: 0
                        if (count > 0) {
                            Text("$s:$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }

        // 分类筛选
        ScrollableTabRow(
            selectedTabIndex = (listOf("全部") + PURCHASE_CATEGORIES).indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp
        ) {
            (listOf("全部") + PURCHASE_CATEGORIES).forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = { Text(cat, style = MaterialTheme.typography.labelSmall) }
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
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { showDetail = item }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    textDecoration = if (item.status == "已退换") TextDecoration.LineThrough else TextDecoration.None
                                )
                                if (!item.spec.isNullOrBlank()) {
                                    Text(text = item.spec!!, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                                Row {
                                    if (item.cat.isNotBlank()) {
                                        Text(text = item.cat, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (!item.brand.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = item.brand!!, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    if (item.price > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "¥${item.price}", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error)
                                    }
                                    if (item.qty > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val qtyStr = if (item.qty == item.qty.toInt().toDouble()) item.qty.toInt().toString() else item.qty.toString()
                                        Text(text = "x$qtyStr", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            // 状态标签
                            FilterChip(
                                selected = false,
                                onClick = { vm.togglePurchaseStatus(item.id) },
                                label = { Text(item.status, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            IconButton(onClick = { vm.deletePurchase(item.id) }) {
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

    // 添加采购弹窗
    if (showAdd) {
        AddPurchaseDialog(onDismiss = { showAdd = false }) { name, cat, brand, spec, qty, price, channel, contact, status, expectDate, budget, note ->
            vm.addPurchase(name, cat, brand, spec, qty, price, channel, contact, status, expectDate, budget, note)
            showAdd = false
        }
    }

    // 详情弹窗
    showDetail?.let { item ->
        PurchaseDetailDialog(item = item) { showDetail = null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, Double, Double, String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("水电") }
    var brand by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("待下单") }
    var expectDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加采购项") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称*") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    // 分类选择
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(value = cat, onValueChange = {}, readOnly = true, label = { Text("分类") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) })
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            PURCHASE_CATEGORIES.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { cat = c; catExpanded = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("品牌") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("规格/型号") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() } },
                            label = { Text("数量") }, modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("单价") }, modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                }
                item {
                    OutlinedTextField(value = channel, onValueChange = { channel = it }, label = { Text("渠道") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("联系方式") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    // 状态选择
                    var statusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                        OutlinedTextField(value = status, onValueChange = {}, readOnly = true, label = { Text("状态") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) })
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            STATUS_LIST.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = expectDate, onValueChange = { expectDate = it }, label = { Text("期望日期") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("YYYY-MM-DD") })
                }
                item {
                    OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("预算") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onAdd(name.trim(), cat, brand.trim(), spec.trim(),
                        qty.toDoubleOrNull() ?: 0.0, price.toDoubleOrNull() ?: 0.0,
                        channel.trim(), contact.trim(), status, expectDate.trim(), budget.trim(), note.trim())
                }
            }) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailDialog(item: PurchaseItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.cat.isNotBlank()) Text("分类: ${item.cat}")
                if (!item.brand.isNullOrBlank()) Text("品牌: ${item.brand}")
                if (!item.spec.isNullOrBlank()) Text("规格: ${item.spec}")
                if (item.qty > 0) {
                    val qtyStr = if (item.qty == item.qty.toInt().toDouble()) item.qty.toInt().toString() else item.qty.toString()
                    Text("数量: $qtyStr")
                }
                if (item.price > 0) Text("单价: ¥${item.price}")
                if (!item.channel.isNullOrBlank()) Text("渠道: ${item.channel}")
                if (!item.contact.isNullOrBlank()) Text("联系方式: ${item.contact}")
                Text("状态: ${item.status}")
                if (!item.expectDate.isNullOrBlank()) Text("期望日期: ${item.expectDate}")
                if (!item.actualDate.isNullOrBlank()) Text("实际日期: ${item.actualDate}")
                if (!item.budget.isNullOrBlank()) Text("预算: ¥${item.budget}")
                if (!item.note.isNullOrBlank()) Text("备注: ${item.note}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
