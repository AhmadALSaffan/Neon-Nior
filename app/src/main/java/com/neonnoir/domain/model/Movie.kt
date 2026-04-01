package com.neonnoir.domain.model

data class Movie(
    val imdbId:     String,
    val title:      String,
    val year:       String,
    val rated:      String,
    val runtime:    String,         // Already formatted: "2h 22m"
    val genres:     List<String>,   // Split from "Sci-Fi, Action"
    val director:   String,
    val actors:     List<String>,   // Split from "Keanu Reeves, ..."
    val plot:       String,
    val poster:     String,         // High-res URL
    val imdbRating: Float,          // Parsed from "8.7"
    val type:       String
)