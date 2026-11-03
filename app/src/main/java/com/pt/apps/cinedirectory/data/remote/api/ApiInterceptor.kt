package com.pt.apps.cinedirectory.data.remote.api


import com.pt.apps.cinedirectory.data.remote.MovieDataCatalog
import com.pt.apps.cinedirectory.data.remote.dto.MovieDto
import com.pt.apps.cinedirectory.data.remote.dto.MoviePageResponseDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.HttpURLConnection

class ApiInterceptor : Interceptor {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val pageAdapter = moshi.adapter(MoviePageResponseDto::class.java)
    private val movieAdapter = moshi.adapter(MovieDto::class.java)


    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url

        val pathSegments = url.pathSegments
        val isTopRated = pathSegments.contains("top_rated")

        if (isTopRated) {
            val page = url.queryParameter("page")?.toIntOrNull() ?: 1
            val limit = url.queryParameter("limit")?.toIntOrNull() ?: 10

            val allMovies = MovieDataCatalog.topRatedMovies
            val totalResults = allMovies.size
            val totalPages = (totalResults + limit - 1) / limit

            val startIndex = ((page - 1) * limit).coerceAtLeast(0)
            val endIndex = (startIndex + limit).coerceAtMost(totalResults)

            val pagedMovies = if (startIndex < totalResults) {
                allMovies.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            val pageResponse = MoviePageResponseDto(
                page = page,
                totalPages = totalPages,
                totalResults = totalResults,
                results = pagedMovies
            )

            val json = pageAdapter.toJson(pageResponse)
            return Response.Builder()
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .build()
        }

        // Check if single movie detail request
        if (pathSegments.size >= 4 && pathSegments[2] == "movies") {
            val movieId = pathSegments[3]
            val movie = MovieDataCatalog.topRatedMovies.find { it.id == movieId }
            if (movie != null) {
                val json = movieAdapter.toJson(movie)
                return Response.Builder()
                    .code(HttpURLConnection.HTTP_OK)
                    .message("OK")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(json.toResponseBody("application/json".toMediaType()))
                    .addHeader("content-type", "application/json")
                    .build()
            } else {
                return Response.Builder()
                    .code(HttpURLConnection.HTTP_NOT_FOUND)
                    .message("Movie not found")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        return chain.proceed(request)
    }
}

