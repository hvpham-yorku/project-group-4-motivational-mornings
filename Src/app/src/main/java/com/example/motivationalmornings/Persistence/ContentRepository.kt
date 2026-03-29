package com.example.motivationalmornings.Persistence

//package com.example.motivationalmornings.data
//
//import com.example.motivationalmornings.Persistence.DailyContentDao
//import com.example.motivationalmornings.Persistence.Intention
//import com.example.motivationalmornings.Persistence.QuoteOfTheDay
//import com.example.motivationalmornings.R
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.flow.map
//import java.time.LocalDate
//
//interface ContentRepository {
//    fun getQuote(): Flow<String>
//    fun getImageResId(): Flow<Int>
//    fun getIntentions(): Flow<List<String>>
//    fun getAllIntentions(): Flow<List<Intention>>
//    suspend fun saveIntention(intention: String)
//    suspend fun updateReflection(uid: Int, reflection: String)
//    suspend fun saveQuote(quote: String)
//    fun getAllQuotes(): Flow<List<QuoteOfTheDay>>
//    suspend fun deleteQuote(quote: QuoteOfTheDay)
//}
//
//class RoomContentRepository(
//    private val dailyContentDao: DailyContentDao
//) : ContentRepository {
//
//    override fun getQuote(): Flow<String> =
//        dailyContentDao.getAllQuotes().map { quotes ->
//            if (quotes.isEmpty()) {
//                "The best way to predict the future is to create it."
//            } else {
//                val sortedQuotes = quotes.sortedBy { it.uid }
//                val dayIndex = (LocalDate.now().toEpochDay() % sortedQuotes.size).toInt()
//                sortedQuotes[dayIndex].text
//            }
//        }
//
//    override fun getImageResId(): Flow<Int> {
//        val images = listOf(
//            R.drawable.imageotd,
//            R.drawable.imageotd2,
//            R.drawable.imageotd3,
//            R.drawable.imageotd4,
//            R.drawable.imageotd5,
//            R.drawable.imageotd6
//        )
//
//        val dayIndex = (LocalDate.now().toEpochDay() % images.size).toInt()
//        return flowOf(images[dayIndex])
//    }
//
//    override fun getIntentions(): Flow<List<String>> =
//        dailyContentDao.getIntentionsByDate(LocalDate.now().toString())
//
//    override fun getAllIntentions(): Flow<List<Intention>> =
//        dailyContentDao.getAllIntentions()
//
//    override suspend fun saveIntention(intention: String) {
//        if (intention.isNotBlank()) {
//            dailyContentDao.insertIntention(
//                Intention(text = intention, date = LocalDate.now().toString())
//            )
//        }
//    }
//
//    override suspend fun updateReflection(uid: Int, reflection: String) {
//        dailyContentDao.updateReflection(uid, reflection)
//    }
//
//    override suspend fun saveQuote(quote: String) {
//        if (quote.isNotBlank()) {
//            dailyContentDao.insertQuote(QuoteOfTheDay(text = quote))
//        }
//    }
//
//    override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> =
//        dailyContentDao.getAllQuotes()
//
//    override suspend fun deleteQuote(quote: QuoteOfTheDay) {
//        dailyContentDao.deleteQuote(quote)
//    }
//}
//
///** Default content shared with Room DB (ITR2: same content in stub and database). */
//private val DEFAULT_QUOTES = listOf(
//    "The best way to predict the future is to create it.",
//    "Every morning is a new opportunity to become a better version of yourself.",
//    "Small steps every day lead to big changes.",
//    "You are capable of amazing things today.",
//    "Start where you are. Use what you have. Do what you can."
//)
//
//class HardcodedContentRepository : ContentRepository {
//    private val _intentionsFlow = MutableStateFlow<List<Intention>>(emptyList())
//    private val _quotesFlow = MutableStateFlow<List<QuoteOfTheDay>>(
//        DEFAULT_QUOTES.mapIndexed { index, text -> QuoteOfTheDay(uid = index + 1, text = text) }
//    )
//
//    override fun getQuote(): Flow<String> = _quotesFlow.map { quotes ->
//        if (quotes.isEmpty()) {
//            DEFAULT_QUOTES.first()
//        } else {
//            val sortedQuotes = quotes.sortedBy { it.uid }
//            val dayIndex = (LocalDate.now().toEpochDay() % sortedQuotes.size).toInt()
//            sortedQuotes[dayIndex].text
//        }
//    }
//
//    override fun getImageResId(): Flow<Int> {
//        val images = listOf(
//            R.drawable.imageotd,
//            R.drawable.imageotd2,
//            R.drawable.imageotd3,
//            R.drawable.imageotd4,
//            R.drawable.imageotd5,
//            R.drawable.imageotd6
//        )
//
//        val dayIndex = (LocalDate.now().toEpochDay() % images.size).toInt()
//        return flowOf(images[dayIndex])
//    }
//
//    override fun getIntentions(): Flow<List<String>> = _intentionsFlow.map { intentions ->
//        intentions.filter { it.date == LocalDate.now().toString() }.map { it.text }
//    }
//
//    override fun getAllIntentions(): Flow<List<Intention>> = _intentionsFlow.asStateFlow()
//
//    override suspend fun saveIntention(intention: String) {
//        if (intention.isNotBlank()) {
//            val currentIntentions = _intentionsFlow.value.toMutableList()
//            currentIntentions.add(0, Intention(text = intention, date = LocalDate.now().toString()))
//            _intentionsFlow.value = currentIntentions
//        }
//    }
//
//    override suspend fun updateReflection(uid: Int, reflection: String) {
//        val currentIntentions = _intentionsFlow.value.toMutableList()
//        val index = currentIntentions.indexOfFirst { it.uid == uid }
//        if (index != -1) {
//            currentIntentions[index] = currentIntentions[index].copy(reflection = reflection)
//            _intentionsFlow.value = currentIntentions
//        }
//    }
//
//    override suspend fun saveQuote(quote: String) {
//        if (quote.isNotBlank()) {
//            val currentQuotes = _quotesFlow.value.toMutableList()
//            val nextId = (currentQuotes.maxOfOrNull { it.uid } ?: 0) + 1
//            currentQuotes.add(0, QuoteOfTheDay(uid = nextId, text = quote))
//            _quotesFlow.value = currentQuotes
//        }
//    }
//
//    override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> = _quotesFlow.asStateFlow()
//
//    override suspend fun deleteQuote(quote: QuoteOfTheDay) {
//        val currentQuotes = _quotesFlow.value.toMutableList()
//        currentQuotes.removeAll { it.uid == quote.uid }
//        _quotesFlow.value = currentQuotes
//    }
//}

