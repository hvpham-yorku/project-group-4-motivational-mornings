package com.example.motivationalmornings.BusinessLogic

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.DatabaseConfig
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.FakeAnalyticsRepository
import com.example.motivationalmornings.Persistence.HardcodedContentRepository
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Persistence.RoomContentRepository
import com.example.motivationalmornings.Persistence.weather.OpenMeteoWeatherRepository
import com.example.motivationalmornings.Persistence.weather.WeatherInfo
import com.example.motivationalmornings.Persistence.weather.WeatherRepository
import com.example.motivationalmornings.Presentation.refreshMotivationalWidgets
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID


class DailyContentViewModel(
    private val contentRepository: ContentRepository,
    private val analytics: Analytics,
    private val appContext: Context,
    private val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
    private val refreshWidgets: suspend () -> Unit = { refreshMotivationalWidgets(appContext) },
) : ViewModel() {

    val quote: StateFlow<String> = contentRepository.getQuote()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Loading quote...")

    /** Today's image, or null while loading / pool is empty. */
    val imageOfTheDay: StateFlow<ImageOfTheDay?> = contentRepository.getImageOfTheDay()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val intentions: StateFlow<List<String>> = contentRepository.getIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allIntentions: StateFlow<List<Intention>> = contentRepository.getAllIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allQuotes: StateFlow<List<QuoteOfTheDay>> = contentRepository.getAllQuotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ── Weather Logic ─────────────────────────────────────────────────────────

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _city = MutableStateFlow("Toronto")
    val city: StateFlow<String> = _city.asStateFlow()

    fun setCity(newCity: String) {
        _city.value = newCity
    }

    fun loadWeather() {
        val cityToSearch = _city.value.trim()
        if (cityToSearch.isBlank()) {
            _error.value = "Enter a city name"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _weather.value = weatherRepository.getCurrentWeather(cityToSearch)
            } catch (e: Exception) {
                _error.value = "Failed to load weather"
            } finally {
                _isLoading.value = false
            }
        }
    }

    val intentionSuggestions: StateFlow<List<IntentionSuggestion>> = combine(
        contentRepository.getAllIntentions(),
        intentions,
        weather
    ) { all, todayTexts, weatherInfo ->
        val weatherString = weatherInfo?.let { "${it.condition}, ${it.temperatureC}°C" }
        analytics.suggestIntentionsFromPatterns(all, weatherString, todayTexts)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun saveIntention(intention: String, weather: String? = null) {
        if (intention.isNotBlank()) {
            viewModelScope.launch {
                // Save to repository for persistence
                contentRepository.saveIntention(intention, weather)
                refreshWidgets()

                // Track the analytics event
                val imageResId = imageOfTheDay.value?.drawableResId ?: 0
                analytics.trackIntentionSet(intention, imageResId, weather)
            }
        }
    }

    val allImages: StateFlow<List<ImageOfTheDay>> = contentRepository.getAllImages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    //new addition
    val quoteOfTheDay: StateFlow<QuoteOfTheDay?> = contentRepository.getQuoteOfTheDay()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    // ── Intentions ────────────────────────────────────────────────────────────

    fun saveReflection(uid: Int, reflection: String) {
        if (reflection.isNotBlank()) {
            viewModelScope.launch {
                contentRepository.updateReflection(uid, reflection)
            }
        }
    }

    // ── Quotes ────────────────────────────────────────────────────────────────

    fun saveQuote(newQuote: String) {
        if (newQuote.isNotBlank()) {
            viewModelScope.launch {
                contentRepository.saveQuote(newQuote)
                refreshWidgets()
            }
        }
    }

    fun deleteQuote(quote: QuoteOfTheDay) {
        viewModelScope.launch {
            contentRepository.deleteQuote(quote)
            refreshWidgets()
        }
    }

    // ── Images ────────────────────────────────────────────────────────────────

    /**
     * Copies the image at [sourceUri] into internal storage, then registers it
     * in the DB so it joins the daily-image pool.
     */
    fun addImageFromUri(sourceUri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val imagesDir = File(appContext.filesDir, "images_of_the_day").apply { mkdirs() }
                    val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")

                    appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    contentRepository.addImage(ImageOfTheDay(filePath = destFile.absolutePath))
                } catch (e: Exception) {
                    // TODO: surface error to UI via a StateFlow<String?> errorMessage if needed
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteImage(image: ImageOfTheDay) {
        viewModelScope.launch {
            // Delete the physical file if it's a user-uploaded image
            image.filePath?.let { path ->
                withContext(Dispatchers.IO) { File(path).delete() }
            }
            contentRepository.deleteImage(image)
        }
    }

    // NEW FUNCTIONS
    fun likeQuote() {
        val currentQuote = quoteOfTheDay.value
        println("QUOTE OF THE DAY = $currentQuote")
        if (currentQuote == null) return

        viewModelScope.launch {
            contentRepository.recordQuoteReaction(currentQuote.uid, "LIKE")
        }
    }

    fun dislikeQuote() {
        val currentQuote = quoteOfTheDay.value ?: return
        viewModelScope.launch {
            contentRepository.recordQuoteReaction(currentQuote.uid, "DISLIKE")
        }
    }

    fun likeImage() {
        val currentImage = imageOfTheDay.value ?: return
        viewModelScope.launch {
            contentRepository.recordImageReaction(currentImage.uid, "LIKE")
        }
    }

    fun dislikeImage() {
        val currentImage = imageOfTheDay.value ?: return
        viewModelScope.launch {
            contentRepository.recordImageReaction(currentImage.uid, "DISLIKE")
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val contentRepository: ContentRepository = if (DatabaseConfig.USE_REAL_DATABASE) {
                        RoomContentRepository(AppDatabase.getDatabase(context).dailyContentDao())
                    } else {
                        HardcodedContentRepository()
                    }
                    val analytics = Analytics(FakeAnalyticsRepository())
                    return DailyContentViewModel(
                        contentRepository,
                        analytics,
                        context.applicationContext
                    ) as T
                }
            }
    }
}
