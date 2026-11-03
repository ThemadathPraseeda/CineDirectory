package com.pt.apps.cinedirectory.domain

import com.pt.apps.cinedirectory.domain.model.FilterType
import com.pt.apps.cinedirectory.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMoviesFlow(
        query: String = "",
        genre: String? = null,
        filterType: FilterType = FilterType.ALL
    ): Flow<List<Movie>>

    fun getMovieByIdFlow(id: String): Flow<Movie?>

    suspend fun refreshMovies(): Result<Unit>

    suspend fun loadNextPage(): Result<Boolean>

    suspend fun toggleFavorite(movieId: String)

    suspend fun toggleWatchlist(movieId: String)

    suspend fun getFavoriteMovies(): List<Movie>

    suspend fun getWatchlistMovies(): List<Movie>
}
