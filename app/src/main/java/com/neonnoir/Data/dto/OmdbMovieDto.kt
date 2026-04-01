package com.neonnoir.Data.remote.dto

import com.google.gson.annotations.SerializedName

data class OmdbMovieDto(
    @SerializedName("Title")      val title: String,
    @SerializedName("Year")       val year: String,
    @SerializedName("Rated")      val rated: String,
    @SerializedName("Released")   val released: String,
    @SerializedName("Runtime")    val runtime: String,
    @SerializedName("Genre")      val genre: String,
    @SerializedName("Director")   val director: String,
    @SerializedName("Actors")     val actors: String,
    @SerializedName("Plot")       val plot: String,
    @SerializedName("Poster")     val poster: String,
    @SerializedName("imdbRating") val imdbRating: String,
    @SerializedName("imdbID")     val imdbId: String,
    @SerializedName("Type")       val type: String,
    @SerializedName("Response")   val response: String,
    @SerializedName("Error")      val error: String?
)