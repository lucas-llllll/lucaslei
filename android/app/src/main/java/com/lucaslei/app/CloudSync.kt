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

    // === 采购 (云端: /data/zx) ===

    suspend fun loadPurchases(): List<PurchaseItem> {
        return try {
            notify("loading", "加载采购数据")
            val json = ApiClient.get("/data/zx")
            Log.d("CloudSync", "Purchase response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") return emptyList<PurchaseItem>()
            val type = object : TypeToken<List<PurchaseItem>>() {}.type
            val list: List<PurchaseItem> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条采购")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Purchase parse error", e)
            notify("err", "数据格式错误")
            emptyList<PurchaseItem>()
        } catch (e: Exception) {
            Log.e("CloudSync", "Purchase load error: ${e.message}", e)
            notify("err", "离线")
            emptyList<PurchaseItem>()
        }
    }

    suspend fun uploadPurchases(localList: List<PurchaseItem>): Boolean {
        return try {
            notify("saving", "上传采购数据")
            // 先拉云端，合并后再写，防止覆盖
            val cloudList = try {
                val json = ApiClient.get("/data/zx")
                if (json.isBlank() || json == "[]") emptyList<PurchaseItem>()
                else gson.fromJson(json, object : TypeToken<List<PurchaseItem>>() {}.type)
            } catch (e: Exception) { emptyList<PurchaseItem>() }
            val merged = mergeById(localList, cloudList)
            ApiClient.put("/data/zx", merged)
            notify("ok", "采购数据已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Purchase upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 待办 (云端: /data/todo) ===

    suspend fun loadTodos(): List<TodoItem> {
        return try {
            notify("loading", "加载待办")
            val json = ApiClient.get("/data/todo")
            Log.d("CloudSync", "Todo response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") return emptyList<TodoItem>()
            val type = object : TypeToken<List<TodoItem>>() {}.type
            val list: List<TodoItem> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条待办")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Todo parse error", e)
            notify("err", "数据格式错误")
            emptyList<TodoItem>()
        } catch (e: Exception) {
            Log.e("CloudSync", "Todo load error: ${e.message}", e)
            notify("err", "离线")
            emptyList<TodoItem>()
        }
    }

    suspend fun uploadTodos(localList: List<TodoItem>): Boolean {
        return try {
            notify("saving", "上传待办")
            val cloudList = try {
                val json = ApiClient.get("/data/todo")
                if (json.isBlank() || json == "[]") emptyList<TodoItem>()
                else gson.fromJson(json, object : TypeToken<List<TodoItem>>() {}.type)
            } catch (e: Exception) { emptyList<TodoItem>() }
            val merged = mergeById(localList, cloudList)
            ApiClient.put("/data/todo", merged)
            notify("ok", "待办已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Todo upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 体重 (云端: /data/weight_wt) ===

    suspend fun loadWeights(): List<WeightRecord> {
        return try {
            notify("loading", "加载体重")
            val json = ApiClient.get("/data/weight_wt")
            Log.d("CloudSync", "Weight response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") return emptyList<WeightRecord>()
            val type = object : TypeToken<List<WeightRecord>>() {}.type
            val list: List<WeightRecord> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条体重")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Weight parse error", e)
            notify("err", "数据格式错误")
            emptyList<WeightRecord>()
        } catch (e: Exception) {
            Log.e("CloudSync", "Weight load error: ${e.message}", e)
            notify("err", "离线")
            emptyList<WeightRecord>()
        }
    }

    suspend fun uploadWeights(localList: List<WeightRecord>): Boolean {
        return try {
            notify("saving", "上传体重")
            val cloudList = try {
                val json = ApiClient.get("/data/weight_wt")
                if (json.isBlank() || json == "[]") emptyList<WeightRecord>()
                else gson.fromJson(json, object : TypeToken<List<WeightRecord>>() {}.type)
            } catch (e: Exception) { emptyList<WeightRecord>() }
            val merged = mergeById(localList, cloudList)
            ApiClient.put("/data/weight_wt", merged)
            notify("ok", "体重已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Weight upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 注射 (云端: /data/weight_ij) ===

    suspend fun loadInjections(): List<WeightInjRecord> {
        return try {
            notify("loading", "加载注射记录")
            val json = ApiClient.get("/data/weight_ij")
            Log.d("CloudSync", "Injection response: ${json.take(300)}")
            if (json.isBlank() || json == "[]") return emptyList<WeightInjRecord>()
            val type = object : TypeToken<List<WeightInjRecord>>() {}.type
            val list: List<WeightInjRecord> = gson.fromJson(json, type)
            notify("ok", "已同步 ${list.size} 条注射记录")
            list
        } catch (e: JsonSyntaxException) {
            Log.e("CloudSync", "Injection parse error", e)
            notify("err", "数据格式错误")
            emptyList<WeightInjRecord>()
        } catch (e: Exception) {
            Log.e("CloudSync", "Injection load error: ${e.message}", e)
            notify("err", "离线")
            emptyList<WeightInjRecord>()
        }
    }

    suspend fun uploadInjections(localList: List<WeightInjRecord>): Boolean {
        return try {
            notify("saving", "上传注射记录")
            val cloudList = try {
                val json = ApiClient.get("/data/weight_ij")
                if (json.isBlank() || json == "[]") emptyList<WeightInjRecord>()
                else gson.fromJson(json, object : TypeToken<List<WeightInjRecord>>() {}.type)
            } catch (e: Exception) { emptyList<WeightInjRecord>() }
            val merged = mergeById(localList, cloudList)
            ApiClient.put("/data/weight_ij", merged)
            notify("ok", "注射记录已同步")
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Injection upload error: ${e.message}", e)
            notify("err", "上传失败")
            false
        }
    }

    // === 全量同步 ===

    suspend fun syncAll(
        localTodos: List<TodoItem>,
        localPurchases: List<PurchaseItem>,
        localWeights: List<WeightRecord>,
        localInjections: List<WeightInjRecord>,
        onProgress: (String) -> Unit = {}
    ): Quadruple<List<TodoItem>, List<PurchaseItem>, List<WeightRecord>, List<WeightInjRecord>> {

        var mergedPurchases = localPurchases
        var mergedTodos = localTodos
        var mergedWeights = localWeights
        var mergedInjections = localInjections

        // Download all
        try {
            onProgress("正在下载采购数据...")
            val cloudPurchases = loadPurchases()
            if (cloudPurchases.isNotEmpty()) mergedPurchases = mergeById(localPurchases, cloudPurchases)
        } catch (e: Exception) { Log.e("CloudSync", "Sync purchases download failed: ${e.message}") }

        try {
            onProgress("正在下载待办...")
            val cloudTodos = loadTodos()
            if (cloudTodos.isNotEmpty()) mergedTodos = mergeById(localTodos, cloudTodos)
        } catch (e: Exception) { Log.e("CloudSync", "Sync todos download failed: ${e.message}") }

        try {
            onProgress("正在下载体重...")
            val cloudWeights = loadWeights()
            if (cloudWeights.isNotEmpty()) mergedWeights = mergeById(localWeights, cloudWeights)
        } catch (e: Exception) { Log.e("CloudSync", "Sync weights download failed: ${e.message}") }

        try {
            onProgress("正在下载注射记录...")
            val cloudInjections = loadInjections()
            if (cloudInjections.isNotEmpty()) mergedInjections = mergeById(localInjections, cloudInjections)
        } catch (e: Exception) { Log.e("CloudSync", "Sync injections download failed: ${e.message}") }

        // Upload merged
        try { onProgress("正在上传采购数据..."); uploadPurchases(mergedPurchases) } catch (e: Exception) { Log.e("CloudSync", "Sync purchases upload failed: ${e.message}") }
        try { onProgress("正在上传待办..."); uploadTodos(mergedTodos) } catch (e: Exception) { Log.e("CloudSync", "Sync todos upload failed: ${e.message}") }
        try { onProgress("正在上传体重..."); uploadWeights(mergedWeights) } catch (e: Exception) { Log.e("CloudSync", "Sync weights upload failed: ${e.message}") }
        try { onProgress("正在上传注射记录..."); uploadInjections(mergedInjections) } catch (e: Exception) { Log.e("CloudSync", "Sync injections upload failed: ${e.message}") }

        onProgress("同步完成")
        return Quadruple(mergedTodos, mergedPurchases, mergedWeights, mergedInjections)
    }

    // === 全量上传 ===

    suspend fun uploadAll(
        todos: List<TodoItem>,
        purchases: List<PurchaseItem>,
        weights: List<WeightRecord>,
        injections: List<WeightInjRecord>,
        onProgress: (String) -> Unit = {}
    ) {
        try { onProgress("正在上传采购..."); uploadPurchases(purchases) } catch (e: Exception) { onProgress("上传失败"); return }
        try { onProgress("正在上传待办..."); uploadTodos(todos) } catch (e: Exception) { onProgress("上传失败"); return }
        try { onProgress("正在上传体重..."); uploadWeights(weights) } catch (e: Exception) { onProgress("上传失败"); return }
        try { onProgress("正在上传注射记录..."); uploadInjections(injections) } catch (e: Exception) { onProgress("上传失败"); return }
        onProgress("上传完成")
    }

    // === Merge: cloud wins for same ID ===
    private fun <T : Any> mergeById(local: List<T>, cloud: List<T>): List<T> {
        val result = mutableMapOf<String, T>()
        local.forEach { item ->
            val id = getId(item)
            if (id.isNotBlank()) result[id] = item
        }
        cloud.forEach { item ->
            val id = getId(item)
            if (id.isNotBlank()) {
                result[id] = item  // cloud wins
            }
        }
        return result.values.toList()
    }

    private fun <T : Any> getId(item: T): String {
        return try {
            val field = item::class.java.getDeclaredField("id")
            field.isAccessible = true
            val value = field.get(item)
            value?.toString() ?: ""
        } catch (e: Exception) { "" }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