import com.example.motivationalmornings.Persistence.DailyContentDao
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

//new imports
import com.example.motivationalmornings.Persistence.QuoteFeedback
import com.example.motivationalmornings.Persistence.ImageFeedback

import kotlinx.coroutines.flow.map
// ─── Interface ────────────────────────────────────────────────────────────────

interface ContentRepository {
    fun getQuote(): Flow<String>
    fun getQuoteOfTheDay(): Flow<QuoteOfTheDay?>


    /**
     * Returns the image to display today as an [ImageOfTheDay].
     * The selection is deterministic for a given calendar day (seeded by epoch day)
     * so the image doesn't change while the app is open.
     */
    fun getImageOfTheDay(): Flow<ImageOfTheDay?>

    fun getIntentions(): Flow<List<String>>
    fun getAllIntentions(): Flow<List<Intention>>
    suspend fun saveIntention(intention: String, weather: String? = null)
    suspend fun updateReflection(uid: Int, reflection: String)
    suspend fun saveQuote(quote: String)
    fun getAllQuotes(): Flow<List<QuoteOfTheDay>>
    suspend fun deleteQuote(quote: QuoteOfTheDay)

    // Image management
    fun getAllImages(): Flow<List<ImageOfTheDay>>
    suspend fun addImage(image: ImageOfTheDay)
    suspend fun deleteImage(image: ImageOfTheDay)

