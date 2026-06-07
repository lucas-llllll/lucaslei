package com.lucaslei.app

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.lucaslei.cyou"
    private const val USER_AGENT = "LucasleiApp/1.0 (Android)"
    private const val AUTH_TOKEN = "Bearer lucaslei"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Authorization", AUTH_TOKEN)
                .build()
            chain.proceed(req)
        }
        .build()

    private val gson = Gson()

    // === GET ===
    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        withTimeout(20000) {
            val req = Request.Builder()
                .url("$BASE_URL$path")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                Log.e("ApiClient", "GET $path -> HTTP ${resp.code}: $body")
                throw Exception("HTTP ${resp.code}")
            }
            resp.body?.string() ?: ""
        }
    }

    // === PUT (JSON body) ===
    suspend fun put(path: String, body: Any): String = withContext(Dispatchers.IO) {
        withTimeout(20000) {
            val json = gson.toJson(body)
            val reqBody = json.toRequestBody(JSON_MEDIA_TYPE)
            val req = Request.Builder()
                .url("$BASE_URL$path")
                .put(reqBody)
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val respBody = resp.body?.string() ?: ""
                Log.e("ApiClient", "PUT $path -> HTTP ${resp.code}: $respBody")
                throw Exception("HTTP ${resp.code}")
            }
            resp.body?.string() ?: ""
        }
    }

    // === PUT form field (for single-value KV) ===
    suspend fun putValue(path: String, value: String): String = withContext(Dispatchers.IO) {
        withTimeout(20000) {
            val reqBody = value.toRequestBody(JSON_MEDIA_TYPE)
            val req = Request.Builder()
                .url("$BASE_URL$path")
                .put(reqBody)
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val respBody = resp.body?.string() ?: ""
                Log.e("ApiClient", "PUT $path -> HTTP ${resp.code}: $respBody")
                throw Exception("HTTP ${resp.code}")
            }
            resp.body?.string() ?: ""
        }
    }

    // === DELETE ===
    suspend fun delete(path: String): String = withContext(Dispatchers.IO) {
        withTimeout(20000) {
            val req = Request.Builder()
                .url("$BASE_URL$path")
                .delete()
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                Log.e("ApiClient", "DELETE $path -> HTTP ${resp.code}: $body")
                throw Exception("HTTP ${resp.code}")
            }
            resp.body?.string() ?: ""
        }
    }

    // === Helpers ===
    fun parseJsonList(json: String): List<Map<String, Any?>> {
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any?>>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseJsonObject(json: String): Map<String, Any?> {
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any?>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun toJson(obj: Any): String = gson.toJson(obj)
}
