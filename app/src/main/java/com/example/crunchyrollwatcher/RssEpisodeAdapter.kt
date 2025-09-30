package com.example.crunchyrollwatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class RssEpisodeAdapter(
    private val onEpisodeClick: (CrunchyrollEpisode) -> Unit,
    private val onFavoriteClick: (CrunchyrollEpisode) -> Unit,
    private val isTitleSaved: (String) -> Boolean
) : ListAdapter<CrunchyrollEpisode, RssEpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val episodeImage: ImageView = itemView.findViewById(R.id.episodeImage)
        private val seriesTitle: TextView = itemView.findViewById(R.id.seriesTitle)
        private val episodeTitle: TextView = itemView.findViewById(R.id.episodeTitle)
        private val episodeNumber: TextView = itemView.findViewById(R.id.episodeNumber)
        private val episodeDescription: TextView = itemView.findViewById(R.id.episodeDescription)
        private val publishDate: TextView = itemView.findViewById(R.id.publishDate)
        private val watchedIndicator: View = itemView.findViewById(R.id.watchedIndicator)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)

        fun bind(episode: CrunchyrollEpisode) {
            seriesTitle.text = episode.seriesTitle
            episodeTitle.text = episode.title
            episodeDescription.text = episode.description

            // Set episode number if available
            if (!episode.episodeNumber.isNullOrEmpty()) {
                episodeNumber.visibility = View.VISIBLE
                episodeNumber.text = "Episode ${episode.episodeNumber}"
            } else {
                episodeNumber.visibility = View.GONE
            }

            // Format and set publish date
            publishDate.text = formatDate(episode.publishDate)

            // Set watched indicator visibility
            watchedIndicator.visibility = if (episode.isWatched) View.VISIBLE else View.GONE

            // Load episode image
            if (!episode.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(episode.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(episodeImage)
            } else {
                episodeImage.setImageResource(R.drawable.ic_placeholder)
            }

            // Update favorite button state
            updateFavoriteButton(episode.seriesTitle)

            // Set click listeners
            itemView.setOnClickListener {
                onEpisodeClick(episode)
            }

            favoriteButton.setOnClickListener {
                onFavoriteClick(episode)
                updateFavoriteButton(episode.seriesTitle)
            }
        }

        private fun updateFavoriteButton(seriesTitle: String) {
            val isSaved = isTitleSaved(seriesTitle)
            favoriteButton.setImageResource(
                if (isSaved) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
            favoriteButton.contentDescription = if (isSaved) "Remove from favorites" else "Add to favorites"
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<CrunchyrollEpisode>() {
        override fun areItemsTheSame(oldItem: CrunchyrollEpisode, newItem: CrunchyrollEpisode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CrunchyrollEpisode, newItem: CrunchyrollEpisode): Boolean {
            return oldItem == newItem
        }
    }
}
