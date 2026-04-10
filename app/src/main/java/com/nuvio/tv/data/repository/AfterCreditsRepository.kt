package com.nuvio.tv.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AfterCreditsRepository @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun getAfterCreditsInfo(imdbId: String): String? = withContext(Dispatchers.IO) {
        if (imdbId.isBlank()) return@withContext null
        try {
            val request = Request.Builder()
                .url("https://aftercredits.almosteffective.com/stream/movie/$imdbId.json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null

                val body = response.body?.string() ?: return@use null
                val json = JSONObject(body)
                val streams = json.optJSONArray("streams") ?: return@use null
                if (streams.length() == 0) return@use null

                val firstStream = streams.getJSONObject(0)
                val title = firstStream.optString("title", "")
                if (title.isBlank() || title == "No Stingers Found") return@use null

                title
            }
        } catch (e: Exception) {
            Log.e("AfterCreditsRepository", "Failed to fetch after credits info for $imdbId", e)
            null
        }
    }
}
