package com.lucaslei.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Recipe(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val favorite: Boolean = false
)

data class PurchaseItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val spec: String = "",
    val price: String = "",
    val bought: Boolean = false,
    val note: String = ""
)

data class WeightRecord(
    val id: String = "",
    val weight: String = "",
    val date: String = "",
    val note: String = ""
)

class DataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lucaslei_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    // === Todo ===
    fun loadTodos(): List<TodoItem> {
        val json = prefs.getString("todos", "[]") ?: "[]"
        val type = object : TypeToken<List<TodoItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveTodos(list: List<TodoItem>) {
        prefs.edit().putString("todos", gson.toJson(list)).apply()
    }

    // === Recipes ===
    fun loadRecipes(): List<Recipe> {
        val json = prefs.getString("recipes", "[]") ?: "[]"
        val type = object : TypeToken<List<Recipe>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveRecipes(list: List<Recipe>) {
        prefs.edit().putString("recipes", gson.toJson(list)).apply()
    }

    // === Purchase ===
    fun loadPurchases(): List<PurchaseItem> {
        val json = prefs.getString("purchases", "[]") ?: "[]"
        val type = object : TypeToken<List<PurchaseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePurchases(list: List<PurchaseItem>) {
        prefs.edit().putString("purchases", gson.toJson(list)).apply()
    }

    // === Weight ===
    fun loadWeights(): List<WeightRecord> {
        val json = prefs.getString("weights", "[]") ?: "[]"
        val type = object : TypeToken<List<WeightRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveWeights(list: List<WeightRecord>) {
        prefs.edit().putString("weights", gson.toJson(list)).apply()
    }
}
