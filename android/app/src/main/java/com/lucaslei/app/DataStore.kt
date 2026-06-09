package com.lucaslei.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

// === 数据模型 ===
// 所有可能缺失的 String 字段用 String? 防止 Gson 反序列化 NPE

data class PurchaseItem(
    val id: String? = null,
    val name: String? = null,
    val brand: String? = null,
    val spec: String? = null,
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val channel: String? = null,
    val contact: String? = null,
    val status: String? = null,
    val expectDate: String? = null,
    val actualDate: String? = null,
    val cat: String? = null,
    val budget: String? = null,
    val note: String? = null,
    val updatedAt: String? = null
)

data class TodoItem(
    val id: String? = null,
    val text: String? = null,
    val deadline: String? = null,
    val done: Boolean = false,
    val priority: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class WeightRecord(
    val id: String? = null,
    val weight: Double = 0.0,
    val date: String? = null,
    val note: String? = null,
    val updatedAt: String? = null
)

data class WeightInjRecord(
    val id: String? = null,
    val date: String? = null,
    val dose: Any? = null,     // 云端: 0.5(number) 或 ""(string)
    val side: String? = null,
    val note: String? = null,
    val seq: Int = 0,
    val updatedAt: String? = null
)

data class Recipe(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val ingredients: String? = null,
    val steps: String? = null,
    val favorite: Boolean = false,
    val updatedAt: String? = null
)

// === 本地存储 ===

class DataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lucaslei_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadPurchases(): List<PurchaseItem> {
        return try {
            val json = prefs.getString("purchases", "[]") ?: "[]"
            gson.fromJson(json, object : TypeToken<List<PurchaseItem>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun savePurchases(list: List<PurchaseItem>) {
        prefs.edit().putString("purchases", gson.toJson(list)).apply()
    }

    fun loadTodos(): List<TodoItem> {
        return try {
            val json = prefs.getString("todos", "[]") ?: "[]"
            gson.fromJson(json, object : TypeToken<List<TodoItem>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun saveTodos(list: List<TodoItem>) {
        prefs.edit().putString("todos", gson.toJson(list)).apply()
    }

    fun loadWeights(): List<WeightRecord> {
        return try {
            val json = prefs.getString("weights", "[]") ?: "[]"
            gson.fromJson(json, object : TypeToken<List<WeightRecord>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun saveWeights(list: List<WeightRecord>) {
        prefs.edit().putString("weights", gson.toJson(list)).apply()
    }

    fun loadInjections(): List<WeightInjRecord> {
        return try {
            val json = prefs.getString("injections", "[]") ?: "[]"
            gson.fromJson(json, object : TypeToken<List<WeightInjRecord>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun saveInjections(list: List<WeightInjRecord>) {
        prefs.edit().putString("injections", gson.toJson(list)).apply()
    }

    fun loadRecipes(): List<Recipe> {
        return try {
            val json = prefs.getString("recipes", "[]") ?: "[]"
            gson.fromJson(json, object : TypeToken<List<Recipe>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }
    fun saveRecipes(list: List<Recipe>) {
        prefs.edit().putString("recipes", gson.toJson(list)).apply()
    }
}
