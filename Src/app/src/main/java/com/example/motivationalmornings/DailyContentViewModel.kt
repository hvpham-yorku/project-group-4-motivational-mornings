package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.data.ContentRepository
import com.example.motivationalmornings.data.HardcodedContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyContentViewModel(private val contentRepository: ContentRepository) : ViewModel() {
    val quote: StateFlow<String> = contentRepository.getQuote()
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val imageResId: StateFlow<Int> = contentRepository.getImageResId()
        .stateIn(viewModelScope, SharingStarted.Lazily, R.drawable.ic_launcher_background)

    val intentions: StateFlow<String> = contentRepository.getIntentions()
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun saveIntention(intention: String) {
        viewModelScope.launch {
            contentRepository.saveIntention(intention)
        }
    }

    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyContentViewModel(HardcodedContentRepository()) as T
            }
        }
    }
}
