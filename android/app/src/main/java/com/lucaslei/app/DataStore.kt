package com.lucaslei.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

// === 数据模型 ===
// 所有可能缺失的 String 字段用 String? 防止 Gson 反序列化 NPE

data class PurchaseItem(
    val id: String = "",
    val name: String = "",
    val brand: String? = null,
    val spec: String? = null,
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val channel: String? = null,
    val contact: String? = null,
    val status: String = "待下单",
    val expectDate: String? = null,
    val actualDate: String? = null,
    val cat: String = "",
    val budget: String? = null,
    val note: String? = null
)

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val deadline: String? = null,
    val done: Boolean = false,
    val priority: String = "medium",
    val createdAt: String = ""
)

data class WeightRecord(
    val id: String = "",
    val weight: Double = 0.0,
    val date: String = "",
    val note: String? = null
)

data class WeightInjRecord(
    val id: String = "",
    val date: String = "",
    val dose: Any? = null,     // 云端: 0.5(number) 或 ""(string)
    val side: String = "左",
    val note: String? = null,
    val seq: Int = 0
)

data class Recipe(
    val id: String = "",
    val name: String = "",
    val category: String? = null,
    val ingredients: String? = null,
    val steps: String? = null,
    val favorite: Boolean = false
)

// === 本地存储 ===

class DataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lucaslei_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadPurchases(): List<PurchaseItem> {
        val json = prefs.getString("purchases", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<PurchaseItem>>() {}.type)
    }
    fun savePurchases(list: List<PurchaseItem>) {
        prefs.edit().putString("purchases", gson.toJson(list)).apply()
    }

    fun loadTodos(): List<TodoItem> {
        val json = prefs.getString("todos", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<TodoItem>>() {}.type)
    }
    fun saveTodos(list: List<TodoItem>) {
        prefs.edit().putString("todos", gson.toJson(list)).apply()
    }

    fun loadWeights(): List<WeightRecord> {
        val json = prefs.getString("weights", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<WeightRecord>>() {}.type)
    }
    fun saveWeights(list: List<WeightRecord>) {
        prefs.edit().putString("weights", gson.toJson(list)).apply()
    }

    fun loadInjections(): List<WeightInjRecord> {
        val json = prefs.getString("injections", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<WeightInjRecord>>() {}.type)
    }
    fun saveInjections(list: List<WeightInjRecord>) {
        prefs.edit().putString("injections", gson.toJson(list)).apply()
    }

    fun loadRecipes(): List<Recipe> {
        val json = prefs.getString("recipes", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<Recipe>>() {}.type)
    }
    fun saveRecipes(list: List<Recipe>) {
        prefs.edit().putString("recipes", gson.toJson(list)).apply()
    }
}
