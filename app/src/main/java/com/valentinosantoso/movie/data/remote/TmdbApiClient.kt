package com.valentinosantoso.movie.data.remote

import com.valentinosantoso.movie.data.model.Cast
import com.valentinosantoso.movie.data.model.CreditsData
import com.valentinosantoso.movie.data.model.CrewDirector
import com.valentinosantoso.movie.data.model.Genre
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.data.model.MovieDetail
import com.valentinosantoso.movie.data.model.VideoTrailer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TmdbApiClient {
    // API Credentials from TMDB Account
    const val API_KEY = "0e3ae1697e705a18185e58411565578e"
    private const val BASE_URL = "https://api.themoviedb.org/3"

    var currentLanguage: String = "id-ID"

    suspend fun getTrendingMovies(page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/trending/movie/day?api_key=$API_KEY&language=$language&page=$page")
    }

    suspend fun getNowPlayingMovies(page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/movie/now_playing?api_key=$API_KEY&language=$language&page=$page")
    }

    suspend fun getPopularMovies(page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/movie/popular?api_key=$API_KEY&language=$language&page=$page")
    }

    suspend fun getTopRatedMovies(page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/movie/top_rated?api_key=$API_KEY&language=$language&page=$page")
    }

    suspend fun getUpcomingMovies(page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/movie/upcoming?api_key=$API_KEY&language=$language&page=$page")
    }

    suspend fun searchMovies(query: String, page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        fetchMovieList("$BASE_URL/search/movie?api_key=$API_KEY&language=$language&query=$encodedQuery&page=$page")
    }

    suspend fun getMoviesByGenre(genreId: Int, page: Int = 1, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/discover/movie?api_key=$API_KEY&language=$language&with_genres=$genreId&page=$page")
    }

    suspend fun getSimilarMovies(movieId: Int, language: String = currentLanguage): Result<List<Movie>> = withContext(Dispatchers.IO) {
        fetchMovieList("$BASE_URL/movie/$movieId/similar?api_key=$API_KEY&language=$language")
    }

    suspend fun getMovieDetails(movieId: Int, language: String = currentLanguage): Result<MovieDetail> = withContext(Dispatchers.IO) {
        try {
            val responseString = executeGetRequest("$BASE_URL/movie/$movieId?api_key=$API_KEY&language=$language")
            val json = JSONObject(responseString)

            val genresList = mutableListOf<Genre>()
            val genresJson = json.optJSONArray("genres")
            if (genresJson != null) {
                for (i in 0 until genresJson.length()) {
                    val gObj = genresJson.getJSONObject(i)
                    genresList.add(Genre(id = gObj.optInt("id"), name = gObj.optString("name")))
                }
            }

            var overview = json.optString("overview")
            var tagline = json.optString("tagline")

            val isIndonesian = language.startsWith("id", ignoreCase = true)

            // Smart Fallback & Translation for Indonesian:
            // 1. If synopsis is empty on TMDB in Indonesian, fetch English and TRANSLATE to Indonesian!
            if (overview.isBlank()) {
                try {
                    val enResponseString = executeGetRequest("$BASE_URL/movie/$movieId?api_key=$API_KEY&language=en-US")
                    val enJson = JSONObject(enResponseString)
                    val enOverview = enJson.optString("overview")
                    if (enOverview.isNotBlank()) {
                        overview = if (isIndonesian) translateToIndonesian(enOverview) else enOverview
                    }
                    if (tagline.isBlank()) {
                        val enTagline = enJson.optString("tagline")
                        if (enTagline.isNotBlank()) {
                            tagline = if (isIndonesian) translateToIndonesian(enTagline) else enTagline
                        }
                    }
                } catch (_: Exception) {
                    // Fallback failure is non-fatal
                }
            } else if (isIndonesian && isLikelyEnglish(overview)) {
                // 2. If synopsis from TMDB is in English despite Indonesian mode, translate to Indonesian!
                overview = translateToIndonesian(overview)
                if (tagline.isNotBlank() && isLikelyEnglish(tagline)) {
                    tagline = translateToIndonesian(tagline)
                }
            }

            val detail = MovieDetail(
                id = json.optInt("id"),
                title = json.optString("title"),
                tagline = tagline,
                overview = overview,
                posterPath = json.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" },
                backdropPath = json.optString("backdrop_path").takeIf { it.isNotEmpty() && it != "null" },
                releaseDate = json.optString("release_date"),
                runtime = json.optInt("runtime", 0),
                voteAverage = json.optDouble("vote_average", 0.0),
                voteCount = json.optInt("vote_count", 0),
                status = json.optString("status"),
                originalLanguage = json.optString("original_language", "en"),
                genres = genresList
            )
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieVideos(movieId: Int): Result<List<VideoTrailer>> = withContext(Dispatchers.IO) {
        try {
            val responseString = executeGetRequest("$BASE_URL/movie/$movieId/videos?api_key=$API_KEY")
            val json = JSONObject(responseString)
            val results = json.optJSONArray("results") ?: JSONArray()
            val list = mutableListOf<VideoTrailer>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val site = item.optString("site")
                val key = item.optString("key")
                if (site.equals("YouTube", ignoreCase = true) && key.isNotBlank()) {
                    val isOfficial = item.optBoolean("official", false)
                    val type = item.optString("type")
                    list.add(
                        VideoTrailer(
                            id = item.optString("id"),
                            key = key,
                            name = item.optString("name"),
                            site = site,
                            type = type,
                            isOfficial = isOfficial
                        )
                    )
                }
            }

            // Sort: Official Trailers first, then Trailers, then other videos
            list.sortWith(
                compareByDescending<VideoTrailer> { it.type.equals("Trailer", ignoreCase = true) }
                    .thenByDescending { it.isOfficial }
            )

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieCredits(movieId: Int): Result<CreditsData> = withContext(Dispatchers.IO) {
        try {
            val responseString = executeGetRequest("$BASE_URL/movie/$movieId/credits?api_key=$API_KEY")
            val json = JSONObject(responseString)

            // Extract Cast
            val castArray = json.optJSONArray("cast") ?: JSONArray()
            val castList = mutableListOf<Cast>()
            for (i in 0 until minOf(castArray.length(), 25)) {
                val castObj = castArray.getJSONObject(i)
                castList.add(
                    Cast(
                        id = castObj.optInt("id"),
                        name = castObj.optString("name"),
                        character = castObj.optString("character"),
                        profilePath = castObj.optString("profile_path").takeIf { it.isNotEmpty() && it != "null" },
                        order = castObj.optInt("order", i)
                    )
                )
            }

            // Extract Director from Crew
            val crewArray = json.optJSONArray("crew") ?: JSONArray()
            var director: CrewDirector? = null
            for (i in 0 until crewArray.length()) {
                val crewObj = crewArray.getJSONObject(i)
                val job = crewObj.optString("job")
                if (job.equals("Director", ignoreCase = true)) {
                    director = CrewDirector(
                        id = crewObj.optInt("id"),
                        name = crewObj.optString("name"),
                        profilePath = crewObj.optString("profile_path").takeIf { it.isNotEmpty() && it != "null" }
                    )
                    break
                }
            }

            Result.success(CreditsData(cast = castList, director = director))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchMovieList(urlString: String): Result<List<Movie>> {
        return try {
            val responseString = executeGetRequest(urlString)
            val json = JSONObject(responseString)
            val results = json.optJSONArray("results") ?: JSONArray()
            val movieList = mutableListOf<Movie>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val genreIdsList = mutableListOf<Int>()
                val genreArray = item.optJSONArray("genre_ids")
                if (genreArray != null) {
                    for (j in 0 until genreArray.length()) {
                        genreIdsList.add(genreArray.getInt(j))
                    }
                }

                movieList.add(
                    Movie(
                        id = item.optInt("id"),
                        title = item.optString("title"),
                        overview = item.optString("overview"),
                        posterPath = item.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" },
                        backdropPath = item.optString("backdrop_path").takeIf { it.isNotEmpty() && it != "null" },
                        releaseDate = item.optString("release_date"),
                        voteAverage = item.optDouble("vote_average", 0.0),
                        voteCount = item.optInt("vote_count", 0),
                        genreIds = genreIdsList
                    )
                )
            }
            Result.success(movieList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeGetRequest(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12000
            connection.readTimeout = 12000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    return reader.readText()
                }
            } else {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                throw Exception("API Error ($responseCode): $errorMessage")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun isLikelyEnglish(text: String): Boolean {
        val lower = " " + text.lowercase() + " "
        val enHits = listOf(" the ", " and ", " with ", " for ", " from ", " their ", " about ", " after ", " before ", " who ", " into ").count { lower.contains(it) }
        val idHits = listOf(" yang ", " dan ", " di ", " dengan ", " dari ", " untuk ", " sebuah ", " mereka ", " saat ", " telah ").count { lower.contains(it) }
        return enHits >= 2 && idHits == 0
    }

    private fun translateToIndonesian(text: String): String {
        if (text.isBlank()) return text
        return try {
            val encodedQuery = URLEncoder.encode(text, "UTF-8")
            val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=id&dt=t&q=$encodedQuery")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")

            if (connection.responseCode in 200..299) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseString)
                val segments = jsonArray.optJSONArray(0)
                if (segments != null) {
                    val sb = StringBuilder()
                    for (i in 0 until segments.length()) {
                        val seg = segments.optJSONArray(i)
                        if (seg != null && seg.length() > 0) {
                            sb.append(seg.optString(0))
                        }
                    }
                    val result = sb.toString().trim()
                    if (result.isNotEmpty()) result else text
                } else {
                    text
                }
            } else {
                text
            }
        } catch (_: Exception) {
            text
        }
    }
}
