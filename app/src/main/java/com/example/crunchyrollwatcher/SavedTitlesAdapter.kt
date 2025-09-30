package com.example.crunchyrollwatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class SavedTitlesAdapter(
    private val onTitleClick: (SavedTitle) -> Unit,
    private val onRemoveClick: (SavedTitle) -> Unit
) : ListAdapter<SavedTitle, SavedTitlesAdapter.SavedTitleViewHolder>(SavedTitleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedTitleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_title, parent, false)
        return SavedTitleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedTitleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SavedTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleImage: ImageView = itemView.findViewById(R.id.titleImage)
        private val titleName: TextView = itemView.findViewById(R.id.titleName)
        private val titleCategory: TextView = itemView.findViewById(R.id.titleCategory)
        private val titleDescription: TextView = itemView.findViewById(R.id.titleDescription)
        private val episodeCount: TextView = itemView.findViewById(R.id.episodeCount)
        private val dateAdded: TextView = itemView.findViewById(R.id.dateAdded)
        private val lastEpisodeLayout: LinearLayout = itemView.findViewById(R.id.lastEpisodeLayout)
        private val lastEpisodeSeen: TextView = itemView.findViewById(R.id.lastEpisodeSeen)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(savedTitle: SavedTitle) {
            titleName.text = savedTitle.title
            titleCategory.text = savedTitle.category

            // Set description
            if (!savedTitle.description.isNullOrEmpty()) {
                titleDescription.visibility = View.VISIBLE
                titleDescription.text = savedTitle.description
            } else {
                titleDescription.visibility = View.GONE
            }

            // Set episode count
            val count = savedTitle.episodeCount
            episodeCount.text = when {
                count <= 0 -> "No episodes tracked"
                count == 1 -> "1 episode tracked"
                else -> "$count episodes tracked"
            }

            // Set date added
            dateAdded.text = formatDate(savedTitle.dateAdded)

            // Show last episode seen if available
            if (!savedTitle.lastEpisodeSeen.isNullOrEmpty()) {
                lastEpisodeLayout.visibility = View.VISIBLE
                lastEpisodeSeen.text = "Last seen: Episode ${savedTitle.lastEpisodeSeen}"
            } else {
                lastEpisodeLayout.visibility = View.GONE
            }

            // Load title image
            if (!savedTitle.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(savedTitle.imageUrl)
                    .placeholder(R.drawable.ic_anime_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(titleImage)
            } else {
                titleImage.setImageResource(R.drawable.ic_anime_placeholder)
            }

            // Set click listeners
            itemView.setOnClickListener {
                onTitleClick(savedTitle)
            }

            removeButton.setOnClickListener {
                onRemoveClick(savedTitle)
            }
        }

        private fun formatDate(timestamp: Long): String {
            return try {
                val date = Date(timestamp)
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Added ${format.format(date)}"
            } catch (e: Exception) {
                "Recently added"
            }
        }
    }

    class SavedTitleDiffCallback : DiffUtil.ItemCallback<SavedTitle>() {
        override fun areItemsTheSame(oldItem: SavedTitle, newItem: SavedTitle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedTitle, newItem: SavedTitle): Boolean {
            return oldItem == newItem
        }
    }
}
