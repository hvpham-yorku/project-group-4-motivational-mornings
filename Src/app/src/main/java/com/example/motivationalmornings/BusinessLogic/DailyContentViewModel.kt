package com.example.motivationalmornings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Presentation.refreshMotivationalWidgets
import com.example.motivationalmornings.analytics.Analytics
import com.example.motivationalmornings.data.ContentRepository
import com.example.motivationalmornings.data.FakeAnalyticsRepository
import com.example.motivationalmornings.data.HardcodedContentRepository
import com.example.motivationalmornings.data.RoomContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyContentViewModel(
    private val contentRepository: ContentRepository,
    private val analytics: Analytics,
    private val appContext: Context,
    private val refreshWidgets: suspend () -> Unit = { refreshMotivationalWidgets(appContext) },
) : ViewModel() {

    val quote: StateFlow<String> = contentRepository.getQuote()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Loading quote...")

    val imageResId: StateFlow<Int> = contentRepository.getImageResId()
        .stateIn(viewModelScope, SharingStarted.Lazily, R.drawable.ic_launcher_background)

    val intentions: StateFlow<List<String>> = contentRepository.getIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allIntentions: StateFlow<List<Intention>> = contentRepository.getAllIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allQuotes: StateFlow<List<QuoteOfTheDay>> = contentRepository.getAllQuotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            viewModelScope.launch {
                // Save to repository for persistence
                contentRepository.saveIntention(intention)
                refreshWidgets()

                // Track the analytics event
                analytics.trackIntentionSet(intention, imageResId.value)
            }
        }
    }

    fun saveReflection(uid: Int, reflection: String) {
        if (reflection.isNotBlank()) {
            viewModelScope.launch {
                contentRepository.updateReflection(uid, reflection)
            }
        }
    }

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

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val contentRepository: ContentRepository = if (DatabaseConfig.USE_REAL_DATABASE) {
                    RoomContentRepository(AppDatabase.getDatabase(context).dailyContentDao())
                } else {
                    HardcodedContentRepository()
                }
                val analyticsRepository = FakeAnalyticsRepository()
                val analytics = Analytics(analyticsRepository)
                return DailyContentViewModel(
                    contentRepository,
                    analytics,
                    context.applicationContext
                ) as T
            }
        }
    }
}
