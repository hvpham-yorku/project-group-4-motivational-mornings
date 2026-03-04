package com.example.motivationalmornings.BusinessLogic

data class RssItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String
)

data class RssFeed(
    val title: String,
    val items: List<RssItem>
)
