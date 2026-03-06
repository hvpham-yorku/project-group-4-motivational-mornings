package com.example.motivationalmornings.BusinessLogic

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
}
