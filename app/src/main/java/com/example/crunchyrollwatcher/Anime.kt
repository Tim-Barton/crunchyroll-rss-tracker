package com.example.crunchyrollwatcher

data class Anime(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    val episodeCount: Int,
    val status: String,
    val genres: List<String>,
    val releaseYear: Int,
    val isWatched: Boolean = false,
    val watchedEpisodes: Int = 0
) {
    fun getFormattedRating(): String {
        return String.format("%.1f", rating)
    }

    fun getGenresString(): String {
        return genres.joinToString(", ")
    }

    fun getWatchProgress(): String {
        return "$watchedEpisodes/$episodeCount"
    }

    fun getWatchProgressPercentage(): Int {
        return if (episodeCount > 0) {
            ((watchedEpisodes.toFloat() / episodeCount.toFloat()) * 100).toInt()
        } else {
            0
        }
    }

    fun isCompleted(): Boolean {
        return watchedEpisodes >= episodeCount
    }
}
