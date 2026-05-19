package com.nuvio.tv.core.tmdb

object AddonRequestIdResolver {
    suspend fun resolve(
        tmdbService: TmdbService,
        mediaType: String,
        id: String?
    ): String? {
        return resolve(mediaType, id) { tmdbId, lookupType ->
            tmdbService.tmdbToImdb(tmdbId, lookupType)
        }
    }

    internal suspend fun resolve(
        mediaType: String,
        id: String?,
        tmdbToImdb: suspend (tmdbId: Int, mediaType: String) -> String?
    ): String? {
        val rawId = id?.trim()?.takeIf { it.isNotBlank() } ?: return id
        if (!rawId.startsWith("tmdb:", ignoreCase = true)) return rawId

        val withoutPrefix = rawId.substringAfter(":")
        val tmdbIdPart = withoutPrefix.substringBefore(":")
        val tmdbId = tmdbIdPart.toIntOrNull() ?: return rawId
        val suffix = withoutPrefix.removePrefix(tmdbIdPart)
        val imdbId = tmdbToImdb(tmdbId, mediaType)?.takeIf { it.isNotBlank() }
            ?: return rawId
        return imdbId + suffix
    }
}
