package com.pt.apps.cinedirectory.data.remote.api

import com.pt.apps.cinedirectory.data.remote.dto.MovieDto
import com.pt.apps.cinedirectory.data.remote.dto.MoviePageResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("api/v1/movies/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): MoviePageResponseDto

    @GET("api/v1/movies/{id}")
    suspend fun getMovieDetail(
        @Path("id") id: String
    ): MovieDto
}