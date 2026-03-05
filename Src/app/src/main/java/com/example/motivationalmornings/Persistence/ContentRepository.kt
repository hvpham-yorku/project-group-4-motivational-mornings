package com.example.motivationalmornings.data

import com.example.motivationalmornings.Persistence.DailyContentDao
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface ContentRepository {
    fun getQuote(): Flow<String>
    fun getImageResId(): Flow<Int>
    fun getIntentions(): Flow<List<String>>
    suspend fun saveIntention(intention: String)
    suspend fun saveQuote(quote: String)
    fun getAllQuotes(): Flow<List<QuoteOfTheDay>>
    suspend fun deleteQuote(quote: QuoteOfTheDay)
}

class RoomContentRepository(
    private val dailyContentDao: DailyContentDao
) : ContentRepository {

    override fun getQuote(): Flow<String> =
        dailyContentDao.getRandomQuote().map { it ?: "The best way to predict the future is to create it." }

    override fun getImageResId(): Flow<Int> {
        val images = listOf(
            R.drawable.imageotd,
            R.drawable.imageotd2,
            R.drawable.imageotd3,
            R.drawable.imageotd4,
            R.drawable.imageotd5,
            R.drawable.imageotd6
        )

        val dayIndex = (LocalDate.now().toEpochDay() % images.size).toInt()
        return flowOf(images[dayIndex])
    }

    override fun getIntentions(): Flow<List<String>> =
        dailyContentDao.getIntentionsByDate(LocalDate.now().toString())

    override suspend fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            dailyContentDao.insertIntention(
                Intention(text = intention, date = LocalDate.now().toString())
            )
        }
    }

    override suspend fun saveQuote(quote: String) {
        if (quote.isNotBlank()) {
            dailyContentDao.insertQuote(QuoteOfTheDay(text = quote))
        }
    }

    override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> =
        dailyContentDao.getAllQuotes()

    override suspend fun deleteQuote(quote: QuoteOfTheDay) {
        dailyContentDao.deleteQuote(quote)
    }
}

class HardcodedContentRepository : ContentRepository {
    private val _intentionsFlow = MutableStateFlow<List<String>>(emptyList())
    private val _quoteFlow = MutableStateFlow("The best way to predict the future is to create it.")
    private val _quotesFlow = MutableStateFlow<List<QuoteOfTheDay>>(emptyList())

    override fun getQuote(): Flow<String> = _quoteFlow.asStateFlow()

    override fun getImageResId(): Flow<Int> {
        val images = listOf(
            R.drawable.imageotd,
            R.drawable.imageotd2,
            R.drawable.imageotd3,
            R.drawable.imageotd4,
            R.drawable.imageotd5,
            R.drawable.imageotd6
        )

        val dayIndex = (LocalDate.now().toEpochDay() % images.size).toInt()
        return flowOf(images[dayIndex])
    }

    override fun getIntentions(): Flow<List<String>> = _intentionsFlow.asStateFlow()

    override suspend fun saveIntention(intention: String) {
        if (intention.isNotBlank()) {
            val currentIntentions = _intentionsFlow.value.toMutableList()
            currentIntentions.add(0, intention)
            _intentionsFlow.value = currentIntentions
        }
    }

    override suspend fun saveQuote(quote: String) {
        if (quote.isNotBlank()) {
            _quoteFlow.value = quote
            val currentQuotes = _quotesFlow.value.toMutableList()
            val nextId = (currentQuotes.maxOfOrNull { it.uid } ?: 0) + 1
            currentQuotes.add(0, QuoteOfTheDay(uid = nextId, text = quote))
            _quotesFlow.value = currentQuotes
        }
    }

    override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> = _quotesFlow.asStateFlow()

    override suspend fun deleteQuote(quote: QuoteOfTheDay) {
        val currentQuotes = _quotesFlow.value.toMutableList()
        currentQuotes.removeAll { it.uid == quote.uid }
        _quotesFlow.value = currentQuotes
    }
}
