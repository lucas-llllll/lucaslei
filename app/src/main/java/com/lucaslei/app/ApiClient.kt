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

    private fun url(path: String) = "$BASE_URL$path"

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url(path)).addHeader("Authorization", "Bearer lucaslei").get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }

    suspend fun post(path: String, body: Any): String = withContext(Dispatchers.IO) {
        val json = gson.toJson(body)
        val reqBody = json.toRequestBody(JSON)
        val req = Request.Builder().url(url(path)).addHeader("Authorization", "Bearer lucaslei").post(reqBody).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }

    suspend fun put(path: String, body: Any): String = withContext(Dispatchers.IO) {
        val json = gson.toJson(body)
        val reqBody = json.toRequestBody(JSON)
        val req = Request.Builder().url(url(path)).addHeader("Authorization", "Bearer lucaslei").put(reqBody).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }

    suspend fun delete(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url(path)).addHeader("Authorization", "Bearer lucaslei").delete().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }
}
