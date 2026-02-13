package com.example.motivationalmornings.data

import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

interface ContentRepository {
    fun getQuote(): Flow<String>
    fun getImageResId(): Flow<Int>
    fun getIntentions(): Flow<String>
    suspend fun saveIntention(intention: String)
}

class HardcodedContentRepository : ContentRepository {
    private val intentionsFlow = MutableStateFlow("My intentions for today are...")
    override fun getQuote(): Flow<String> = flowOf("The best way to predict the future is to create it.")
    override fun getImageResId(): Flow<Int> = flowOf(R.drawable.ic_launcher_background)
    override fun getIntentions(): Flow<String> = intentionsFlow
    override suspend fun saveIntention(intention: String) {
        intentionsFlow.value = intention
    }
}