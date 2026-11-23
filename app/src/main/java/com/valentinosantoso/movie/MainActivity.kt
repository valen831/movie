package com.valentinosantoso.movie

import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.valentinosantoso.movie.data.local.FavoriteManager
import com.valentinosantoso.movie.data.local.LanguageManager
import com.valentinosantoso.movie.data.local.ThemeManager
import com.valentinosantoso.movie.data.local.ThemeMode
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.data.remote.TmdbApiClient
import com.valentinosantoso.movie.ui.components.AppTab
import com.valentinosantoso.movie.ui.components.MovieBottomBar
import com.valentinosantoso.movie.ui.screens.DetailScreen
import com.valentinosantoso.movie.ui.screens.FavoritesScreen
import com.valentinosantoso.movie.ui.screens.HomeScreen
import com.valentinosantoso.movie.ui.screens.SearchScreen
import com.valentinosantoso.movie.ui.theme.MovieTheme

class MainActivity : ComponentActivity() {

    private lateinit var favoriteManager: FavoriteManager
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        favoriteManager = FavoriteManager(applicationContext)
        themeManager = ThemeManager(applicationContext)
        languageManager = LanguageManager(applicationContext)

        setContent {
            val themeMode by themeManager.themeModeFlow.collectAsState()
            val currentLang by languageManager.languageFlow.collectAsState()

            // Update TMDB API language
            TmdbApiClient.currentLanguage = languageManager.tmdbLanguage

            val baseConfig = LocalConfiguration.current
            val localizedConfig = remember(currentLang, baseConfig.orientation) {
                Configuration(baseConfig).apply {
                    setLocale(languageManager.currentLocale)
                }
            }

            CompositionLocalProvider(LocalConfiguration provides localizedConfig) {
                MovieTheme(themeMode = themeMode) {
                    MovieApp(
                        favoriteManager = favoriteManager,
                        themeManager = themeManager,
                        languageManager = languageManager,
                        themeMode = themeMode,
                        currentLang = currentLang
                    )
                }
            }
        }
    }
}

@Composable
fun MovieApp(
    favoriteManager: FavoriteManager,
    themeManager: ThemeManager,
    languageManager: LanguageManager,
    themeMode: ThemeMode,
    currentLang: String
) {
    val context = LocalContext.current
    val favorites by favoriteManager.favoritesFlow.collectAsState()

    var currentTabName by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    val currentTab = try {
        AppTab.valueOf(currentTabName)
    } catch (e: Exception) {
        AppTab.HOME
    }

    var selectedMovieId by rememberSaveable { mutableStateOf<Int?>(null) }

    val onFavoriteToggle: (Movie) -> Unit = { movie ->
        val isNowFav = favoriteManager.toggleFavorite(movie)
        Toast.makeText(
            context,
            if (isNowFav) context.getString(R.string.toast_added_fav) else context.getString(R.string.toast_removed_fav),
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
            currentTabName = AppTab.HOME.name
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedMovieId == null) {
                MovieBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTabName = tab.name
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
                                    isDarkTheme = themeMode == ThemeMode.DARK,
                                    currentLanguage = currentLang,
                                    onToggleTheme = { themeManager.toggleTheme() },
                                    onToggleLanguage = { languageManager.toggleLanguage() },
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
                                        currentTabName = AppTab.HOME.name
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
