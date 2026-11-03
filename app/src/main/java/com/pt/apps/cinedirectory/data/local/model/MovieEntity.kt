package com.pt.apps.cinedirectory.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pt.apps.cinedirectory.data.remote.dto.MovieDto
import com.pt.apps.cinedirectory.domain.model.Movie

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val rank: Int,
    val title: String,
    val originalTitle: String,
    val year: Int,
    val rating: Double,
    val voteCount: String,
    val duration: String,
    val genresCsv: String,
    val director: String,
    val castCsv: String,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String = "",
    val page: Int = 1,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Movie {
        return Movie(
            id = id,
            rank = rank,
            title = title,
            originalTitle = originalTitle,
            year = year,
            rating = rating,
            voteCount = voteCount,
            duration = duration,
            genres = genresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            director = director,
            cast = castCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            overview = overview,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            trailerUrl = trailerUrl,
            isFavorite = isFavorite,
            isInWatchlist = isInWatchlist
        )
    }

    companion object {
        fun fromDto(
            dto: MovieDto,
            page: Int,
            isFavorite: Boolean = false,
            isInWatchlist: Boolean = false
        ): MovieEntity {
            return MovieEntity(
                id = dto.id,
                rank = dto.rank,
                title = dto.title,
                originalTitle = dto.originalTitle ?: dto.title,
                year = dto.year,
                rating = dto.rating,
                voteCount = dto.voteCount,
                duration = dto.duration,
                genresCsv = dto.genres.joinToString(","),
                director = dto.director,
                castCsv = dto.cast.joinToString(","),
                overview = dto.overview,
                posterUrl = dto.posterUrl,
                backdropUrl = dto.backdropUrl,
                trailerUrl = dto.trailerUrl ?: "",
                page = page,
                isFavorite = isFavorite,
                isInWatchlist = isInWatchlist,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}

