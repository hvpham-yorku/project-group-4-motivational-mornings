package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.data.ContentRepository
import com.example.motivationalmornings.data.HardcodedContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyContentViewModel(private val contentRepository: ContentRepository) : ViewModel() {

    val quote: StateFlow<String> = contentRepository.getQuote()
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val imageResId: StateFlow<Int> = contentRepository.getImageResId()
        .stateIn(viewModelScope, SharingStarted.Lazily, R.drawable.ic_launcher_background)

    // Updated to List<String> to match frontend
    private val _intentions = MutableStateFlow<List<String>>(emptyList())
    val intentions: StateFlow<List<String>> = _intentions.asStateFlow()

    fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            viewModelScope.launch {
                // Add to existing list (in-memory for now)
                val currentIntentions = _intentions.value.toMutableList()
                currentIntentions.add(0, intention) // Add to top
                _intentions.value = currentIntentions

                // Also save to repository for persistence
                contentRepository.saveIntention(intention)
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyContentViewModel(HardcodedContentRepository()) as T
            }
        }
    }
}
