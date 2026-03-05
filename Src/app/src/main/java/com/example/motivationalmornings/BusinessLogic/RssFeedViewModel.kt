package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RssFeedViewModel(
    private val repository: RssRepository = RssRepository()
) : ViewModel() {

    private val _rssItems = MutableStateFlow<List<RssItem>>(emptyList())
    val rssItems: StateFlow<List<RssItem>> = _rssItems.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        _rssItems.value = repository.getRssItems()
    }
}