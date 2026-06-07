package com.lucaslei.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = DataStore(application)
    val cloudSync = CloudSync(dataStore)

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val purchases: StateFlow<List<PurchaseItem>> = _purchases.asStateFlow()

    private val _weights = MutableStateFlow<List<WeightRecord>>(emptyList())
    val weights: StateFlow<List<WeightRecord>> = _weights.asStateFlow()

    private val _syncStatus = MutableStateFlow<Pair<String, String>>("" to "")
    val syncStatus: StateFlow<Pair<String, String>> = _syncStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow("")
    val syncProgress: StateFlow<String> = _syncProgress.asStateFlow()

    init {
        loadAll()
        cloudSync.setListener { status, text ->
            _syncStatus.value = status to text
        }
    }

    fun loadAll() {
        _todos.value = dataStore.loadTodos()
        _recipes.value = dataStore.loadRecipes()
        _purchases.value = dataStore.loadPurchases()
        _weights.value = dataStore.loadWeights()
    }

    // === Todo ===
    fun addTodo(text: String, deadline: String = "", priority: String = "medium") {
        val item = TodoItem(
            id = "tmp${System.currentTimeMillis()}",
            text = text,
            deadline = deadline,
            priority = priority,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
        )
        val list = _todos.value + item
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadTodos(list)
            } catch (e: Exception) {
                Log.e("VM", "addTodo sync failed: ${e.message}")
            }
        }
    }

    fun toggleTodo(id: String) {
        val list = _todos.value.map { if (it.id == id) it.copy(done = !it.done) else it }
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadTodos(list)
            } catch (e: Exception) {
                Log.e("VM", "toggleTodo sync failed: ${e.message}")
            }
        }
    }

    fun deleteTodo(id: String) {
        val list = _todos.value.filter { it.id != id }
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadTodos(list)
            } catch (e: Exception) {
                Log.e("VM", "deleteTodo sync failed: ${e.message}")
            }
        }
    }

    // === Recipe (纯本地) ===
    fun addRecipe(name: String, category: String, ingredients: String, steps: String) {
        val item = Recipe(id = System.currentTimeMillis().toString(), name = name, category = category, ingredients = ingredients, steps = steps)
        val list = _recipes.value + item
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    fun toggleRecipeFavorite(id: String) {
        val list = _recipes.value.map { if (it.id == id) it.copy(favorite = !it.favorite) else it }
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    fun deleteRecipe(id: String) {
        val list = _recipes.value.filter { it.id != id }
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    // === Purchase ===
    fun addPurchase(name: String, cat: String, brand: String, spec: String, qty: Int, price: Double, channel: String, contact: String, status: String, expectDate: String, budget: String, note: String) {
        val item = PurchaseItem(
            id = "r${System.currentTimeMillis()}",
            name = name,
            brand = brand,
            spec = spec,
            qty = qty,
            price = price,
            channel = channel,
            contact = contact,
            status = status,
            expectDate = expectDate,
            actualDate = "",
            cat = cat,
            budget = budget,
            note = note
        )
        val list = _purchases.value + item
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadPurchases(list)
            } catch (e: Exception) {
                Log.e("VM", "addPurchase sync failed: ${e.message}")
            }
        }
    }

    fun updatePurchase(id: String, update: (PurchaseItem) -> PurchaseItem) {
        val list = _purchases.value.map { if (it.id == id) update(it) else it }
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadPurchases(list)
            } catch (e: Exception) {
                Log.e("VM", "updatePurchase sync failed: ${e.message}")
            }
        }
    }

    fun togglePurchaseStatus(id: String) {
        val statusOrder = listOf("待下单", "已下单", "已到货", "已安装")
        updatePurchase(id) { item ->
            val currentIdx = statusOrder.indexOf(item.status)
            val nextStatus = if (currentIdx < statusOrder.size - 1) statusOrder[currentIdx + 1] else statusOrder[0]
            item.copy(status = nextStatus)
        }
    }

    fun deletePurchase(id: String) {
        val list = _purchases.value.filter { it.id != id }
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadPurchases(list)
            } catch (e: Exception) {
                Log.e("VM", "deletePurchase sync failed: ${e.message}")
            }
        }
    }

    // === Weight ===
    fun addWeight(weight: String, date: String, note: String) {
        val item = WeightRecord(id = System.currentTimeMillis().toString(), weight = weight, date = date, note = note)
        val list = _weights.value + item
        _weights.value = list
        dataStore.saveWeights(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadWeights(list)
            } catch (e: Exception) {
                Log.e("VM", "addWeight sync failed: ${e.message}")
            }
        }
    }

    fun deleteWeight(id: String) {
        val list = _weights.value.filter { it.id != id }
        _weights.value = list
        dataStore.saveWeights(list)
        viewModelScope.launch {
            try {
                cloudSync.uploadWeights(list)
            } catch (e: Exception) {
                Log.e("VM", "deleteWeight sync failed: ${e.message}")
            }
        }
    }

    // === Cloud Sync ===
    fun syncFromCloud() {
        viewModelScope.launch {
            try {
                val (mergedTodos, mergedPurchases, mergedWeights) = cloudSync.syncAll(
                    localTodos = _todos.value,
                    localPurchases = _purchases.value,
                    localWeights = _weights.value
                ) { progress ->
                    _syncProgress.value = progress
                }
                _todos.value = mergedTodos
                dataStore.saveTodos(mergedTodos)
                _purchases.value = mergedPurchases
                dataStore.savePurchases(mergedPurchases)
                _weights.value = mergedWeights
                dataStore.saveWeights(mergedWeights)
            } catch (e: Exception) {
                _syncStatus.value = "err" to "同步失败: ${e.message}"
            } finally {
                _syncProgress.value = ""
            }
        }
    }

    fun uploadToCloud() {
        viewModelScope.launch {
            try {
                cloudSync.uploadAll(
                    todos = _todos.value,
                    purchases = _purchases.value,
                    weights = _weights.value
                ) { progress ->
                    _syncProgress.value = progress
                }
            } catch (e: Exception) {
                _syncStatus.value = "err" to "上传失败: ${e.message}"
            } finally {
                _syncProgress.value = ""
            }
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(application) as T
    }
}
