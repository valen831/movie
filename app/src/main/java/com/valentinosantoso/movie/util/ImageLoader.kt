package com.valentinosantoso.movie.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.valentinosantoso.movie.ui.theme.DarkSurfaceVariant
import com.valentinosantoso.movie.ui.theme.PrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageMemoryCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory for cache

    val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
}

sealed interface ImageState {
    data object Loading : ImageState
    data class Success(val bitmap: Bitmap) : ImageState
    data object Error : ImageState
}

@Composable
fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector? = null
) {
    var state by remember(url) {
        val cached = url?.let { ImageMemoryCache.cache.get(it) }
        mutableStateOf(if (cached != null) ImageState.Success(cached) else ImageState.Loading)
    }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            state = ImageState.Error
            return@LaunchedEffect
        }

        val cached = ImageMemoryCache.cache.get(url)
        if (cached != null) {
            state = ImageState.Success(cached)
            return@LaunchedEffect
        }

        state = ImageState.Loading
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode in 200..299) {
                    val input: InputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        ImageMemoryCache.cache.put(url, bitmap)
                        state = ImageState.Success(bitmap)
                    } else {
                        state = ImageState.Error
                    }
                } else {
                    state = ImageState.Error
                }
            } catch (e: Exception) {
                state = ImageState.Error
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Crossfade(targetState = state, label = "image_crossfade") { currentState ->
            when (currentState) {
                is ImageState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryRed,
                            strokeWidth = 2.dp
                        )
                    }
                }

                is ImageState.Success -> {
                    Image(
                        bitmap = currentState.bitmap.asImageBitmap(),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                }

                is ImageState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display placeholder or fallback box
                        if (placeholderIcon != null) {
                            Icon(
                                imageVector = placeholderIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
