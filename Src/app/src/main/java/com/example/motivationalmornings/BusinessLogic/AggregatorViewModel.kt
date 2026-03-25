package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.data.AggregatorArticle
import com.example.motivationalmornings.data.AggregatorWebScraper
import com.example.motivationalmornings.data.DefaultAggregatorWebScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AggregatorViewModel(
    private val scraper: AggregatorWebScraper = DefaultAggregatorWebScraper(),
) : ViewModel() {

    private val _sourceUrl = MutableStateFlow("")
    val sourceUrl: StateFlow<String> = _sourceUrl.asStateFlow()

    private val _articles = MutableStateFlow<List<AggregatorArticle>>(emptyList())
    val articles: StateFlow<List<AggregatorArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onSourceUrlChanged(url: String) {
        _sourceUrl.value = url
        _errorMessage.value = null
    }

    fun loadHeadlines() {
        val url = _sourceUrl.value.trim()
        if (url.isEmpty()) {
            _errorMessage.value = "Enter a news section URL (for example a world news page)"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            scraper.scrapeHeadlines(url)
                .onSuccess { list ->
                    _articles.value = list
                    if (list.isEmpty()) {
                        _errorMessage.value =
                            "No headlines were found. Try another page with story links, such as a major news section."
                    }
                }
                .onFailure { err ->
                    _articles.value = emptyList()
                    _errorMessage.value =
                        err.message ?: "Could not load that page. Check the URL and your connection."
                }
            _isLoading.value = false
        }
    }
}
