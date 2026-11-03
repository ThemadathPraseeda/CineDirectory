package com.pt.apps.cinedirectory.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDto(
    @Json(name = "id") val id: String,
    @Json(name = "rank") val rank: Int,
    @Json(name = "title") val title: String,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "year") val year: Int,
    @Json(name = "rating") val rating: Double,
    @Json(name = "vote_count") val voteCount: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "genres") val genres: List<String>,
    @Json(name = "director") val director: String,
    @Json(name = "cast") val cast: List<String>,
    @Json(name = "overview") val overview: String,
    @Json(name = "poster_url") val posterUrl: String,
    @Json(name = "backdrop_url") val backdropUrl: String,
    @Json(name = "trailer_url") val trailerUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class MoviePageResponseDto(
    @Json(name = "page") val page: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int,
    @Json(name = "results") val results: List<MovieDto>
)
