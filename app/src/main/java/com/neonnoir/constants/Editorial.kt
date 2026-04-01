package com.neonnoir.constants

object Editorial {

    // Hero feature shown in the home screen banner
    const val FEATURED_HERO_ID = "tt1856101"  // Blade Runner 2049

    // Trending Now carousel IDs
    val TRENDING_IDS = listOf(
        "tt1856101",  // Blade Runner 2049
        "tt0816692",  // Interstellar
        "tt4154796",  // Avengers: Endgame
        "tt0133093",  // The Matrix
        "tt0468569",  // The Dark Knight
        "tt2015381"   // Guardians of the Galaxy
    )

    // Recently Added carousel IDs
    val RECENTLY_ADDED_IDS = listOf(
        "tt1375666",  // Inception
        "tt0110912",  // Pulp Fiction
        "tt0482571",  // The Prestige
        "tt0120737",  // The Fellowship of the Ring
        "tt0167260"   // Return of the King
    )

    // Genre tile labels mapped to OMDB search keywords
    val GENRE_SEARCHES = mapOf(
        "SCI-FI"   to "sci-fi",
        "HORROR"   to "horror",
        "DRAMA"    to "drama",
        "ACTION"   to "action",
        "THRILLER" to "thriller",
        "NEO-NOIR" to "noir"
    )
}