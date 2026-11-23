package com.valentinosantoso.movie.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.valentinosantoso.movie.data.model.Movie

@Composable
fun MovieRecyclerView(
    movies: List<Movie>,
    isFavorite: (Int) -> Boolean,
    onMovieClick: (Movie) -> Unit,
    onFavoriteToggle: (Movie) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                clipToPadding = false
                val pad = (16 * context.resources.displayMetrics.density).toInt()
                setPadding(pad, 0, pad, 0)
                adapter = MovieRecyclerAdapter(
                    movies = movies,
                    isFavorite = isFavorite,
                    onMovieClick = onMovieClick,
                    onFavoriteToggle = onFavoriteToggle,
                    isDarkTheme = isDarkTheme
                )
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? MovieRecyclerAdapter)?.update(movies, isDarkTheme)
        }
    )
}
