package com.example.crunchyrollwatcher

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RssRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

    suspend fun fetchCrunchyrollRss(): Result<List<CrunchyrollEpisode>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://www.crunchyroll.com/rss")
                    .addHeader("User-Agent", "AnimeRSSTracker/1.0")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val xmlContent = response.body?.string()
                    if (xmlContent != null) {
                        val episodes = parseRssXml(xmlContent)
                        Result.success(episodes)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Log.e("RssRepository", "Error fetching RSS feed", e)
                Result.failure(e)
            }
        }
    }

    private fun parseRssXml(xmlContent: String): List<CrunchyrollEpisode> {
        val episodes = mutableListOf<CrunchyrollEpisode>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var currentItem: MutableMap<String, String>? = null
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            currentItem = mutableMapOf()
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (currentItem != null && currentTag.isNotEmpty()) {
                            val text = parser.text?.trim()
                            if (!text.isNullOrEmpty()) {
                                currentItem[currentTag] = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && currentItem != null) {
                            val episode = createEpisodeFromItem(currentItem)
                            if (episode != null) {
                                episodes.add(episode)
                            }
                            currentItem = null
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("RssRepository", "Error parsing RSS XML", e)
        }

        return episodes
    }

    private fun createEpisodeFromItem(item: Map<String, String>): CrunchyrollEpisode? {
        val title = item["title"] ?: return null
        val link = item["link"] ?: return null
        val description = item["description"] ?: ""
        val pubDate = item["pubDate"] ?: ""
        val guid = item["guid"] ?: link

        // Extract series title and episode info from the full title
        val titleParts = title.split(" - ")
        val seriesTitle = titleParts.firstOrNull() ?: "Unknown Series"
        val episodeInfo = if (titleParts.size > 1) {
            titleParts.drop(1).joinToString(" - ")
        } else {
            title
        }

        // Extract episode number if present
        val episodeNumber = extractEpisodeNumber(title)

        // Extract image URL from description
        val imageUrl = extractImageUrl(description)

        // Clean up description
        val cleanDescription = cleanHtmlDescription(description)

        return CrunchyrollEpisode(
            id = guid,
            title = episodeInfo,
            description = cleanDescription,
            link = link,
            imageUrl = imageUrl,
            publishDate = pubDate,
            category = "Anime",
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
        var clean = htmlDescription
            .replace(Regex("<img[^>]*>"), "")
            .replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .trim()

        clean = clean.replace(Regex("\\s+"), " ")
        return clean
    }

    fun filterEpisodes(episodes: List<CrunchyrollEpisode>, filter: RssFilter): List<CrunchyrollEpisode> {
        return when (filter.type) {
            RssFilterType.ALL -> episodes
            RssFilterType.NEW_EPISODES -> episodes.take(20) // Last 20 episodes
            RssFilterType.DUBS -> episodes.filter {
                it.title.contains("Dub", ignoreCase = true) ||
                it.seriesTitle.contains("Dub", ignoreCase = true)
            }
            RssFilterType.SUBS -> episodes.filter {
                !it.title.contains("Dub", ignoreCase = true) &&
                !it.seriesTitle.contains("Dub", ignoreCase = true)
            }
            RssFilterType.SPECIFIC_SERIES -> {
                if (filter.seriesName != null) {
                    episodes.filter {
                        it.seriesTitle.contains(filter.seriesName, ignoreCase = true)
                    }
                } else {
                    episodes
                }
            }
        }
    }

    fun sortEpisodesByDate(episodes: List<CrunchyrollEpisode>, ascending: Boolean = false): List<CrunchyrollEpisode> {
        return episodes.sortedWith { a, b ->
            try {
                val dateA = dateFormat.parse(a.publishDate)
                val dateB = dateFormat.parse(b.publishDate)
                if (ascending) {
                    dateA?.compareTo(dateB) ?: 0
                } else {
                    dateB?.compareTo(dateA) ?: 0
                }
            } catch (e: Exception) {
                0
            }
        }
    }
}
