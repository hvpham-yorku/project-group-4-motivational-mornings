package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RssItem(
    val id: Int,
    val title: String,
    val description: String,
    val link: String
)

// This acts as a dummy "database"/backend for now
class RssRepository {

    // Hardcoded list can be later replace with a real DB or network
    private val allItems = listOf(
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

    // Track subscribed items (starts with all items subscribed)
    private val _subscribedItems = MutableStateFlow(allItems)
    val subscribedItems: StateFlow<List<RssItem>> = _subscribedItems.asStateFlow()

    fun getRssItems(): List<RssItem> = _subscribedItems.value

    fun unsubscribeFromFeed(itemId: Int) {
        _subscribedItems.value = _subscribedItems.value.filter { it.id != itemId }
    }

    fun getAllAvailableItems(): List<RssItem> = allItems
}

class RssFeedViewModel(
    private val repository: RssRepository = RssRepository()
) : ViewModel() {

    private val _rssItems = MutableStateFlow<List<RssItem>>(emptyList())
    val rssItems: StateFlow<List<RssItem>> = _rssItems.asStateFlow()

    init {
        // In future, replace this with a suspend call or use cases
        loadFeed()
    }

    private fun loadFeed() {
        _rssItems.value = repository.getRssItems()
    }

    fun unsubscribeFromFeed(itemId: Int) {
        repository.unsubscribeFromFeed(itemId)
        // Reload the feed to reflect the change
        loadFeed()
    }
}

/*package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RssFeedViewModel(
    private val repository: RssRepository = RssRepository()
) : ViewModel() {

    private val _rssItems = MutableStateFlow<List<RssItem>>(emptyList())
    val rssItems: StateFlow<List<RssItem>> = _rssItems.asStateFlow()

    private val _currentFeedUrl = MutableStateFlow("")
    val currentFeedUrl: StateFlow<String> = _currentFeedUrl.asStateFlow()

    private val _subscribedFeeds = MutableStateFlow<List<String>>(emptyList())
    val subscribedFeeds: StateFlow<List<String>> = _subscribedFeeds.asStateFlow()

    init {
        // Optionally load a default if needed, but starting empty as requested for "adding"
    }

    fun onFeedUrlChanged(newUrl: String) {
        _currentFeedUrl.value = newUrl
    }

    fun subscribeToFeed() {
        val url = _currentFeedUrl.value.trim()
        if (url.isNotEmpty() && !_subscribedFeeds.value.contains(url)) {
            _subscribedFeeds.value = _subscribedFeeds.value + url
            loadFeed(url)
            _currentFeedUrl.value = "" // Clear input after subscribing
        }
    }

    fun loadFeed(feedUrl: String) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                repository.getRssItems(feedUrl)
            }
            _rssItems.value = items
        }
    }

    companion object {
        private const val DEFAULT_FEED_URL = "https://example.com/feed"
    }
}*/
