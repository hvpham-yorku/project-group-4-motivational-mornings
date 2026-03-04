package com.example.motivationalmornings.BusinessLogic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class RssRepository(private val parser: RssParser = RssParser()) {
    suspend fun fetchFeed(url: String): List<RssItem> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.inputStream.use { inputStream ->
                parser.parse(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
