package com.example.motivationalmornings.BusinessLogic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.AggregatorArticle
import com.example.motivationalmornings.Persistence.AggregatorSourceUrl
import com.example.motivationalmornings.Persistence.AggregatorWebScraper
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.DailyContentDao
import com.example.motivationalmornings.Persistence.DefaultAggregatorWebScraper
import com.example.motivationalmornings.Persistence.TrackedStock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StockQuote(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double
)

class AggregatorViewModel(
    private val scraper: AggregatorWebScraper = DefaultAggregatorWebScraper(),
    private val dailyContentDao: DailyContentDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _sourceUrl = MutableStateFlow("")
    val sourceUrl: StateFlow<String> = _sourceUrl.asStateFlow()

    private val _subscribedSources = MutableStateFlow<List<String>>(emptyList())
    val subscribedSources: StateFlow<List<String>> = _subscribedSources.asStateFlow()

    private val _selectedSourceUrl = MutableStateFlow<String?>(null)
    val selectedSourceUrl: StateFlow<String?> = _selectedSourceUrl.asStateFlow()

    private val _articlesByUrl = MutableStateFlow<Map<String, List<AggregatorArticle>>>(emptyMap())

    private val _articles = MutableStateFlow<List<AggregatorArticle>>(emptyList())
    val articles: StateFlow<List<AggregatorArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshingAll = MutableStateFlow(false)
    val isRefreshingAll: StateFlow<Boolean> = _isRefreshingAll.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Stock Tracker States
    private val _trackedStocks = MutableStateFlow<List<String>>(emptyList())
    val trackedStocks: StateFlow<List<String>> = _trackedStocks.asStateFlow()

    private val _stockQuotes = MutableStateFlow<List<StockQuote>>(emptyList())
    val stockQuotes: StateFlow<List<StockQuote>> = _stockQuotes.asStateFlow()

    private val _stockSymbolInput = MutableStateFlow("")
    val stockSymbolInput: StateFlow<String> = _stockSymbolInput.asStateFlow()

    private val prefsName = "aggregator_prefs"
    private val keywordsKey = "keywords"

    init {
        dailyContentDao?.let { dao ->
            viewModelScope.launch {
                dao.getAggregatorSourceUrls()
                    .catch { }
                    .collect { urls ->
                        _subscribedSources.value = urls
                        reconcileSelection(urls)
                        refreshDisplayedArticles()
                    }
            }
            viewModelScope.launch {
                dao.getTrackedStocks()
                    .catch { }
                    .collect { stocks ->
                        _trackedStocks.value = stocks
                        refreshStockQuotes(stocks)
                    }
            }
        }
    }

    private fun reconcileSelection(urls: List<String>) {
        val sel = _selectedSourceUrl.value
        when {
            sel != null && sel !in urls -> _selectedSourceUrl.value = urls.firstOrNull()
            sel == null && urls.isNotEmpty() -> _selectedSourceUrl.value = urls.first()
        }
    }

    private fun refreshDisplayedArticles() {
        val sel = _selectedSourceUrl.value
        _articles.value = sel?.let { _articlesByUrl.value[it] } ?: emptyList()
    }

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

    /**
     * Call when the aggregator screen is shown. Reloads headlines for every saved source in parallel.
     */
    fun refreshAllSavedSources() {
        viewModelScope.launch {
            val urls: List<String> = if (dailyContentDao != null) {
                withContext(ioDispatcher) {
                    dailyContentDao.getAggregatorSourceUrls().first()
                }
            } else {
                _subscribedSources.value
            }
            if (urls.isEmpty()) return@launch
            scrapeUrlsIntoMap(urls)
        }
        viewModelScope.launch {
            val stocks = if (dailyContentDao != null) {
                withContext(ioDispatcher) { dailyContentDao.getTrackedStocks().first() }
            } else {
                _trackedStocks.value
            }
            refreshStockQuotes(stocks)
        }
    }

    fun selectSource(url: String) {
        _selectedSourceUrl.value = url
        refreshDisplayedArticles()
        _errorMessage.value = null
    }

    fun removeSource(url: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dailyContentDao?.deleteAggregatorSourceUrl(url)
            }
            if (dailyContentDao == null) {
                _subscribedSources.value = _subscribedSources.value.filter { it != url }
                reconcileSelection(_subscribedSources.value)
            }
            val newMap = _articlesByUrl.value.toMutableMap()
            newMap.remove(url)
            _articlesByUrl.value = newMap
            refreshDisplayedArticles()
        }
    }

    fun addSource() {
        val url = _sourceUrl.value.trim()
        if (url.isEmpty()) {
            _errorMessage.value = "Enter a news section URL (for example a world news page)"
            return
        }
        if (_subscribedSources.value.contains(url)) {
            viewModelScope.launch {
                selectSource(url)
                _sourceUrl.value = ""
                scrapeUrlsIntoMap(listOf(url), showGlobalLoading = true)
            }
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            withContext(ioDispatcher) {
                dailyContentDao?.insertAggregatorSourceUrl(AggregatorSourceUrl(url = url))
            }
            if (dailyContentDao == null) {
                _subscribedSources.value = _subscribedSources.value + url
            }
            _selectedSourceUrl.value = url
            _sourceUrl.value = ""
            scrapeUrlsIntoMap(listOf(url), showGlobalLoading = true)
        }
    }

    // Stock Actions
    fun onStockSymbolInputChanged(symbol: String) {
        _stockSymbolInput.value = symbol.uppercase()
    }

    fun addStock() {
        val symbol = _stockSymbolInput.value.trim().uppercase()
        if (symbol.isEmpty()) return
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dailyContentDao?.insertTrackedStock(TrackedStock(symbol))
            }
            if (dailyContentDao == null) {
                _trackedStocks.value = _trackedStocks.value + symbol
            }
            _stockSymbolInput.value = ""
        }
    }

    fun removeStock(symbol: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                dailyContentDao?.deleteTrackedStock(symbol)
            }
            if (dailyContentDao == null) {
                _trackedStocks.value = _trackedStocks.value.filter { it != symbol }
            }
        }
    }

    private suspend fun refreshStockQuotes(symbols: List<String>) {
        if (symbols.isEmpty()) {
            _stockQuotes.value = emptyList()
            return
        }
        // Simulated stock data as real APIs require keys
        val mockQuotes = symbols.map { symbol ->
            val basePrice = when(symbol) {
                "AAPL" -> 180.0
                "GOOGL" -> 140.0
                "MSFT" -> 370.0
                "AMZN" -> 145.0
                "TSLA" -> 240.0
                else -> 100.0
            }
            val randomChange = (Math.random() - 0.5) * 5.0
            StockQuote(
                symbol = symbol,
                price = basePrice + randomChange,
                change = randomChange,
                changePercent = (randomChange / basePrice) * 100.0
            )
        }
        _stockQuotes.value = mockQuotes
    }

    /**
     * Loads headlines from the current URL field without persisting (used by unit tests).
     */
    fun loadHeadlines() {
        val url = _sourceUrl.value.trim()
        if (url.isEmpty()) {
            _errorMessage.value = "Enter a news section URL (for example a world news page)"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // Run scraper on caller context so unit tests with test dispatcher observe results.
            val result = scraper.scrapeHeadlines(url)
            result
                .onSuccess { list ->
                    val newMap = _articlesByUrl.value.toMutableMap()
                    newMap[url] = list
                    _articlesByUrl.value = newMap
                    _selectedSourceUrl.value = url
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

    private suspend fun scrapeUrlsIntoMap(urls: List<String>, showGlobalLoading: Boolean = false) {
        if (urls.isEmpty()) return
        if (showGlobalLoading) {
            _isLoading.value = true
            _errorMessage.value = null
        } else {
            _isRefreshingAll.value = true
        }
        try {
            val newMap = _articlesByUrl.value.toMutableMap()
            var firstError: String? = null
            coroutineScope {
                val deferred = urls.map { url ->
                    async(ioDispatcher) {
                        url to scraper.scrapeHeadlines(url)
                    }
                }
                deferred.awaitAll().forEach { (url, result) ->
                    result
                        .onSuccess { list -> newMap[url] = list }
                        .onFailure { err ->
                            if (firstError == null) {
                                firstError = err.message
                                    ?: "Could not load that page. Check the URL and your connection."
                            }
                        }
                }
            }
            _articlesByUrl.value = newMap.toMap()
            refreshDisplayedArticles()
            val sel = _selectedSourceUrl.value
            if (firstError != null && sel != null && newMap[sel].isNullOrEmpty()) {
                _errorMessage.value = firstError
            } else if (firstError != null && sel == null) {
                _errorMessage.value = firstError
            } else {
                _errorMessage.value = null
            }
        } finally {
            _isRefreshingAll.value = false
            if (showGlobalLoading) {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(context)
                    return AggregatorViewModel(
                        dailyContentDao = database.dailyContentDao(),
                    ) as T
                }
            }
    }
}
