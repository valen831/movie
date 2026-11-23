package com.valentinosantoso.movie.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinosantoso.movie.R
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.data.remote.TmdbApiClient
import com.valentinosantoso.movie.ui.components.MovieBackdropCard
import com.valentinosantoso.movie.ui.components.MovieRecyclerView
import com.valentinosantoso.movie.ui.theme.PrimaryRed
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    isFavorite: (Int) -> Boolean,
    isDarkTheme: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onFavoriteToggle: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var trendingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var nowPlayingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var topRatedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var upcomingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val loadData = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val tmdbLang = if (currentLanguage == "en") "en-US" else "id-ID"
                TmdbApiClient.currentLanguage = tmdbLang

                val trendingRes = TmdbApiClient.getTrendingMovies(language = tmdbLang)
                val nowPlayingRes = TmdbApiClient.getNowPlayingMovies(language = tmdbLang)
                val popularRes = TmdbApiClient.getPopularMovies(language = tmdbLang)
                val topRatedRes = TmdbApiClient.getTopRatedMovies(language = tmdbLang)
                val upcomingRes = TmdbApiClient.getUpcomingMovies(language = tmdbLang)

                if (trendingRes.isSuccess) trendingMovies = trendingRes.getOrDefault(emptyList())
                if (nowPlayingRes.isSuccess) nowPlayingMovies = nowPlayingRes.getOrDefault(emptyList())
                if (popularRes.isSuccess) popularMovies = popularRes.getOrDefault(emptyList())
                if (topRatedRes.isSuccess) topRatedMovies = topRatedRes.getOrDefault(emptyList())
                if (upcomingRes.isSuccess) upcomingMovies = upcomingRes.getOrDefault(emptyList())

                if (trendingMovies.isEmpty() && popularMovies.isEmpty()) {
                    errorMessage = "Koneksi ke TMDB gagal atau data kosong."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Terjadi kesalahan"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentLanguage) {
        loadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else if (errorMessage != null && trendingMovies.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_results_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "Periksa koneksi internet Anda",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { loadData() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.retry_button))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // App Header Bar with Multi Language & Theme Toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CINEMA_",
                                    color = PrimaryRed,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "FLIX",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        // Right side controls: Language switcher + Theme switcher
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Multi-Language Toggle (🇮🇩 ID / 🇬🇧 EN)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onToggleLanguage() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (currentLanguage == "en") "🇬🇧 EN" else "🇮🇩 ID",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Dark / Light Theme Toggle (☀️ Terang / 🌙 Gelap)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onToggleTheme() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isDarkTheme) stringResource(R.string.theme_light) else stringResource(R.string.theme_dark),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Hero Featured Movie (First Trending)
                if (trendingMovies.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            MovieBackdropCard(
                                movie = trendingMovies.first(),
                                onMovieClick = onMovieClick,
                                modifier = if (isLandscape) Modifier.height(200.dp) else Modifier
                            )
                        }
                    }
                }

                // Section 1: Sedang Tayang di Bioskop (Recycler View Card)
                if (nowPlayingMovies.isNotEmpty()) {
                    item {
                        MovieSectionHeader(title = stringResource(R.string.now_playing))
                        MovieRecyclerView(
                            movies = nowPlayingMovies,
                            isFavorite = isFavorite,
                            onMovieClick = onMovieClick,
                            onFavoriteToggle = onFavoriteToggle,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                // Section 2: Film Populer (Recycler View Card)
                if (popularMovies.isNotEmpty()) {
                    item {
                        MovieSectionHeader(title = stringResource(R.string.popular_movies))
                        MovieRecyclerView(
                            movies = popularMovies,
                            isFavorite = isFavorite,
                            onMovieClick = onMovieClick,
                            onFavoriteToggle = onFavoriteToggle,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                // Section 3: Rating Tertinggi (Recycler View Card)
                if (topRatedMovies.isNotEmpty()) {
                    item {
                        MovieSectionHeader(title = stringResource(R.string.top_rated))
                        MovieRecyclerView(
                            movies = topRatedMovies,
                            isFavorite = isFavorite,
                            onMovieClick = onMovieClick,
                            onFavoriteToggle = onFavoriteToggle,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                // Section 4: Segera Rilis (Recycler View Card)
                if (upcomingMovies.isNotEmpty()) {
                    item {
                        MovieSectionHeader(title = stringResource(R.string.upcoming_movies))
                        MovieRecyclerView(
                            movies = upcomingMovies,
                            isFavorite = isFavorite,
                            onMovieClick = onMovieClick,
                            onFavoriteToggle = onFavoriteToggle,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieSectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(PrimaryRed, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
