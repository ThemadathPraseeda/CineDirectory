package com.pt.apps.cinedirectory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pt.apps.cinedirectory.data.local.model.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY rank ASC")
    fun getAllMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' OR director LIKE '%' || :query || '%' ORDER BY rank ASC")
    fun searchMoviesFlow(query: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY rank ASC")
    fun getFavoriteMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isInWatchlist = 1 ORDER BY rank ASC")
    fun getWatchlistMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun getMovieByIdFlow(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: String): MovieEntity?

    @Query("SELECT isFavorite FROM movies WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("SELECT isInWatchlist FROM movies WHERE id = :id")
    suspend fun isInWatchlist(id: String): Boolean?

    @Query("SELECT id FROM movies WHERE isFavorite = 1")
    suspend fun getAllFavoriteIds(): List<String>

    @Query("SELECT id FROM movies WHERE isInWatchlist = 1")
    suspend fun getAllWatchlistIds(): List<String>

    @Query("SELECT MAX(page) FROM movies")
    suspend fun getMaxPage(): Int?

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("UPDATE movies SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("UPDATE movies SET isInWatchlist = :isWatch WHERE id = :id")
    suspend fun setWatchlist(id: String, isWatch: Boolean)

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY rank ASC")
    suspend fun getFavoriteMoviesSync(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE isInWatchlist = 1 ORDER BY rank ASC")
    suspend fun getWatchlistMoviesSync(): List<MovieEntity>
}
