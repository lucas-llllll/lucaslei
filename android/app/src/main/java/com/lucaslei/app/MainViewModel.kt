package com.lucaslei.app

import android.app.Application
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

    private fun loadAll() {
        _todos.value = dataStore.loadTodos()
        _recipes.value = dataStore.loadRecipes()
        _purchases.value = dataStore.loadPurchases()
        _weights.value = dataStore.loadWeights()
    }

    // === Todo ===
    fun addTodo(text: String) {
        val item = TodoItem(id = System.currentTimeMillis().toString(), text = text)
        val list = _todos.value + item
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch { cloudSync.saveTodos(list) }
    }

    fun toggleTodo(id: String) {
        val list = _todos.value.map { if (it.id == id) it.copy(done = !it.done) else it }
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch { cloudSync.saveTodos(list) }
    }

    fun deleteTodo(id: String) {
        val list = _todos.value.filter { it.id != id }
        _todos.value = list
        dataStore.saveTodos(list)
        viewModelScope.launch { cloudSync.saveTodos(list) }
    }

    // === Recipe ===
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
    fun addPurchase(name: String, category: String, spec: String, price: String, note: String) {
        val item = PurchaseItem(id = System.currentTimeMillis().toString(), name = name, category = category, spec = spec, price = price, note = note)
        val list = _purchases.value + item
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch { cloudSync.savePurchases(list) }
    }

    fun togglePurchaseBought(id: String) {
        val list = _purchases.value.map { if (it.id == id) it.copy(bought = !it.bought) else it }
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch { cloudSync.savePurchases(list) }
    }

    fun deletePurchase(id: String) {
        val list = _purchases.value.filter { it.id != id }
        _purchases.value = list
        dataStore.savePurchases(list)
        viewModelScope.launch { cloudSync.savePurchases(list) }
    }

    // === Weight ===
    fun addWeight(weight: String, date: String, note: String) {
        val item = WeightRecord(id = System.currentTimeMillis().toString(), weight = weight, date = date, note = note)
        val list = _weights.value + item
        _weights.value = list
        dataStore.saveWeights(list)
        viewModelScope.launch { cloudSync.saveWeights(list) }
    }

    fun deleteWeight(id: String) {
        val list = _weights.value.filter { it.id != id }
        _weights.value = list
        dataStore.saveWeights(list)
        viewModelScope.launch { cloudSync.saveWeights(list) }
    }

    // === Cloud Sync ===
    fun syncFromCloud() {
        viewModelScope.launch {
            cloudSync.syncAll { progress ->
                _syncProgress.value = progress
            }
            loadAll()
            _syncProgress.value = ""
        }
    }

    fun uploadToCloud() {
        viewModelScope.launch {
            cloudSync.uploadAll(
                todos = _todos.value,
                purchases = _purchases.value,
                weights = _weights.value
            ) { progress ->
                _syncProgress.value = progress
            }
            _syncProgress.value = ""
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(application) as T
    }
}