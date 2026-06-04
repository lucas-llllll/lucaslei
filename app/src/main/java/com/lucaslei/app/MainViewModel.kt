package com.lucaslei.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val dataStore: DataStore) : ViewModel() {

    // === Todo ===
    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    // === Recipes ===
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    // === Purchases ===
    private val _purchases = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val purchases: StateFlow<List<PurchaseItem>> = _purchases.asStateFlow()

    // === Weights ===
    private val _weights = MutableStateFlow<List<WeightRecord>>(emptyList())
    val weights: StateFlow<List<WeightRecord>> = _weights.asStateFlow()

    // === Loading state ===
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        _todos.value = dataStore.loadTodos()
        _recipes.value = dataStore.loadRecipes()
        _purchases.value = dataStore.loadPurchases()
        _weights.value = dataStore.loadWeights()
    }

    // === Todo operations ===
    fun addTodo(text: String) {
        val item = TodoItem(id = System.currentTimeMillis().toString(), text = text)
        val list = _todos.value + item
        _todos.value = list
        dataStore.saveTodos(list)
    }

    fun toggleTodo(id: String) {
        val list = _todos.value.map {
            if (it.id == id) it.copy(done = !it.done) else it
        }
        _todos.value = list
        dataStore.saveTodos(list)
    }

    fun deleteTodo(id: String) {
        val list = _todos.value.filter { it.id != id }
        _todos.value = list
        dataStore.saveTodos(list)
    }

    // === Recipe operations ===
    fun addRecipe(name: String, category: String, ingredients: String, steps: String) {
        val item = Recipe(id = System.currentTimeMillis().toString(), name = name, category = category, ingredients = ingredients, steps = steps)
        val list = _recipes.value + item
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    fun toggleRecipeFavorite(id: String) {
        val list = _recipes.value.map {
            if (it.id == id) it.copy(favorite = !it.favorite) else it
        }
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    fun deleteRecipe(id: String) {
        val list = _recipes.value.filter { it.id != id }
        _recipes.value = list
        dataStore.saveRecipes(list)
    }

    // === Purchase operations ===
    fun addPurchase(name: String, category: String, spec: String, price: String, note: String) {
        val item = PurchaseItem(id = System.currentTimeMillis().toString(), name = name, category = category, spec = spec, price = price, note = note)
        val list = _purchases.value + item
        _purchases.value = list
        dataStore.savePurchases(list)
    }

    fun togglePurchaseBought(id: String) {
        val list = _purchases.value.map {
            if (it.id == id) it.copy(bought = !it.bought) else it
        }
        _purchases.value = list
        dataStore.savePurchases(list)
    }

    fun deletePurchase(id: String) {
        val list = _purchases.value.filter { it.id != id }
        _purchases.value = list
        dataStore.savePurchases(list)
    }

    // === Weight operations ===
    fun addWeight(weight: String, date: String, note: String) {
        val item = WeightRecord(id = System.currentTimeMillis().toString(), weight = weight, date = date, note = note)
        val list = _weights.value + item
        _weights.value = list
        dataStore.saveWeights(list)
    }

    fun deleteWeight(id: String) {
        val list = _weights.value.filter { it.id != id }
        _weights.value = list
        dataStore.saveWeights(list)
    }

    // === Sync with CF API ===
    fun syncFromCloud() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Try to fetch from each subdomain and merge
                // For now, this is a placeholder - actual API integration
                // will be added after inspecting the CF Workers
            } catch (e: Exception) {
                // Keep local data on failure
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class MainViewModelFactory(private val dataStore: DataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(dataStore) as T
    }
}
