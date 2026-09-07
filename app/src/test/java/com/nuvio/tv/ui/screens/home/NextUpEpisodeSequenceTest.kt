package com.nuvio.tv.ui.screens.home

import com.nuvio.tv.domain.model.WatchProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class NextUpEpisodeSequenceTest {
    private val today = LocalDate.of(2026, 9, 7)
    private val seed = WatchProgress(
        contentId = "series", contentType = "series", name = "Gogglebox",
        poster = null, backdrop = null, logo = null, videoId = "28:1",
        season = 28, episode = 1, episodeTitle = null,
        position = 100, duration = 100, lastWatched = 0
    )

    private fun video(episode: Int, released: String? = "2026-09-01", season: Int = 28,
                      available: Boolean? = null) = CwVideoSummary(
        id = "$season:$episode", title = "Episode $episode", released = released,
        thumbnail = null, season = season, episode = episode, overview = null,
        available = available
    )

    private fun resolve(videos: List<CwVideoSummary>, showUnaired: Boolean = false) =
        resolveNextUpVideoFromMeta(
            seed,
            CwMetaSummary(
                id = "series", name = "Gogglebox", poster = null, backdropUrl = null,
                logo = null, description = null, genres = emptyList(), releaseInfo = null,
                imdbRating = null, language = null, country = null, videos = videos
            ),
            showUnairedNextUp = showUnaired,
            todayLocal = today
        )

    @Test fun `does not skip future episodes to an incorrectly dated episode six`() {
        val videos = listOf(video(1)) + (2..5).map { video(it, "2026-09-14") } + video(6)
        assertNull(resolve(videos))
        assertEquals(2, resolve(videos, showUnaired = true)?.episode)
    }

    @Test fun `does not skip unavailable undated episode when upcoming is enabled`() {
        assertNull(resolve(listOf(video(1), video(2, null, available = false), video(6)), true))
    }

    @Test fun `does not bridge a gap in metadata`() {
        assertNull(resolve(listOf(video(1), video(6))))
    }

    @Test fun `returns immediate aired successor even when provider order is reversed`() {
        assertEquals(2, resolve(listOf(video(3), video(2), video(1)))?.episode)
    }

    @Test fun `missing date respects upcoming setting without skipping ahead`() {
        val videos = listOf(video(1), video(2, null), video(3))
        assertNull(resolve(videos))
        assertEquals(2, resolve(videos, true)?.episode)
    }

    @Test fun `season premiere retains seven day upcoming window`() {
        assertNull(resolve(listOf(video(1), video(1, "2026-09-15", 29)), true))
        assertEquals(29, resolve(listOf(video(1), video(1, "2026-09-14", 29)), true)?.season)
        assertNull(resolve(listOf(video(1), video(1, "2026-09-14", 29))))
        assertEquals(29, resolve(listOf(video(1), video(1, "2026-09-01", 29)))?.season)
    }

    @Test fun `does not skip an undated premiere to episode two`() {
        assertNull(resolve(listOf(video(1), video(1, null, 29), video(2, season = 29)), true))
    }

    @Test fun `no successor leaves next up empty`() {
        assertNull(resolve(listOf(video(1))))
    }
}
