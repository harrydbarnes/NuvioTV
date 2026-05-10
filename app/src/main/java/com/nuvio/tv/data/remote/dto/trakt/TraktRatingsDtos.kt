package com.nuvio.tv.data.remote.dto.trakt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TraktRatingsRequestDto(
    @Json(name = "movies") val movies: List<TraktRatedMovieDto>? = null,
    @Json(name = "shows") val shows: List<TraktRatedShowDto>? = null,
    @Json(name = "episodes") val episodes: List<TraktRatedEpisodeDto>? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatedMovieDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "year") val year: Int? = null,
    @Json(name = "ids") val ids: TraktIdsDto? = null,
    @Json(name = "rating") val rating: Int
)

@JsonClass(generateAdapter = true)
data class TraktRatedShowDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "year") val year: Int? = null,
    @Json(name = "ids") val ids: TraktIdsDto? = null,
    @Json(name = "seasons") val seasons: List<TraktRatedSeasonDto>? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatedSeasonDto(
    @Json(name = "number") val number: Int? = null,
    @Json(name = "episodes") val episodes: List<TraktRatedEpisodeDto>? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatedEpisodeDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "season") val season: Int? = null,
    @Json(name = "number") val number: Int? = null,
    @Json(name = "rating") val rating: Int
)

@JsonClass(generateAdapter = true)
data class TraktRatingsResponseDto(
    @Json(name = "added") val added: TraktRatingsMutationCountsDto? = null,
    @Json(name = "not_found") val notFound: TraktRatingsRequestDto? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatingsMutationCountsDto(
    @Json(name = "movies") val movies: Int? = null,
    @Json(name = "shows") val shows: Int? = null,
    @Json(name = "episodes") val episodes: Int? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatingItemDto(
    @Json(name = "rated_at") val ratedAt: String? = null,
    @Json(name = "rating") val rating: Int? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "movie") val movie: TraktMovieDto? = null,
    @Json(name = "show") val show: TraktShowDto? = null,
    @Json(name = "episode") val episode: TraktEpisodeDto? = null
)
