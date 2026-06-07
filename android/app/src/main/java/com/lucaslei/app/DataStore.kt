package com.lucaslei.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// === 数据模型 ===

data class PurchaseItem(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val spec: String = "",
    val qty: Int = 0,
    val price: Double = 0.0,
    val channel: String = "",
    val contact: String = "",
    val status: String = "待下单",
    val expectDate: String = "",
    val actualDate: String = "",
    val cat: String = "",
    val budget: String = "",
    val note: String = ""
)

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val deadline: String = "",
    val done: Boolean = false,
    val priority: String = "medium",
    val createdAt: String = ""
)

data class WeightRecord(
    val id: String = "",
    val weight: Double = 0.0,  // kg, Double
    val date: String = "",
    val note: String = ""
)

data class WeightInjRecord(
    val id: String = "",
    val date: String = "",
    val dose: String = "",     // "0.5" 或 ""
    val side: String = "左",   // "左"/"右"
    val note: String = "",
    val seq: Int = 0
)

data class Recipe(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val favorite: Boolean = false
)

// === 本地存储 ===

class DataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lucaslei_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 采购 KV: /data/zx
    fun loadPurchases(): List<PurchaseItem> {
        val json = prefs.getString("purchases", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<PurchaseItem>>() {}.type)
    }
    fun savePurchases(list: List<PurchaseItem>) {
        prefs.edit().putString("purchases", gson.toJson(list)).apply()
    }

    // 待办 KV: /data/todo
    fun loadTodos(): List<TodoItem> {
        val json = prefs.getString("todos", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<TodoItem>>() {}.type)
    }
    fun saveTodos(list: List<TodoItem>) {
        prefs.edit().putString("todos", gson.toJson(list)).apply()
    }

    // 体重 KV: /data/weight_wt
    fun loadWeights(): List<WeightRecord> {
        val json = prefs.getString("weights", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<WeightRecord>>() {}.type)
    }
    fun saveWeights(list: List<WeightRecord>) {
        prefs.edit().putString("weights", gson.toJson(list)).apply()
    }

    // 注射 KV: /data/weight_ij
    fun loadInjections(): List<WeightInjRecord> {
        val json = prefs.getString("injections", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<WeightInjRecord>>() {}.type)
    }
    fun saveInjections(list: List<WeightInjRecord>) {
        prefs.edit().putString("injections", gson.toJson(list)).apply()
    }

    // 菜谱 (纯本地)
    fun loadRecipes(): List<Recipe> {
        val json = prefs.getString("recipes", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<Recipe>>() {}.type)
    }
    fun saveRecipes(list: List<Recipe>) {
        prefs.edit().putString("recipes", gson.toJson(list)).apply()
    }
}
