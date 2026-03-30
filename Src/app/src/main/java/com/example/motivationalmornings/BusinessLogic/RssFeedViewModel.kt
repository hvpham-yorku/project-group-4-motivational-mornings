package com.example.motivationalmornings.BusinessLogic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.DailyContentDao
import com.example.motivationalmornings.Persistence.RssFeedUrl
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RssFeedViewModel(
    private val repository: RssRepository = RssRepository(),
    private val dailyContentDao: DailyContentDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _rssItems = MutableStateFlow<List<RssItem>>(emptyList())
    val rssItems: StateFlow<List<RssItem>> = _rssItems.asStateFlow()

    private val _currentFeedUrl = MutableStateFlow("")
    val currentFeedUrl: StateFlow<String> = _currentFeedUrl.asStateFlow()

    private val _subscribedFeeds = MutableStateFlow<List<String>>(emptyList())
    val subscribedFeeds: StateFlow<List<String>> = _subscribedFeeds.asStateFlow()

    init {
        dailyContentDao?.let { dao ->
            viewModelScope.launch {
                dao.getRssFeedUrls()
                    .catch { }
                    .collect { urls -> _subscribedFeeds.value = urls }
            }
        }
    }

    fun onFeedUrlChanged(newUrl: String) {
        _currentFeedUrl.value = newUrl
    }

    fun subscribeToFeed() {
        val url = _currentFeedUrl.value.trim()
        if (url.isEmpty() || _subscribedFeeds.value.contains(url)) return
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dailyContentDao?.insertRssFeedUrl(RssFeedUrl(url = url))
            }
            _subscribedFeeds.value = _subscribedFeeds.value + url
            loadFeed(url)
            _currentFeedUrl.value = "" // Clear input after subscribing
        }
    }

    fun loadFeed(feedUrl: String) {
        viewModelScope.launch {
            val items = withContext(ioDispatcher) {
                repository.getRssItems(feedUrl)
            }
            _rssItems.value = items
        }
    }

    fun unsubscribeFromFeed(url: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dailyContentDao?.deleteRssFeedUrl(url)
            }
            _subscribedFeeds.value = _subscribedFeeds.value.filter { it != url }
        }
    }

    companion object {

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(context)
                    return RssFeedViewModel(
                        repository = RssRepository(),
                        dailyContentDao = database.dailyContentDao()
                    ) as T
                }
            }
    }
}
