package com.valentinosantoso.movie.data.local

import android.content.Context
import android.content.SharedPreferences
import com.valentinosantoso.movie.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class FavoriteManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("movie_favorites_prefs", Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<List<Movie>>(emptyList())
    val favoritesFlow: StateFlow<List<Movie>> = _favoritesFlow.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val jsonString = prefs.getString("favorite_movies", "[]") ?: "[]"
        val list = mutableListOf<Movie>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val genreList = mutableListOf<Int>()
                val genreArray = item.optJSONArray("genreIds")
                if (genreArray != null) {
                    for (j in 0 until genreArray.length()) {
                        genreList.add(genreArray.getInt(j))
                    }
                }

                list.add(
                    Movie(
                        id = item.getInt("id"),
                        title = item.getString("title"),
                        overview = item.optString("overview", ""),
                        posterPath = item.optString("posterPath").takeIf { it.isNotEmpty() && it != "null" },
                        backdropPath = item.optString("backdropPath").takeIf { it.isNotEmpty() && it != "null" },
                        releaseDate = item.optString("releaseDate", ""),
                        voteAverage = item.optDouble("voteAverage", 0.0),
                        voteCount = item.optInt("voteCount", 0),
                        genreIds = genreList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _favoritesFlow.value = list
    }

    fun isFavorite(movieId: Int): Boolean {
        return _favoritesFlow.value.any { it.id == movieId }
    }

    fun toggleFavorite(movie: Movie): Boolean {
        val currentList = _favoritesFlow.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == movie.id }
        val isNowFavorite: Boolean

        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
            isNowFavorite = false
        } else {
            currentList.add(0, movie)
            isNowFavorite = true
        }

        saveFavorites(currentList)
        return isNowFavorite
    }

    fun removeFavorite(movieId: Int) {
        val currentList = _favoritesFlow.value.toMutableList()
        currentList.removeAll { it.id == movieId }
        saveFavorites(currentList)
    }

    private fun saveFavorites(list: List<Movie>) {
        try {
            val jsonArray = JSONArray()
            for (movie in list) {
                val obj = JSONObject().apply {
                    put("id", movie.id)
                    put("title", movie.title)
                    put("overview", movie.overview)
                    put("posterPath", movie.posterPath ?: "")
                    put("backdropPath", movie.backdropPath ?: "")
                    put("releaseDate", movie.releaseDate)
                    put("voteAverage", movie.voteAverage)
                    put("voteCount", movie.voteCount)
                    val genreArray = JSONArray()
                    movie.genreIds.forEach { genreArray.put(it) }
                    put("genreIds", genreArray)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("favorite_movies", jsonArray.toString()).apply()
            _favoritesFlow.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
