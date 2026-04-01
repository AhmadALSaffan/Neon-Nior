package com.neonnoir.Data.mapper

import com.neonnoir.Data.remote.dto.OmdbMovieDto
import com.neonnoir.Data.remote.dto.OmdbSearchItemDto
import com.neonnoir.domain.model.Movie
import com.neonnoir.domain.model.SearchResult

object MovieMapper {

    // Converts an OmdbMovieDto to a Movie domain model
    fun OmdbMovieDto.toDomain(): Movie = Movie(
        imdbId     = imdbId,
        title      = title,
        year       = year,
        rated      = rated,
        runtime    = formatRuntime(runtime),
        genres     = genre.split(", ").filter { it.isNotBlank() },
        director   = director,
        actors     = actors.split(", ").filter { it.isNotBlank() },
        plot       = plot,
        poster     = upgradePosterResolution(poster),
        imdbRating = imdbRating.toFloatOrNull() ?: 0f,
        type       = type
    )

    // Converts an OmdbSearchItemDto to a SearchResult domain model
    fun OmdbSearchItemDto.toDomain(): SearchResult = SearchResult(
        imdbId = imdbId,
        title  = title,
        year   = year,
        type   = type,
        poster = upgradePosterResolution(poster)
    )

    // Converts "142 min" to "2h 22m" format
    private fun formatRuntime(raw: String): String {
        val mins = raw.replace(" min", "").trim().toIntOrNull() ?: return raw
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    // Upgrades OMDB poster URL from 300px to 600px resolution
    private fun upgradePosterResolution(url: String): String {
        if (url == "N/A") return ""
        return url.replace("SX300", "SX600")
    }
}