package com.valentinosantoso.movie.data.model

import java.util.Locale

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int> = emptyList()
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val fullBackdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val formattedRating: String
        get() = String.format(Locale.US, "%.1f", voteAverage)

    val releaseYear: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else releaseDate
}

data class Genre(
    val id: Int,
    val name: String
)

data class VideoTrailer(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val isOfficial: Boolean
) {
    val youtubeUrl: String
        get() = "https://www.youtube.com/watch?v=$key"

    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}

data class CrewDirector(
    val id: Int,
    val name: String,
    val profilePath: String?
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
    val order: Int = 0
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

data class CreditsData(
    val cast: List<Cast>,
    val director: CrewDirector?
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val runtime: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val status: String,
    val originalLanguage: String = "en",
    val genres: List<Genre>
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val fullBackdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val formattedRating: String
        get() = String.format(Locale.US, "%.1f", voteAverage)

    val formattedRuntime: String
        get() {
            if (runtime <= 0) return "-"
            val hours = runtime / 60
            val minutes = runtime % 60
            return if (hours > 0) "${hours}j ${minutes}m" else "${minutes}m"
        }

    val releaseYear: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else releaseDate

    val ageRating: String
        get() = when {
            genres.any { it.name.contains("Animation", ignoreCase = true) || it.name.contains("Family", ignoreCase = true) } -> "SU"
            genres.any { it.name.contains("Horror", ignoreCase = true) || it.name.contains("Crime", ignoreCase = true) } -> "17+"
            else -> "13+"
        }

    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            voteCount = voteCount,
            genreIds = genres.map { it.id }
        )
    }
}
