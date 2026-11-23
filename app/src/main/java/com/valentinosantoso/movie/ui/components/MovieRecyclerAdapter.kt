package com.valentinosantoso.movie.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.valentinosantoso.movie.R
import com.valentinosantoso.movie.data.model.Movie
import com.valentinosantoso.movie.util.ImageMemoryCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MovieRecyclerAdapter(
    private var movies: List<Movie>,
    private val isFavorite: (Int) -> Boolean,
    private val onMovieClick: (Movie) -> Unit,
    private val onFavoriteToggle: (Movie) -> Unit,
    private var isDarkTheme: Boolean
) : RecyclerView.Adapter<MovieRecyclerAdapter.MovieViewHolder>() {

    fun update(newMovies: List<Movie>, darkTheme: Boolean) {
        this.movies = newMovies
        this.isDarkTheme = darkTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_card, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardRoot: CardView = itemView.findViewById(R.id.card_movie_root)
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_movie_poster)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_movie_rating)
        private val btnFavorite: ImageView = itemView.findViewById(R.id.btn_movie_favorite)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_movie_title)
        private val tvYear: TextView = itemView.findViewById(R.id.tv_movie_year)

        fun bind(movie: Movie) {
            val fav = isFavorite(movie.id)

            // Theme colors
            if (isDarkTheme) {
                cardRoot.setCardBackgroundColor(Color.parseColor("#1A1D28"))
                tvTitle.setTextColor(Color.parseColor("#FFFFFF"))
                tvYear.setTextColor(Color.parseColor("#A2A8BC"))
            } else {
                cardRoot.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                tvTitle.setTextColor(Color.parseColor("#12151D"))
                tvYear.setTextColor(Color.parseColor("#555E70"))
            }

            tvTitle.text = movie.title
            tvYear.text = if (movie.releaseYear.isNotBlank()) movie.releaseYear else "Film"
            tvRating.text = "★ ${movie.formattedRating}"

            // Rating badge rounded background
            val ratingBg = GradientDrawable().apply {
                setColor(Color.parseColor("#B3000000"))
                cornerRadius = 12f
            }
            tvRating.background = ratingBg

            // Favorite button circular background
            val favBg = GradientDrawable().apply {
                setColor(Color.parseColor("#99000000"))
                shape = GradientDrawable.OVAL
            }
            btnFavorite.background = favBg

            // Favorite icon color
            btnFavorite.setColorFilter(if (fav) Color.parseColor("#E50914") else Color.parseColor("#FFFFFF"))
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)

            btnFavorite.setOnClickListener {
                onFavoriteToggle(movie)
                notifyItemChanged(bindingAdapterPosition)
            }

            itemView.setOnClickListener {
                onMovieClick(movie)
            }

            // Load poster image
            loadPoster(movie.fullPosterUrl)
        }

        private fun loadPoster(url: String?) {
            ivPoster.setImageDrawable(null)
            if (url.isNullOrBlank()) {
                ivPoster.setBackgroundColor(Color.parseColor(if (isDarkTheme) "#262A3B" else "#EEF1F6"))
                return
            }

            val cached = ImageMemoryCache.cache.get(url)
            if (cached != null) {
                ivPoster.setImageBitmap(cached)
                return
            }

            ivPoster.setBackgroundColor(Color.parseColor(if (isDarkTheme) "#262A3B" else "#EEF1F6"))
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    if (conn.responseCode in 200..299) {
                        val bitmap = BitmapFactory.decodeStream(conn.inputStream)
                        if (bitmap != null) {
                            ImageMemoryCache.cache.put(url, bitmap)
                            withContext(Dispatchers.Main) {
                                ivPoster.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignored fallback
                }
            }
        }
    }
}
