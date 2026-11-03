package com.pt.apps.cinedirectory.domain.model

data class Movie(
    val id: String,
    val rank: Int,
    val title: String,
    val originalTitle: String = title,
    val year: Int,
    val rating: Double,
    val voteCount: String,
    val duration: String,
    val genres: List<String>,
    val director: String,
    val cast: List<String>,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String = "",
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false
)

enum class FilterType {
    ALL,
    FAVORITES,
    WATCHLIST
}
