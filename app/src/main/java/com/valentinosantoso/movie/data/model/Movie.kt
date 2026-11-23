package com.valentinosantoso.movie.data.model

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
        get() = String.format("%.1f", voteAverage)

    val releaseYear: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else releaseDate
}

data class Genre(
    val id: Int,
    val name: String
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
    val genres: List<Genre>
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val fullBackdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val formattedRating: String
        get() = String.format("%.1f", voteAverage)

    val formattedRuntime: String
        get() {
            if (runtime <= 0) return "-"
            val hours = runtime / 60
            val minutes = runtime % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val releaseYear: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else releaseDate

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

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}
