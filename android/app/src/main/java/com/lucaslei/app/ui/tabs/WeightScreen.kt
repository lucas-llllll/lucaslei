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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lucaslei.app.MainViewModel
import com.lucaslei.app.WeightRecord
import com.lucaslei.app.WeightInjRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(vm: MainViewModel) {
    val weights by vm.weights.collectAsState()
    val injections by vm.injections.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddWeight by remember { mutableStateOf(false) }
    var showAddInj by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SmallTopAppBar(
            title = { Text("体重") },
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

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("体重") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("注射") })
        }

        when (selectedTab) {
            0 -> WeightTab(vm, weights, showAddWeight) { showAddWeight = it }
            1 -> InjectionTab(vm, injections, showAddInj) { showAddInj = it }
        }
    }

    if (showAddWeight) {
        AddWeightDialog(onDismiss = { showAddWeight = false }) { weight, date, note ->
            vm.addWeight(weight, date, note)
            showAddWeight = false
        }
    }

    if (showAddInj) {
        AddInjectionDialog(onDismiss = { showAddInj = false }) { date, dose, side, note, seq ->
            vm.addInjection(date, dose, side, note, seq)
            showAddInj = false
        }
    }
}

@Composable
fun WeightTab(vm: MainViewModel, weights: List<WeightRecord>, showAdd: Boolean, onShowAdd: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (weights.isNotEmpty()) {
            val latest = weights.maxByOrNull { it.date }
            Card(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = null,
                        modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("${latest?.weight ?: 0.0} kg", style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(latest?.date ?: "", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }
        }

        if (weights.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无体重记录", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weights.sortedByDescending { it.date }, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${record.weight} kg", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                if (!record.note.isNullOrBlank()) {
                                    Text(record.note ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { vm.deleteWeight(record.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onShowAdd(true) },
            modifier = Modifier.padding(16.dp).align(Alignment.End),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "记录体重")
        }
    }
}

@Composable
fun InjectionTab(vm: MainViewModel, injections: List<WeightInjRecord>, showAdd: Boolean, onShowAdd: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (injections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Vaccines, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("暂无注射记录", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(injections.sortedByDescending { it.seq }, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${record.seq}", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(36.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row {
                                    Text(record.date, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (record.side == "左") "← 左" else "右 →",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (record.side == "左") Color(0xFF2196F3) else Color(0xFFE91E63))
                                    if (record.dose != null && record.dose != "") {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("剂量: ${record.dose}", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                if (!record.note.isNullOrBlank()) {
                                    Text(record.note ?: "", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            IconButton(onClick = { vm.deleteInjection(record.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onShowAdd(true) },
            modifier = Modifier.padding(16.dp).align(Alignment.End),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "记录注射")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onAdd: (Double, String, String) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录体重") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("体重 (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text("日期: $today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (weight.isNotBlank()) {
                    onAdd(weight.toDoubleOrNull() ?: 0.0, today, note.trim())
                }
            }) { Text("记录") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInjectionDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, Int) -> Unit) {
    var date by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var side by remember { mutableStateOf("左") }
    var note by remember { mutableStateOf("") }
    var seq by remember { mutableStateOf("") }

    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录注射") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date.ifBlank { today },
                    onValueChange = { date = it },
                    label = { Text("日期") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("YYYY-MM-DD") }
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("剂量 (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = side == "左",
                        onClick = { side = "左" },
                        label = { Text("← 左") }
                    )
                    FilterChip(
                        selected = side == "右",
                        onClick = { side = "右" },
                        label = { Text("右 →") }
                    )
                }
                OutlinedTextField(
                    value = seq,
                    onValueChange = { seq = it.filter { c -> c.isDigit() } },
                    label = { Text("第几针") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(
                    date.ifBlank { today },
                    dose.trim(),
                    side,
                    note.trim(),
                    seq.toIntOrNull() ?: 0
                )
            }) { Text("记录") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
