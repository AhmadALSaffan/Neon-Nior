package com.neonnoir.domain.model

data class SearchResult(
    val imdbId: String,
    val title:  String,
    val year:   String,
    val type:   String,
    val poster: String
)