    // new methods
    suspend fun recordQuoteReaction(quoteId: Int, reaction: String)
    suspend fun recordImageReaction(imageId: Int, reaction: String)

}

// ─── Room-backed implementation ───────────────────────────────────────────────

class RoomContentRepository(
    private val dailyContentDao: DailyContentDao
) : ContentRepository {

//    override fun getQuote(): Flow<String> =
//        dailyContentDao.getAllQuotes().map { quotes ->
//            if (quotes.isEmpty()) {
//                "The best way to predict the future is to create it."
//            } else {
//                val sorted = quotes.sortedBy { it.uid }
//                val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
//                sorted[idx].text
//            }
//        }

    override fun getQuote(): Flow<String> = getQuoteOfTheDay().map { it?.text ?: "" }

    override fun getQuoteOfTheDay(): Flow<QuoteOfTheDay?> =
        dailyContentDao.getAllQuotes().map { quotes ->
            if (quotes.isEmpty()) null
            else {
                val sorted = quotes.sortedBy { it.uid }
                val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
                sorted[idx]
            }
        }

    /**
     * Picks one image per day by seeding the index with the current epoch day.
     * Returns null only if the pool is completely empty.
     */
    override fun getImageOfTheDay(): Flow<ImageOfTheDay?> =
        dailyContentDao.getAllImages().map { images ->
            if (images.isEmpty()) null
            else {
                val sorted = images.sortedBy { it.uid }
                val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
                sorted[idx]
            }
        }

    override fun getIntentions(): Flow<List<String>> =
        dailyContentDao.getIntentionsByDate(LocalDate.now().toString())

    override fun getAllIntentions(): Flow<List<Intention>> =
        dailyContentDao.getAllIntentions()

    override suspend fun saveIntention(intention: String, weather: String?) {
        if (intention.isNotBlank()) {
            val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            dailyContentDao.insertIntention(
                Intention(
                    text = intention,
                    date = LocalDate.now().toString(),
                    weather = weather,
                    time = currentTime
                )
            )
        }
    }

    override suspend fun updateReflection(uid: Int, reflection: String) {
        dailyContentDao.updateReflection(uid, reflection)
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

    override fun getAllImages(): Flow<List<ImageOfTheDay>> =
        dailyContentDao.getAllImages()

    override suspend fun addImage(image: ImageOfTheDay) {
        dailyContentDao.insertImage(image)
    }

    override suspend fun deleteImage(image: ImageOfTheDay) {
        dailyContentDao.deleteImage(image)
    }

    //new methods
    override suspend fun recordQuoteReaction(quoteId: Int, reaction: String) {
        dailyContentDao.insertQuoteFeedback(
            QuoteFeedback(
                quoteId = quoteId,
                reaction = reaction,
                createdAt = java.time.LocalDateTime.now().toString()
            )
        )
    }

    override suspend fun recordImageReaction(imageId: Int, reaction: String) {
        dailyContentDao.insertImageFeedback(
            ImageFeedback(
                imageId = imageId,
                reaction = reaction,
                createdAt = java.time.LocalDateTime.now().toString()
            )
        )
    }
}

// ─── Hardcoded stub (for unit tests) ─────────────────────────────────────────

private val DEFAULT_QUOTES = listOf(
    "The best way to predict the future is to create it.",
    "Every morning is a new opportunity to become a better version of yourself.",
    "Small steps every day lead to big changes.",
    "You are capable of amazing things today.",
    "Start where you are. Use what you have. Do what you can."
)

private val DEFAULT_DRAWABLE_IMAGES = listOf(
    R.drawable.imageotd,
    R.drawable.imageotd2,
    R.drawable.imageotd3,
    R.drawable.imageotd4,
    R.drawable.imageotd5,
    R.drawable.imageotd6,
).mapIndexed { idx, resId -> ImageOfTheDay(uid = idx + 1, drawableResId = resId) }

class HardcodedContentRepository : ContentRepository {

    private val _intentionsFlow = MutableStateFlow<List<Intention>>(emptyList())
    private val _quotesFlow = MutableStateFlow(
        DEFAULT_QUOTES.mapIndexed { idx, text -> QuoteOfTheDay(uid = idx + 1, text = text) }
    )
    private val _imagesFlow = MutableStateFlow(DEFAULT_DRAWABLE_IMAGES)

//    override fun getQuote(): Flow<String> = _quotesFlow.map { quotes ->
//        if (quotes.isEmpty()) DEFAULT_QUOTES.first()
//        else {
//            val sorted = quotes.sortedBy { it.uid }
//            val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
//            sorted[idx].text
//        }
//    }

    override fun getQuoteOfTheDay(): Flow<QuoteOfTheDay?> =
        _quotesFlow.map { quotes ->
            if (quotes.isEmpty()) {
                null
            } else {
                val sorted = quotes.sortedBy { it.uid }
                val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
                sorted[idx]
            }
        }

    override fun getQuote(): Flow<String> =
        getQuoteOfTheDay().map { it?.text ?: "" }



    override fun getImageOfTheDay(): Flow<ImageOfTheDay?> = _imagesFlow.map { images ->
        if (images.isEmpty()) null
        else {
            val sorted = images.sortedBy { it.uid }
            val idx = (LocalDate.now().toEpochDay() % sorted.size).toInt()
            sorted[idx]
        }
    }

    override fun getIntentions(): Flow<List<String>> = _intentionsFlow.map { list ->
        list.filter { it.date == LocalDate.now().toString() }.map { it.text }
    }

    override fun getAllIntentions(): Flow<List<Intention>> = _intentionsFlow.asStateFlow()

    override suspend fun saveIntention(intention: String, weather: String?) {
        if (intention.isNotBlank()) {
            val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val currentIntentions = _intentionsFlow.value.toMutableList()
            currentIntentions.add(
                0,
                Intention(
                    text = intention,
                    date = LocalDate.now().toString(),
                    weather = weather,
                    time = currentTime
                )
            )
            _intentionsFlow.value = currentIntentions
        }
    }

    override suspend fun updateReflection(uid: Int, reflection: String) {
        val updated = _intentionsFlow.value.toMutableList()
        val idx = updated.indexOfFirst { it.uid == uid }
        if (idx != -1) updated[idx] = updated[idx].copy(reflection = reflection)
        _intentionsFlow.value = updated
    }

    override suspend fun saveQuote(quote: String) {
        if (quote.isNotBlank()) {
            val updated = _quotesFlow.value.toMutableList()
            val nextId = (updated.maxOfOrNull { it.uid } ?: 0) + 1
            updated.add(0, QuoteOfTheDay(uid = nextId, text = quote))
            _quotesFlow.value = updated
        }
    }

    override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> = _quotesFlow.asStateFlow()

    override suspend fun deleteQuote(quote: QuoteOfTheDay) {
        _quotesFlow.value = _quotesFlow.value.filterNot { it.uid == quote.uid }
    }

    override fun getAllImages(): Flow<List<ImageOfTheDay>> = _imagesFlow.asStateFlow()

    override suspend fun addImage(image: ImageOfTheDay) {
        val updated = _imagesFlow.value.toMutableList()
        val nextId = (updated.maxOfOrNull { it.uid } ?: 0) + 1
        updated.add(0, image.copy(uid = nextId))
        _imagesFlow.value = updated
    }

    override suspend fun deleteImage(image: ImageOfTheDay) {
        _imagesFlow.value = _imagesFlow.value.filterNot { it.uid == image.uid }
    }

    override suspend fun recordQuoteReaction(quoteId: Int, reaction: String) { }

    override suspend fun recordImageReaction(imageId: Int, reaction: String) { }
}