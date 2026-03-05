package com.example.motivationalmornings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.analytics.Analytics
import com.example.motivationalmornings.data.ContentRepository
import com.example.motivationalmornings.data.FakeAnalyticsRepository
import com.example.motivationalmornings.data.RoomContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyContentViewModel(
    private val contentRepository: ContentRepository,
    private val analytics: Analytics
) : ViewModel() {

    val quote: StateFlow<String> = contentRepository.getQuote()
        .stateIn(viewModelScope, SharingStarted.Lazily, "Loading quote...")

    val imageResId: StateFlow<Int> = contentRepository.getImageResId()
        .stateIn(viewModelScope, SharingStarted.Lazily, R.drawable.ic_launcher_background)

    val intentions: StateFlow<List<String>> = contentRepository.getIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            viewModelScope.launch {
                // Save to repository for persistence
                contentRepository.saveIntention(intention)

                // Track the analytics event
                analytics.trackIntentionSet(intention, imageResId.value)
            }
        }
    }

    fun saveQuote(newQuote: String) {
        if (newQuote.isNotBlank()) {
            viewModelScope.launch {
                contentRepository.saveQuote(newQuote)
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context)
                val contentRepository = RoomContentRepository(database.dailyContentDao())
                val analyticsRepository = FakeAnalyticsRepository()
                val analytics = Analytics(analyticsRepository)
                return DailyContentViewModel(
                    contentRepository,
                    analytics
                ) as T
            }
        }
    }
}
