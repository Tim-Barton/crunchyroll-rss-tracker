package com.example.crunchyrollwatcher

import com.google.gson.annotations.SerializedName
import java.util.Date

data class RssFeed(
    @SerializedName("channel")
    val channel: RssChannel
)

data class RssChannel(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("item")
    val items: List<RssItem>
)

data class RssItem(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("pubDate")
    val pubDate: String,
    @SerializedName("guid")
    val guid: String,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("enclosure")
    val enclosure: RssEnclosure? = null
)

data class RssEnclosure(
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("length")
    val length: String
)

// Converted model for easier use in the app
data class CrunchyrollEpisode(
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val imageUrl: String?,
    val publishDate: String,
    val category: String,
    val seriesTitle: String,
    val episodeNumber: String?,
    val isWatched: Boolean = false
) {
    companion object {
        fun fromRssItem(rssItem: RssItem): CrunchyrollEpisode {
            // Extract series title and episode info from the full title
            val titleParts = rssItem.title.split(" - ")
            val seriesTitle = titleParts.firstOrNull() ?: "Unknown Series"
            val episodeInfo = if (titleParts.size > 1) titleParts.drop(1).joinToString(" - ") else ""

            // Extract episode number if present
            val episodeNumber = extractEpisodeNumber(rssItem.title)

            // Extract image URL from description (it contains HTML with img tag)
            val imageUrl = extractImageUrl(rssItem.description)

            // Clean up description by removing HTML tags
            val cleanDescription = cleanHtmlDescription(rssItem.description)

            return CrunchyrollEpisode(
                id = rssItem.guid,
                title = episodeInfo.ifEmpty { rssItem.title },
                description = cleanDescription,
                link = rssItem.link,
                imageUrl = imageUrl,
                publishDate = rssItem.pubDate,
                category = rssItem.category ?: "Anime",
                seriesTitle = seriesTitle,
                episodeNumber = episodeNumber
            )
        }

        private fun extractEpisodeNumber(title: String): String? {
            val episodeRegex = Regex("Episode\\s+(\\d+)")
            return episodeRegex.find(title)?.groupValues?.get(1)
        }

        private fun extractImageUrl(htmlDescription: String): String? {
            val imgRegex = Regex("<img[^>]+src=\"([^\"]+)\"")
            return imgRegex.find(htmlDescription)?.groupValues?.get(1)
        }

        private fun cleanHtmlDescription(htmlDescription: String): String {
            // Remove HTML tags and get clean text
            var clean = htmlDescription
                .replace(Regex("<img[^>]*>"), "")
                .replace(Regex("<br\\s*/?>"), "\n")
                .replace(Regex("<[^>]+>"), "")
                .trim()

            // Remove excessive whitespace
            clean = clean.replace(Regex("\\s+"), " ")

            return clean
        }
    }
}

// Filter options for RSS feed
enum class RssFilterType {
    ALL,
    NEW_EPISODES,
    DUBS,
    SUBS,
    SPECIFIC_SERIES
}

data class RssFilter(
    val type: RssFilterType = RssFilterType.ALL,
    val seriesName: String? = null,
    val language: String? = null
)

// Model for saved/favorite titles
data class SavedTitle(
    val id: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val category: String = "Anime",
    val dateAdded: Long = System.currentTimeMillis(),
    val episodeCount: Int = 0,
    val lastEpisodeSeen: String? = null
) {
    companion object {
        fun fromCrunchyrollEpisode(episode: CrunchyrollEpisode): SavedTitle {
            return SavedTitle(
                id = generateIdFromTitle(episode.seriesTitle),
                title = episode.seriesTitle,
                description = episode.description,
                imageUrl = episode.imageUrl,
                category = episode.category,
                episodeCount = 1,
                lastEpisodeSeen = episode.episodeNumber
            )
        }

        private fun generateIdFromTitle(title: String): String {
            // Generate a consistent ID from the series title
            return title.lowercase().replace(Regex("[^a-z0-9]"), "_")
        }
    }
}

// Repository interface for managing saved titles
interface SavedTitlesRepository {
    fun getSavedTitles(): List<SavedTitle>
    fun saveTitleToList(savedTitle: SavedTitle): Boolean
    fun removeTitleFromList(titleId: String): Boolean
    fun isTitleSaved(titleId: String): Boolean
    fun updateEpisodeCount(titleId: String, episodeNumber: String?): Boolean
}
