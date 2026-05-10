package com.nuvio.tv.data.repository

import android.util.Log
import com.nuvio.tv.data.remote.api.TraktApi
import com.nuvio.tv.data.remote.dto.trakt.TraktRatedEpisodeDto
import com.nuvio.tv.data.remote.dto.trakt.TraktRatedMovieDto
import com.nuvio.tv.data.remote.dto.trakt.TraktRatedSeasonDto
import com.nuvio.tv.data.remote.dto.trakt.TraktRatedShowDto
import com.nuvio.tv.data.remote.dto.trakt.TraktRatingsRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktRatingService @Inject constructor(
    private val traktApi: TraktApi,
    private val traktAuthService: TraktAuthService
) {
    companion object {
        private const val TAG = "TraktRatingService"
    }

    suspend fun getExistingRating(item: TraktScrobbleItem): Int? {
        if (!traktAuthService.getCurrentAuthState().isAuthenticated) return null
        if (!traktAuthService.hasRequiredCredentials()) return null

        val type = when (item) {
            is TraktScrobbleItem.Movie -> "movies"
            is TraktScrobbleItem.Episode -> "episodes"
        }

        val response = traktAuthService.executeAuthorizedRequest { authHeader ->
            traktApi.getRatings(authHeader, type)
        } ?: return null

        if (!response.isSuccessful) {
            Log.w(TAG, "Failed to load existing rating: ${response.code()}")
            return null
        }

        val ratings = response.body().orEmpty()
        return ratings.firstOrNull { rating ->
            when (item) {
                is TraktScrobbleItem.Movie -> {
                    val movie = rating.movie
                    movie?.ids?.imdb?.let { it == item.ids.imdb } == true ||
                        movie?.ids?.tmdb?.let { it == item.ids.tmdb } == true ||
                        movie?.ids?.trakt?.let { it == item.ids.trakt } == true
                }
                is TraktScrobbleItem.Episode -> {
                    val show = rating.show
                    val episode = rating.episode
                    episode?.season == item.season &&
                        episode.number == item.number &&
                        (
                            show?.ids?.imdb?.let { it == item.showIds.imdb } == true ||
                                show?.ids?.tmdb?.let { it == item.showIds.tmdb } == true ||
                                show?.ids?.trakt?.let { it == item.showIds.trakt } == true
                            )
                }
            }
        }?.rating?.coerceIn(1, 10)
    }

    suspend fun submitRating(item: TraktScrobbleItem, rating: Int): Boolean {
        if (!traktAuthService.getCurrentAuthState().isAuthenticated) return false
        if (!traktAuthService.hasRequiredCredentials()) return false

        val clamped = rating.coerceIn(1, 10)
        val request = when (item) {
            is TraktScrobbleItem.Movie -> TraktRatingsRequestDto(
                movies = listOf(
                    TraktRatedMovieDto(
                        title = item.title,
                        year = item.year,
                        ids = item.ids,
                        rating = clamped
                    )
                )
            )
            is TraktScrobbleItem.Episode -> TraktRatingsRequestDto(
                shows = listOf(
                    TraktRatedShowDto(
                        title = item.showTitle,
                        year = item.showYear,
                        ids = item.showIds,
                        seasons = listOf(
                            TraktRatedSeasonDto(
                                number = item.season,
                                episodes = listOf(
                                    TraktRatedEpisodeDto(
                                        title = item.episodeTitle,
                                        number = item.number,
                                        rating = clamped
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }

        val response = traktAuthService.executeAuthorizedWriteRequest { authHeader ->
            traktApi.addRatings(authHeader, request)
        } ?: return false

        val success = response.isSuccessful || response.code() == 201
        if (!success) {
            Log.w(TAG, "Failed to submit rating: ${response.code()}")
        }
        return success
    }
}
