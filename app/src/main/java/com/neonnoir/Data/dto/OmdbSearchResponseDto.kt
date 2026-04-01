package com.neonnoir.Data.remote.dto

import com.google.gson.annotations.SerializedName

data class OmdbSearchResponseDto(
    @SerializedName("Search")       val search: List<OmdbSearchItemDto>?,
    @SerializedName("totalResults") val totalResults: String?,
    @SerializedName("Response")     val response: String,
    @SerializedName("Error")        val error: String?
)

data class OmdbSearchItemDto(
    @SerializedName("Title")  val title: String,
    @SerializedName("Year")   val year: String,
    @SerializedName("imdbID") val imdbId: String,
    @SerializedName("Type")   val type: String,
    @SerializedName("Poster") val poster: String
)