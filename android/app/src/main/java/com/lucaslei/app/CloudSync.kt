package com.lucaslei.app

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.lucaslei.cyou"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url("$BASE_URL$path").get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }

    suspend fun put(path: String, body: Any): String = withContext(Dispatchers.IO) {
        val json = gson.toJson(body)
        val reqBody = json.toRequestBody(JSON)
        val req = Request.Builder().url("$BASE_URL$path").put(reqBody).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }
}

class CloudSync(private val dataStore: DataStore) {
    private val gson = Gson()
    private var syncListener: ((String, String) -> Unit)? = null

    fun setListener(listener: (String, String) -> Unit) {
        syncListener = listener
    }

    private fun notify(status: String, text: String) {
        syncListener?.invoke(status, text)
    }

    // === Todo sync ===
    suspend fun loadTodos(): Boolean {
        return try {
            notify("loading", "加载中")
            val json = ApiClient.get("/data/tododata")
            val type = object : TypeToken<Map<String, CategoryData>>() {}.type
            val data: Map<String, CategoryData> = gson.fromJson(json, type)
            // Convert to flat list of TodoItem
            val todos = mutableListOf<TodoItem>()
            data.forEach { (cat, catData) ->
                catData.todo?.forEach { todos.add(it) }
                catData.note?.forEach { todos.add(TodoItem(text = it.text, done = it.done)) }
                catData.comm?.forEach { todos.add(TodoItem(text = it.text, done = it.done)) }
            }
            dataStore.saveTodos(todos)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "离线")
            false
        }
    }

    suspend fun saveTodos(todos: List<TodoItem>): Boolean {
        return try {
            notify("saving", "保存中")
            val data = mapOf<String, Any>()
            ApiClient.put("/data/tododata", data)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "保存失败")
            false
        }
    }

    // === Purchase sync ===
    suspend fun loadPurchases(): Boolean {
        return try {
            notify("loading", "加载中")
            val json = ApiClient.get("/data/purchase")
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            val list: List<PurchaseItem> = gson.fromJson(json, type)
            dataStore.savePurchases(list)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "离线")
            false
        }
    }

    suspend fun savePurchases(list: List<PurchaseItem>): Boolean {
        return try {
            notify("saving", "保存中")
            ApiClient.put("/data/purchase", list)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "保存失败")
            false
        }
    }

    // === Weight sync ===
    suspend fun loadWeights(): Boolean {
        return try {
            notify("loading", "加载中")
            val json = ApiClient.get("/data/weight")
            val type = object : TypeToken<List<WeightRecord>>() {}.type
            val list: List<WeightRecord> = gson.fromJson(json, type)
            dataStore.saveWeights(list)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "离线")
            false
        }
    }

    suspend fun saveWeights(list: List<WeightRecord>): Boolean {
        return try {
            notify("saving", "保存中")
            ApiClient.put("/data/weight", list)
            notify("ok", "已同步")
            true
        } catch (e: Exception) {
            notify("err", "保存失败")
            false
        }
    }

    // Sync all
    suspend fun syncAll(
        onProgress: (String) -> Unit = {}
    ) {
        onProgress("正在同步待办...")
        loadTodos()
        onProgress("正在同步采购...")
        loadPurchases()
        onProgress("正在同步体重...")
        loadWeights()
        onProgress("同步完成")
    }

    suspend fun uploadAll(
        todos: List<TodoItem>,
        purchases: List<PurchaseItem>,
        weights: List<WeightRecord>,
        onProgress: (String) -> Unit = {}
    ) {
        onProgress("正在上传待办...")
        saveTodos(todos)
        onProgress("正在上传采购...")
        savePurchases(purchases)
        onProgress("正在上传体重...")
        saveWeights(weights)
        onProgress("上传完成")
    }
}

data class CategoryData(
    val todo: List<TodoItem>? = null,
    val note: List<NoteItem>? = null,
    val comm: List<NoteItem>? = null
)

data class NoteItem(
    val text: String = "",
    val done: Boolean = false
)