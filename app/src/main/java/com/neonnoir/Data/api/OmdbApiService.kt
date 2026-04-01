package com.neonnoir.Data.remote.api

import com.neonnoir.Data.remote.dto.OmdbMovieDto
import com.neonnoir.Data.remote.dto.OmdbSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApiService {

    // Searches movies by title — apiKey injected via OkHttp interceptor
    @GET(".")
    suspend fun searchMovies(
        @Query("s")    query: String,
        @Query("page") page: Int,
        @Query("type") type: String?
    ): OmdbSearchResponseDto

    // Fetches full movie details by IMDB ID
    @GET(".")
    suspend fun getMovieById(
        @Query("i")    imdbId: String,
        @Query("plot") plot: String
    ): OmdbMovieDto
}