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

    private val _currentFeedUrl = MutableStateFlow(DEFAULT_FEED_URL)
    val currentFeedUrl: StateFlow<String> = _currentFeedUrl.asStateFlow()

    init {
        loadFeed(DEFAULT_FEED_URL)
    }

    fun onFeedUrlChanged(newUrl: String) {
        _currentFeedUrl.value = newUrl
    }

    fun loadFeed(feedUrl: String = _currentFeedUrl.value) {
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