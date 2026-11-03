package com.pt.apps.cinedirectory.data.remote

import com.pt.apps.cinedirectory.data.local.dao.MovieDao
import com.pt.apps.cinedirectory.data.local.model.MovieEntity
import com.pt.apps.cinedirectory.data.remote.api.MovieApiService
import com.pt.apps.cinedirectory.domain.MovieRepository
import com.pt.apps.cinedirectory.domain.model.FilterType
import com.pt.apps.cinedirectory.domain.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MovieRepoImp (
    private val apiService: MovieApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override fun getMoviesFlow(
        query: String,
        genre: String?,
        filterType: FilterType
    ): Flow<List<Movie>> {
        val baseFlow = when (filterType) {
            FilterType.ALL -> movieDao.getAllMoviesFlow()
            FilterType.FAVORITES -> movieDao.getFavoriteMoviesFlow()
            FilterType.WATCHLIST -> movieDao.getWatchlistMoviesFlow()
        }

        return baseFlow.map { entities ->
            entities.map { it.toDomain() }.filter { movie ->
                val matchesQuery = query.isBlank() ||
                        movie.title.contains(query, ignoreCase = true) ||
                        movie.director.contains(query, ignoreCase = true) ||
                        movie.cast.any { it.contains(query, ignoreCase = true) }

                val matchesGenre = genre.isNullOrBlank() ||
                        genre.equals("All", ignoreCase = true) ||
                        movie.genres.any { it.equals(genre, ignoreCase = true) }

                matchesQuery && matchesGenre
            }
        }
    }

    override fun getMovieByIdFlow(id: String): Flow<Movie?> {
        return movieDao.getMovieByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun refreshMovies(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopRatedMovies(page = 1, limit = 10)
            val favIds = movieDao.getAllFavoriteIds().toSet()
            val watchIds = movieDao.getAllWatchlistIds().toSet()

            val entities = response.results.map { dto ->
                MovieEntity.fromDto(
                    dto = dto,
                    page = 1,
                    isFavorite = favIds.contains(dto.id),
                    isInWatchlist = watchIds.contains(dto.id)
                )
            }
            movieDao.insertMovies(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            // If offline, check if we have cached data in Room
            val cachedCount = movieDao.getCount()
            if (cachedCount > 0) {
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun loadNextPage(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentMaxPage = movieDao.getMaxPage() ?: 1
            val nextPage = currentMaxPage + 1
            val response = apiService.getTopRatedMovies(page = nextPage, limit = 10)

            if (response.results.isEmpty() || nextPage > response.totalPages) {
                return@withContext Result.success(false) // No more pages
            }

            val favIds = movieDao.getAllFavoriteIds().toSet()
            val watchIds = movieDao.getAllWatchlistIds().toSet()

            val entities = response.results.map { dto ->
                MovieEntity.fromDto(
                    dto = dto,
                    page = nextPage,
                    isFavorite = favIds.contains(dto.id),
                    isInWatchlist = watchIds.contains(dto.id)
                )
            }
            movieDao.insertMovies(entities)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(movieId: String) = withContext(Dispatchers.IO) {
        val current = movieDao.isFavorite(movieId) ?: false
        movieDao.setFavorite(movieId, !current)
    }

    override suspend fun toggleWatchlist(movieId: String) = withContext(Dispatchers.IO) {
        val current = movieDao.isInWatchlist(movieId) ?: false
        movieDao.setWatchlist(movieId, !current)
    }

    override suspend fun getFavoriteMovies(): List<Movie> = withContext(Dispatchers.IO) {
        movieDao.getFavoriteMoviesSync().map { it.toDomain() }
    }

    override suspend fun getWatchlistMovies(): List<Movie> = withContext(Dispatchers.IO) {
        movieDao.getWatchlistMoviesSync().map { it.toDomain() }
    }
}
