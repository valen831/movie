package com.valentinosantoso.movie

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.valentinosantoso.movie.data.local.FavoriteManager
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.ui.components.AppTab
import com.valentinosantoso.movie.ui.components.MovieBottomBar
import com.valentinosantoso.movie.ui.screens.DetailScreen
import com.valentinosantoso.movie.ui.screens.FavoritesScreen
import com.valentinosantoso.movie.ui.screens.HomeScreen
import com.valentinosantoso.movie.ui.screens.SearchScreen
import com.valentinosantoso.movie.ui.theme.DarkBackground
import com.valentinosantoso.movie.ui.theme.MovieTheme

class MainActivity : ComponentActivity() {

    private lateinit var favoriteManager: FavoriteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        favoriteManager = FavoriteManager(applicationContext)

        setContent {
            MovieTheme {
                MovieApp(favoriteManager = favoriteManager)
            }
        }
    }
}

@Composable
fun MovieApp(favoriteManager: FavoriteManager) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val favorites by favoriteManager.favoritesFlow.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var selectedMovieId by remember { mutableStateOf<Int?>(null) }

    val onFavoriteToggle: (Movie) -> Unit = { movie ->
        val isNowFav = favoriteManager.toggleFavorite(movie)
        Toast.makeText(
            context,
            if (isNowFav) "Ditambahkan ke Favorit ❤️" else "Dihapus dari Favorit",
            Toast.LENGTH_SHORT
        ).show()
    }

    val isFavorite: (Int) -> Boolean = { id ->
        favorites.any { it.id == id }
    }

    // Hardware back button behavior
    if (selectedMovieId != null) {
        BackHandler {
            selectedMovieId = null
        }
    } else if (currentTab != AppTab.HOME) {
        BackHandler {
            currentTab = AppTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        bottomBar = {
            if (selectedMovieId == null) {
                MovieBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedMovieId,
                label = "navigation_crossfade"
            ) { movieId ->
                if (movieId != null) {
                    DetailScreen(
                        movieId = movieId,
                        isFavorite = isFavorite,
                        onFavoriteToggle = onFavoriteToggle,
                        onMovieClick = { nextMovie ->
                            selectedMovieId = nextMovie.id
                        },
                        onBackClick = {
                            selectedMovieId = null
                        }
                    )
                } else {
                    Crossfade(
                        targetState = currentTab,
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            AppTab.HOME -> {
                                HomeScreen(
                                    isFavorite = isFavorite,
                                    onMovieClick = { movie ->
                                        selectedMovieId = movie.id
                                    },
                                    onFavoriteToggle = onFavoriteToggle
                                )
                            }

                            AppTab.SEARCH -> {
                                SearchScreen(
                                    isFavorite = isFavorite,
                                    onMovieClick = { movie ->
                                        selectedMovieId = movie.id
                                    },
                                    onFavoriteToggle = onFavoriteToggle
                                )
                            }

                            AppTab.FAVORITES -> {
                                FavoritesScreen(
                                    favorites = favorites,
                                    onMovieClick = { movie ->
                                        selectedMovieId = movie.id
                                    },
                                    onFavoriteToggle = onFavoriteToggle,
                                    onExploreClick = {
                                        currentTab = AppTab.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
