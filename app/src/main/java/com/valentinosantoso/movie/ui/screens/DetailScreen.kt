package com.valentinosantoso.movie.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinosantoso.movie.data.model.Cast
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.data.model.MovieDetail
import com.valentinosantoso.movie.data.remote.TmdbApiClient
import com.valentinosantoso.movie.ui.components.MovieCard
import com.valentinosantoso.movie.ui.theme.DarkBackground
import com.valentinosantoso.movie.ui.theme.DarkSurface
import com.valentinosantoso.movie.ui.theme.DarkSurfaceVariant
import com.valentinosantoso.movie.ui.theme.PrimaryRed
import com.valentinosantoso.movie.ui.theme.StarGold
import com.valentinosantoso.movie.ui.theme.TextMuted
import com.valentinosantoso.movie.ui.theme.TextPrimary
import com.valentinosantoso.movie.ui.theme.TextSecondary
import com.valentinosantoso.movie.util.NetworkImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    movieId: Int,
    isFavorite: (Int) -> Boolean,
    onFavoriteToggle: (Movie) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<MovieDetail?>(null) }
    var castList by remember { mutableStateOf<List<Cast>>(emptyList()) }
    var similarMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loadDetail = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val detailRes = TmdbApiClient.getMovieDetails(movieId)
                val creditsRes = TmdbApiClient.getMovieCredits(movieId)
                val similarRes = TmdbApiClient.getSimilarMovies(movieId)

                if (detailRes.isSuccess) {
                    detail = detailRes.getOrNull()
                } else {
                    errorMessage = detailRes.exceptionOrNull()?.localizedMessage ?: "Gagal memuat detail film"
                }

                if (creditsRes.isSuccess) castList = creditsRes.getOrDefault(emptyList())
                if (similarRes.isSuccess) similarMovies = similarRes.getOrDefault(emptyList())
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(movieId) {
        loadDetail()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = PrimaryRed, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Memuat detail...", color = TextSecondary, fontSize = 14.sp)
            }
        } else if (errorMessage != null && detail == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Gagal Memuat Detail Film",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage ?: "Terjadi kesalahan", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { loadDetail() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Coba Lagi")
                }
            }
        } else if (detail != null) {
            val movieDetail = detail!!
            val isFav = isFavorite(movieDetail.id)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Backdrop Header with Gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        NetworkImage(
                            url = movieDetail.fullBackdropUrl ?: movieDetail.fullPosterUrl,
                            contentDescription = movieDetail.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top Gradient for Top Bar visibility
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0x99000000), Color.Transparent)
                                    )
                                )
                        )

                        // Bottom Gradient blending into DarkBackground
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            DarkBackground.copy(alpha = 0.4f),
                                            DarkBackground
                                        ),
                                        startY = 120f
                                    )
                                )
                        )
                    }
                }

                // Title, Poster, Metadata
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = (-60).dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Poster Thumbnail (Overlapping)
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(165.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            NetworkImage(
                                url = movieDetail.fullPosterUrl,
                                contentDescription = movieDetail.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title & Quick Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movieDetail.title,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )

                            if (movieDetail.tagline.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${movieDetail.tagline}\"",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rating and Runtime
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = movieDetail.formattedRating,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " (${movieDetail.voteCount})",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(text = "•", color = TextMuted)
                                Text(
                                    text = movieDetail.formattedRuntime,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(text = "•", color = TextMuted)
                                Text(
                                    text = movieDetail.releaseYear,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Favorite and Share
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onFavoriteToggle(movieDetail.toMovie()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFav) PrimaryRed else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (isFav) Color.White else PrimaryRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFav) "Di Favorit" else "Tambah Favorit",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Tonton film \"${movieDetail.title}\" (${movieDetail.releaseYear})! Rating: ⭐ ${movieDetail.formattedRating}/10.\nInfo lebih lanjut: https://www.themoviedb.org/movie/${movieDetail.id}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Film"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Bagikan", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }

                // Genre Badges
                if (movieDetail.genres.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            movieDetail.genres.forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(DarkSurfaceVariant, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = genre.name,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Overview / Synopsis
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Sinopsis",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (movieDetail.overview.isNotBlank()) movieDetail.overview else "Tidak ada ringkasan sinopsis untuk film ini.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Cast / Actors List
                if (castList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column {
                            Text(
                                text = "Pemeran Utama",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(castList) { cast ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(DarkSurfaceVariant)
                                        ) {
                                            NetworkImage(
                                                url = cast.fullProfileUrl,
                                                contentDescription = cast.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = cast.name,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = cast.character,
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Similar Movies
                if (similarMovies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column {
                            Text(
                                text = "Film Serupa",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(similarMovies) { simMovie ->
                                    MovieCard(
                                        movie = simMovie,
                                        isFavorite = isFavorite(simMovie.id),
                                        onMovieClick = onMovieClick,
                                        onFavoriteToggle = onFavoriteToggle,
                                        cardWidth = 130
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Floating Back & Favorite Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            if (detail != null) {
                val isFav = isFavorite(detail!!.id)
                IconButton(
                    onClick = { onFavoriteToggle(detail!!.toMovie()) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorit",
                        tint = if (isFav) PrimaryRed else Color.White
                    )
                }
            }
        }
    }
}
