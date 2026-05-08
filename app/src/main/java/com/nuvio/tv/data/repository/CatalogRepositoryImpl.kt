package com.nuvio.tv.data.repository

import android.util.Log
import com.nuvio.tv.core.network.NetworkResult
import com.nuvio.tv.core.network.safeApiCall
import com.nuvio.tv.data.mapper.toDomain
import com.nuvio.tv.data.remote.api.AddonApi
import com.nuvio.tv.domain.model.CatalogRow
import com.nuvio.tv.domain.model.ContentType
import com.nuvio.tv.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val api: AddonApi
) : CatalogRepository {
    companion object {
        private const val TAG = "CatalogRepository"
        private const val MAX_CACHE_ENTRIES = 80
        private const val CACHE_TTL_MS = 5 * 60 * 1000L
    }

    private data class CatalogCacheKey(
        val url: String,
        val addonId: String,
        val addonName: String,
        val catalogId: String,
        val catalogName: String,
        val type: String,
        val skip: Int,
        val skipStep: Int,
        val supportsSkip: Boolean
    )

    private data class CatalogCacheEntry(
        val row: CatalogRow,
        val cachedAtMs: Long
    )

    private val catalogCache = object : LinkedHashMap<CatalogCacheKey, CatalogCacheEntry>(MAX_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CatalogCacheKey, CatalogCacheEntry>?): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    override fun getCatalog(
        addonBaseUrl: String,
        addonId: String,
        addonName: String,
        catalogId: String,
        catalogName: String,
        type: String,
        skip: Int,
        skipStep: Int,
        extraArgs: Map<String, String>,
        supportsSkip: Boolean
    ): Flow<NetworkResult<CatalogRow>> = flow {
        val url = buildCatalogUrl(addonBaseUrl, type, catalogId, skip, extraArgs)
        val cacheKey = CatalogCacheKey(
            url = url,
            addonId = addonId,
            addonName = addonName,
            catalogId = catalogId,
            catalogName = catalogName,
            type = type,
            skip = skip,
            skipStep = skipStep,
            supportsSkip = supportsSkip
        )
        val now = System.currentTimeMillis()
        synchronized(catalogCache) { catalogCache[cacheKey] }
            ?.takeIf { now - it.cachedAtMs <= CACHE_TTL_MS }
            ?.let { cachedEntry ->
            Log.d(
                TAG,
                "Catalog cache hit addonId=$addonId addonName=$addonName type=$type catalogId=$catalogId skip=$skip url=$url"
            )
            emit(NetworkResult.Success(cachedEntry.row))
            return@flow
        }

        emit(NetworkResult.Loading)
        Log.d(
            TAG,
            "Fetching catalog addonId=$addonId addonName=$addonName type=$type catalogId=$catalogId skip=$skip skipStep=$skipStep supportsSkip=$supportsSkip url=$url"
        )

        when (val result = safeApiCall { api.getCatalog(url) }) {
            is NetworkResult.Success -> {
                val items = result.data.metas.map { it.toDomain() }.distinctBy { it.id }
                Log.d(
                    TAG,
                    "Catalog fetch success addonId=$addonId type=$type catalogId=$catalogId items=${items.size}"
                )

                val effectiveSkipStep = if (skip == 0 && items.isNotEmpty() && items.size < skipStep) {
                    items.size
                } else {
                    skipStep
                }
                val catalogRow = CatalogRow(
                    addonId = addonId,
                    addonName = addonName,
                    addonBaseUrl = addonBaseUrl,
                    catalogId = catalogId,
                    catalogName = catalogName,
                    type = ContentType.fromString(type),
                    rawType = type,
                    items = items,
                    isLoading = false,
                    hasMore = supportsSkip && items.isNotEmpty(),
                    currentPage = if (effectiveSkipStep > 0) skip / effectiveSkipStep else 0,
                    supportsSkip = supportsSkip,
                    skipStep = effectiveSkipStep,
                    extraArgs = extraArgs
                )
                synchronized(catalogCache) {
                    val cacheEntry = CatalogCacheEntry(catalogRow, System.currentTimeMillis())
                    catalogCache[cacheKey] = cacheEntry
                    catalogCache[cacheKey.copy(skipStep = effectiveSkipStep)] = cacheEntry
                }
                emit(NetworkResult.Success(catalogRow))
            }
            is NetworkResult.Error -> {
                Log.w(
                    TAG,
                    "Catalog fetch failed addonId=$addonId type=$type catalogId=$catalogId code=${result.code} message=${result.message} url=$url"
                )
                emit(result)
            }
            NetworkResult.Loading -> { /* Already emitted */ }
        }
    }

    private fun buildCatalogUrl(
        baseUrl: String,
        type: String,
        catalogId: String,
        skip: Int,
        extraArgs: Map<String, String>
    ): String {
        val trimmedBase = baseUrl.trimEnd('/')
        val queryStart = trimmedBase.indexOf('?')
        val basePath = if (queryStart >= 0) trimmedBase.substring(0, queryStart).trimEnd('/') else trimmedBase
        val baseQuery = if (queryStart >= 0) trimmedBase.substring(queryStart) else ""

        val catalogPath = if (extraArgs.isEmpty()) {
            if (skip > 0) {
                "$basePath/catalog/$type/$catalogId/skip=$skip.json"
            } else {
                "$basePath/catalog/$type/$catalogId.json"
            }
        } else {
            val allArgs = LinkedHashMap<String, String>()
            allArgs.putAll(extraArgs)

            if (!allArgs.containsKey("skip") && skip > 0) {
                allArgs["skip"] = skip.toString()
            }

            val encodedArgs = allArgs.entries.joinToString("&") { (key, value) ->
                "${encodeArg(key)}=${encodeArg(value)}"
            }

            "$basePath/catalog/$type/$catalogId/$encodedArgs.json"
        }

        return catalogPath + baseQuery
    }

    private fun encodeArg(value: String): String {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    }
}
