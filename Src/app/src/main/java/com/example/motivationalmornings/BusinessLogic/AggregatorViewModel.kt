package com.example.motivationalmornings.BusinessLogic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.AggregatorArticle
import com.example.motivationalmornings.Persistence.AggregatorWebScraper
import com.example.motivationalmornings.Persistence.DefaultAggregatorWebScraper
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

    private val prefsName = "aggregator_prefs"
    private val keywordsKey = "keywords"

    private fun prefs(context: Context) =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun loadKeywords(context: Context): String {
        return prefs(context).getString(keywordsKey, "") ?: ""
    }

    fun saveKeywords(context: Context, keywords: String) {
        prefs(context).edit().putString(keywordsKey, keywords).apply()
    }

    fun filterArticles(articles: List<AggregatorArticle>, keywordText: String): List<AggregatorArticle> {
        val keywords = keywordText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (keywords.isEmpty()) return articles

        return articles.filter { article ->
            val haystack = (article.title + " " + article.url).lowercase()
            keywords.any { k -> haystack.contains(k.lowercase()) }
        }
    }

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