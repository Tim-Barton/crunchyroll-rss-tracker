package com.example.crunchyrollwatcher

import retrofit2.Response
import retrofit2.http.GET

interface RssService {
    @GET("rss")
    suspend fun getCrunchyrollRssFeed(): Response<String>
}
