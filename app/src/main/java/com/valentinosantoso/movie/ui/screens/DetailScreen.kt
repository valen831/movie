package com.valentinosantoso.movie.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinosantoso.movie.data.model.Cast
import com.valentinosantoso.movie.data.model.CrewDirector
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.data.model.MovieDetail
import com.valentinosantoso.movie.data.model.VideoTrailer
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
    var director by remember { mutableStateOf<CrewDirector?>(null) }
    var trailers by remember { mutableStateOf<List<VideoTrailer>>(emptyList()) }
    var similarMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val openTrailer: (VideoTrailer) -> Unit = { trailer ->
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${trailer.key}"))
            context.startActivity(appIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trailer.youtubeUrl))
            context.startActivity(webIntent)
        }
    }

    val loadDetail = {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val detailRes = TmdbApiClient.getMovieDetails(movieId)
                val creditsRes = TmdbApiClient.getMovieCredits(movieId)
                val videosRes = TmdbApiClient.getMovieVideos(movieId)
                val similarRes = TmdbApiClient.getSimilarMovies(movieId)

                if (detailRes.isSuccess) {
                    detail = detailRes.getOrNull()
                } else {
                    errorMessage = detailRes.exceptionOrNull()?.localizedMessage ?: "Gagal memuat detail film"
                }

                if (creditsRes.isSuccess) {
                    val credits = creditsRes.getOrNull()
                    castList = credits?.cast ?: emptyList()
                    director = credits?.director
                }

                if (videosRes.isSuccess) {
                    trailers = videosRes.getOrDefault(emptyList())
                }

                if (similarRes.isSuccess) {
                    similarMovies = similarRes.getOrDefault(emptyList())
                }
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
                Text(text = "Memuat film...", color = TextSecondary, fontSize = 14.sp)
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
            val primaryTrailer = trailers.firstOrNull()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // Header Backdrop with Play Trailer Overlay
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                    ) {
                        NetworkImage(
                            url = movieDetail.fullBackdropUrl ?: movieDetail.fullPosterUrl,
                            contentDescription = movieDetail.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top gradient for status bar and back button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xB3000000), Color.Transparent)
                                    )
                                )
                        )

                        // Bottom gradient fading into background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            DarkBackground.copy(alpha = 0.5f),
                                            DarkBackground
                                        ),
                                        startY = 100f
                                    )
                                )
                        )

                        // Center Play Button for Trailer (TIX ID style)
                        if (primaryTrailer != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(PrimaryRed.copy(alpha = 0.9f))
                                    .clickable { openTrailer(primaryTrailer) }
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Tonton Trailer",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Trailer badge at bottom-right of backdrop
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                    .clickable { openTrailer(primaryTrailer) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "LIHAT TRAILER",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Info Section (Poster + Title + Metadata)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Poster Card
                            Box(
                                modifier = Modifier
                                    .width(105.dp)
                                    .height(155.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            ) {
                                NetworkImage(
                                    url = movieDetail.fullPosterUrl,
                                    contentDescription = movieDetail.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Title, Ratings, Specs
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = movieDetail.title,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 25.sp
                                )

                                if (movieDetail.tagline.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"${movieDetail.tagline}\"",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Rating & Score
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = movieDetail.formattedRating,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "/10",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = " (${movieDetail.voteCount} vote)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Age Rating, Duration, Release Year
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Age Rating badge (e.g. 13+, 17+, SU)
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, PrimaryRed, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = movieDetail.ageRating,
                                            color = PrimaryRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Duration
                                    Box(
                                        modifier = Modifier
                                            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = movieDetail.formattedRuntime,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Year
                                    Box(
                                        modifier = Modifier
                                            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = movieDetail.releaseYear,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Action Buttons (Tonton Trailer, Favorit, Bagikan)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Watch Trailer Button
                        Button(
                            onClick = {
                                if (primaryTrailer != null) {
                                    openTrailer(primaryTrailer)
                                } else {
                                    Toast.makeText(context, "Trailer belum tersedia untuk film ini", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tonton Trailer",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Favorite Button
                        Button(
                            onClick = { onFavoriteToggle(movieDetail.toMovie()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFav) PrimaryRed.copy(alpha = 0.25f) else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            border = if (isFav) androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed) else null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (isFav) PrimaryRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFav) "Favorit" else "+ Simpan",
                                color = if (isFav) PrimaryRed else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Tonton film \"${movieDetail.title}\" (${movieDetail.releaseYear})! Rating: ⭐ ${movieDetail.formattedRating}/10.\nTrailer: ${primaryTrailer?.youtubeUrl ?: "https://www.themoviedb.org/movie/${movieDetail.id}"}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Film"))
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Genre Badges Flow
                if (movieDetail.genres.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
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
                                        .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
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

                // Sinopsis Section
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Ringkasan Cerita",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (movieDetail.overview.isNotBlank()) movieDetail.overview else "Belum ada sinopsis tersedia untuk film ini.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                // Video & Trailer Gallery
                if (trailers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(22.dp))
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Video & Trailer",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryRed, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${trailers.size}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(trailers) { trailer ->
                                    Card(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .clickable { openTrailer(trailer) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                    ) {
                                        Column {
                                            // YouTube Thumbnail + Play button overlay
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(125.dp)
                                            ) {
                                                NetworkImage(
                                                    url = trailer.thumbnailUrl,
                                                    contentDescription = trailer.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                // Dim overlay
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.35f))
                                                )

                                                // YouTube Play icon
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .size(42.dp)
                                                        .background(PrimaryRed, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                // Trailer Tag
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(6.dp)
                                                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = trailer.type.uppercase(),
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Video title
                                            Text(
                                                text = trailer.name,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Aktor & Sutradara (Pemeran & Kru ala TIX ID)
                if (castList.isNotEmpty() || director != null) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Aktor & Sutradara",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${castList.size + (if (director != null) 1 else 0)} Orang",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Director Card First
                                if (director != null) {
                                    item {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(85.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(74.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, StarGold, CircleShape)
                                                    .background(DarkSurfaceVariant)
                                            ) {
                                                NetworkImage(
                                                    url = director!!.fullProfileUrl,
                                                    contentDescription = director!!.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Box(
                                                modifier = Modifier
                                                    .background(StarGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, StarGold, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "SUTRADARA",
                                                    color = StarGold,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = director!!.name,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                // Cast / Actors
                                items(castList) { cast ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(85.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, Color(0xFF2C3246), CircleShape)
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
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )

                                        if (cast.character.isNotBlank()) {
                                            Text(
                                                text = "sebagai ${cast.character}",
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Similar Movies
                if (similarMovies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column {
                            Text(
                                text = "Film Rekomendasi Terkait",
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

        // Top Floating Header (Back Button & Favorite Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
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
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
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
