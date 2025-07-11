package com.example.crunchyrollwatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AnimeAdapter(
    private val onAnimeClick: (Anime) -> Unit
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    private var animeList: List<Anime> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount(): Int = animeList.size

    fun updateList(newList: List<Anime>) {
        val diffCallback = AnimeDiffCallback(animeList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        animeList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.animeCardView)
        private val imageView: ImageView = itemView.findViewById(R.id.animeImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.animeTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.animeDescriptionTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.animeRatingTextView)
        private val genresTextView: TextView = itemView.findViewById(R.id.animeGenresTextView)
        private val episodeCountTextView: TextView = itemView.findViewById(R.id.animeEpisodeCountTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.animeStatusTextView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.animeProgressBar)
        private val progressTextView: TextView = itemView.findViewById(R.id.animeProgressTextView)
        private val addToWatchlistButton: MaterialButton = itemView.findViewById(R.id.addToWatchlistButton)

        fun bind(anime: Anime) {
            titleTextView.text = anime.title
            descriptionTextView.text = anime.description
            ratingTextView.text = "★ ${anime.getFormattedRating()}"
            genresTextView.text = anime.getGenresString()
            episodeCountTextView.text = "${anime.episodeCount} episodes"
            statusTextView.text = anime.status

            // Set progress
            if (anime.isWatched) {
                progressBar.visibility = View.VISIBLE
                progressTextView.visibility = View.VISIBLE
                progressBar.progress = anime.getWatchProgressPercentage()
                progressTextView.text = anime.getWatchProgress()

                if (anime.isCompleted()) {
                    progressTextView.text = "Completed"
                    addToWatchlistButton.text = "Completed"
                    addToWatchlistButton.isEnabled = false
                } else {
                    addToWatchlistButton.text = "Continue Watching"
                    addToWatchlistButton.isEnabled = true
                }
            } else {
                progressBar.visibility = View.GONE
                progressTextView.visibility = View.GONE
                addToWatchlistButton.text = "Add to Watchlist"
                addToWatchlistButton.isEnabled = true
            }

            // Set click listeners
            cardView.setOnClickListener {
                onAnimeClick(anime)
            }

            addToWatchlistButton.setOnClickListener {
                onAnimeClick(anime)
            }

            // TODO: Load image using an image loading library like Glide or Coil
            // For now, set a placeholder
            imageView.setImageResource(R.drawable.ic_anime_placeholder)
        }
    }

    private class AnimeDiffCallback(
        private val oldList: List<Anime>,
        private val newList: List<Anime>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
