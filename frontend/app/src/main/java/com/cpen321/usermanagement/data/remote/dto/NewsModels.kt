package com.cpen321.usermanagement.data.remote.dto

data class NewsArticle(
    val title: String,
    val url: String,
    val source: String,
    val publishedAt: String
)

data class HobbyNews(
    val hobby: String,
    val articles: List<NewsArticle>
)

data class NewsRequest(
    val hobbies: List<String>
)

data class NewsResponse(
    val results: List<HobbyNews>
)