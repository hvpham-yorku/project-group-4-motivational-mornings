package com.example.motivationalmornings.Persistence

class RssRepository {
    private val dummyItems = listOf(
        RssItem(
            id = 1,
            title = "Morning Motivation",
            description = "Start your day with a positive quote.",
            link = "https://example.com/morning-motivation"
        ),
        RssItem(
            id = 2,
            title = "Mindfulness Minute",
            description = "A short mindfulness exercise for your commute.",
            link = "https://example.com/mindfulness-minute"
        ),
        RssItem(
            id = 3,
            title = "Gratitude Check",
            description = "Three things to be grateful for today.",
            link = "https://example.com/gratitude-check"
        )
    )

    fun getRssItems(): List<RssItem> = dummyItems
}