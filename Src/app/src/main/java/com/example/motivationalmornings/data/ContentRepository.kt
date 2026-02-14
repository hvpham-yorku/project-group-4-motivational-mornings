package com.example.motivationalmornings.data

import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

interface ContentRepository {
    fun getQuote(): Flow<String>
    fun getImageResId(): Flow<Int>
    fun getIntentions(): Flow<List<String>>  // ✅ Function signature
    suspend fun saveIntention(intention: String)
}

class HardcodedContentRepository : ContentRepository {
    // Private backing flow (no override needed)
    private val _intentionsFlow = MutableStateFlow<List<String>>(emptyList())

    override fun getQuote(): Flow<String> =
        flowOf("The best way to predict the future is to create it.")

    override fun getImageResId(): Flow<Int> =
        flowOf(R.drawable.imageotd)

    // ✅ Override the INTERFACE FUNCTION (not a property)
    override fun getIntentions(): Flow<List<String>> = _intentionsFlow.asStateFlow()

    override suspend fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            val currentIntentions = _intentionsFlow.value.toMutableList()
            currentIntentions.add(0, intention)
            _intentionsFlow.value = currentIntentions
        }
    }
}
