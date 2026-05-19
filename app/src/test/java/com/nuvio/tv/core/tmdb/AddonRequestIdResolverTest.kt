package com.nuvio.tv.core.tmdb

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddonRequestIdResolverTest {
    @Test
    fun `resolves tmdb movie id to imdb id`() = runTest {
        val resolved = AddonRequestIdResolver.resolve(
            mediaType = "movie",
            id = "tmdb:18785"
        ) { tmdbId, mediaType ->
            assertEquals(18785, tmdbId)
            assertEquals("movie", mediaType)
            "tt1119646"
        }

        assertEquals("tt1119646", resolved)
    }

    @Test
    fun `preserves episode suffix for tmdb series video id`() = runTest {
        val resolved = AddonRequestIdResolver.resolve(
            mediaType = "series",
            id = "tmdb:1399:1:2"
        ) { tmdbId, mediaType ->
            assertEquals(1399, tmdbId)
            assertEquals("series", mediaType)
            "tt0944947"
        }

        assertEquals("tt0944947:1:2", resolved)
    }

    @Test
    fun `keeps original id when lookup misses`() = runTest {
        val resolved = AddonRequestIdResolver.resolve(
            mediaType = "movie",
            id = "tmdb:18785"
        ) { _, _ -> null }

        assertEquals("tmdb:18785", resolved)
    }

    @Test
    fun `keeps imdb id unchanged`() = runTest {
        val resolved = AddonRequestIdResolver.resolve(
            mediaType = "movie",
            id = "tt1119646"
        ) { _, _ -> error("TMDB lookup should not be called") }

        assertEquals("tt1119646", resolved)
    }
}
