package com.example.motivationalmornings.Persistence

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

open class RssRepository {

    open fun getRssItems(feedUrl: String): List<RssItem> {
        val trimmedUrl = feedUrl.trim()
        if (trimmedUrl.isEmpty()) return emptyList()

        return try {
            val url = URL(trimmedUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                doInput = true
            }

            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                emptyList()
            } else {
                connection.inputStream.use { stream ->
                    val items = parseRss(stream)
                    connection.disconnect()
                    items
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRss(input: InputStream): List<RssItem> {
        val items = mutableListOf<RssItem>()

        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, null)
        }

        var eventType = parser.eventType
        var insideItem = false
        var currentTitle: String? = null
        var currentDescription: String? = null
        var currentLink: String? = null
        var idCounter = 1

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name.equals("item", ignoreCase = true) ||
                        name.equals("entry", ignoreCase = true)
                    ) {
                        insideItem = true
                        currentTitle = null
                        currentDescription = null
                        currentLink = null
                    } else if (insideItem) {
                        when {
                            name.equals("title", ignoreCase = true) -> {
                                currentTitle = parser.nextText().orEmpty()
                            }

                            name.equals("description", ignoreCase = true) ||
                                    name.equals("summary", ignoreCase = true) -> {
                                currentDescription = parser.nextText().orEmpty()
                            }

                            name.equals("link", ignoreCase = true) -> {
                                // RSS: text content, Atom: href attribute
                                val href = parser.getAttributeValue(null, "href")
                                currentLink = href ?: parser.nextText().orEmpty()
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (name.equals("item", ignoreCase = true) ||
                        name.equals("entry", ignoreCase = true)
                    ) {
                        if (!currentTitle.isNullOrBlank() && !currentLink.isNullOrBlank()) {
                            items.add(
                                RssItem(
                                    id = idCounter++,
                                    title = currentTitle!!.trim(),
                                    description = currentDescription?.trim().orEmpty(),
                                    link = currentLink!!.trim()
                                )
                            )
                        }
                        insideItem = false
                    }
                }
            }

            eventType = parser.next()
        }

        return items
    }
}