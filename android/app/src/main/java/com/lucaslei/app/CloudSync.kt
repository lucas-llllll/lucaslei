package com.lucaslei.app

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class CloudSync(private val dataStore: DataStore) {
    private val gson = Gson()
    private var syncListener: ((String, String) -> Unit)? = null

    fun setListener(listener: (String, String) -> Unit) {
        syncListener = listener
    }

    private fun notify(status: String, text: String) {
        syncListener?.invoke(status, text)
    }

    // === 采购同步 (云端: /data/memo) ===

    suspend fun loadPurchases(): List<PurchaseItem> {
        return try {
            notify("loading", "加载采购数据")
            val json = ApiClient.get("/data/memo")
            Log.d("CloudSync", "Purchase response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") {
                return emptyList()
            }
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            val list: List<PurchaseItem> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条采购")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Purchase parse error", e)
            notify("err", "数据格式错误")
            emptyList()
        } catch (e: Exception) {
            Log.e("CloudSync", "Purchase load error: ${e.message}", e)
            notify("err", "离线")
            emptyList()
        }
    }

    suspend fun uploadPurchases(list: List<PurchaseItem>): Boolean {
        return try {
            notify("saving", "上传采购数据")
            ApiClient.put("/data/memo", list)
            notify("ok", "采购数据已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Purchase upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 待办同步 (云端: /data/todo) ===

    suspend fun loadTodos(): List<TodoItem> {
        return try {
            notify("loading", "加载待办")
            val json = ApiClient.get("/data/todo")
            Log.d("CloudSync", "Todo response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") {
                return emptyList()
            }
            val type = object : TypeToken<List<TodoItem>>() {}.type
            val list: List<TodoItem> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条待办")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Todo parse error", e)
            notify("err", "数据格式错误")
            emptyList()
        } catch (e: Exception) {
            Log.e("CloudSync", "Todo load error: ${e.message}", e)
            notify("err", "离线")
            emptyList()
        }
    }

    suspend fun uploadTodos(list: List<TodoItem>): Boolean {
        return try {
            notify("saving", "上传待办")
            ApiClient.put("/data/todo", list)
            notify("ok", "待办已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Todo upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 体重同步 (云端: /data/weight) ===

    suspend fun loadWeights(): List<WeightRecord> {
        return try {
            notify("loading", "加载体重")
            val json = ApiClient.get("/data/weight")
            Log.d("CloudSync", "Weight response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") {
                return emptyList()
            }
            val type = object : TypeToken<List<WeightRecord>>() {}.type
            val list: List<WeightRecord> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条体重")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Weight parse error", e)
            notify("err", "数据格式错误")
            emptyList()
        } catch (e: Exception) {
            Log.e("CloudSync", "Weight load error: ${e.message}", e)
            notify("err", "离线")
            emptyList()
        }
    }

    suspend fun uploadWeights(list: List<WeightRecord>): Boolean {
        return try {
            notify("saving", "上传体重")
            ApiClient.put("/data/weight", list)
            notify("ok", "体重已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Weight upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 全量同步：先从云端拉取，合并后上传 ===

    suspend fun syncAll(
        localTodos: List<TodoItem>,
        localPurchases: List<PurchaseItem>,
        localWeights: List<WeightRecord>,
        onProgress: (String) -> Unit = {}
    ): Triple<List<TodoItem>, List<PurchaseItem>, List<WeightRecord>> {

        var mergedPurchases = localPurchases
        var mergedTodos = localTodos
        var mergedWeights = localWeights

        // Step 1: Download
        try {
            onProgress("正在下载采购数据...")
            val cloudPurchases = loadPurchases()
            if (cloudPurchases.isNotEmpty()) {
                mergedPurchases = mergeById(localPurchases, cloudPurchases)
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync purchases download failed: ${e.message}")
        }

        try {
            onProgress("正在下载待办...")
            val cloudTodos = loadTodos()
            if (cloudTodos.isNotEmpty()) {
                mergedTodos = mergeById(localTodos, cloudTodos)
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync todos download failed: ${e.message}")
        }

        try {
            onProgress("正在下载体重...")
            val cloudWeights = loadWeights()
            if (cloudWeights.isNotEmpty()) {
                mergedWeights = mergeById(localWeights, cloudWeights)
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync weights download failed: ${e.message}")
        }

        // Step 2: Upload merged
        try {
            onProgress("正在上传采购数据...")
            uploadPurchases(mergedPurchases)
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync purchases upload failed: ${e.message}")
        }

        try {
            onProgress("正在上传待办...")
            uploadTodos(mergedTodos)
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync todos upload failed: ${e.message}")
        }

        try {
            onProgress("正在上传体重...")
            uploadWeights(mergedWeights)
        } catch (e: Exception) {
            Log.e("CloudSync", "Sync weights upload failed: ${e.message}")
        }

        onProgress("同步完成")
        return Triple(mergedTodos, mergedPurchases, mergedWeights)
    }

    // === 全量上传（覆盖云端） ===

    suspend fun uploadAll(
        todos: List<TodoItem>,
        purchases: List<PurchaseItem>,
        weights: List<WeightRecord>,
        onProgress: (String) -> Unit = {}
    ) {
        try {
            onProgress("正在上传采购...")
            uploadPurchases(purchases)
        } catch (e: Exception) {
            Log.e("CloudSync", "Upload purchases failed: ${e.message}")
            onProgress("上传失败")
            return
        }

        try {
            onProgress("正在上传待办...")
            uploadTodos(todos)
        } catch (e: Exception) {
            Log.e("CloudSync", "Upload todos failed: ${e.message}")
            onProgress("上传失败")
            return
        }

        try {
            onProgress("正在上传体重...")
            uploadWeights(weights)
        } catch (e: Exception) {
            Log.e("CloudSync", "Upload weights failed: ${e.message}")
            onProgress("上传失败")
            return
        }

        onProgress("上传完成")
    }

    // === Merge: cloud wins for same ID ===
    private fun <T> mergeById(local: List<T>, cloud: List<T>): List<T> {
        val result = mutableMapOf<String, T>()
        local.forEach { item ->
            val id = getId(item)
            if (id.isNotBlank()) result[id] = item
        }
        cloud.forEach { item ->
            val id = getId(item)
            if (id.isNotBlank()) {
                if (!result.containsKey(id)) {
                    result[id] = item
                }
                // local已有的保留local版本
            }
        }
        return result.values.toList()
    }

    private fun getId(item: Any): String {
        return try {
            val field = item::class.java.getDeclaredField("id")
            field.isAccessible = true
            (field.get(item) as? String) ?: ""
        } catch (e: Exception) { "" }
    }
}
