package com.example.motivationalmornings

import com.example.motivationalmornings.Presentation.AppDestinations
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.WbSunny
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {

    @Test
    fun appDestinations_hasCorrectNumberOfEntries() {
        // Given: AppDestinations enum
        // When: Getting all entries
        val entries = AppDestinations.entries

        // Then: Should have exactly 5 destinations
        assertEquals(5, entries.size)
    }

    @Test
    fun dashboard_hasCorrectLabel() {
        // Given: DASHBOARD destination
        // When: Getting label
        val label = AppDestinations.DASHBOARD.label

        // Then: Label should be "Dashboard"
        assertEquals("Dashboard", label)
    }

    @Test
    fun dashboard_hasCorrectIcon() {
        // Given: DASHBOARD destination
        // When: Getting icon
        val icon = AppDestinations.DASHBOARD.icon

        // Then: Icon should be BarChart icon
        assertEquals(Icons.Default.BarChart, icon)
    }

    @Test
    fun dailyContent_hasCorrectLabel() {
        // Given: DAILY_CONTENT destination
        // When: Getting label
        val label = AppDestinations.DAILY_CONTENT.label

        // Then: Label should be "Daily"
        assertEquals("Daily", label)
    }

    @Test
    fun dailyContent_hasCorrectIcon() {
        // Given: DAILY_CONTENT destination
        // When: Getting icon
        val icon = AppDestinations.DAILY_CONTENT.icon

        // Then: Icon should be WbSunny icon
        assertEquals(Icons.Default.WbSunny, icon)
    }

    @Test
    fun aggregator_hasCorrectLabel() {
        // Given: AGGREGATOR destination
        // When: Getting label
        val label = AppDestinations.AGGREGATOR.label

        // Then: Label should be "Aggregator"
        assertEquals("Aggregator", label)
    }

    @Test
    fun aggregator_hasCorrectIcon() {
        // Given: AGGREGATOR destination
        // When: Getting icon
        val icon = AppDestinations.AGGREGATOR.icon

        // Then: Icon should be Newspaper icon
        assertEquals(Icons.Default.Newspaper, icon)
    }

    @Test
    fun rssFeed_hasCorrectLabel() {
        // Given: RSS_FEED destination
        // When: Getting label
        val label = AppDestinations.RSS_FEED.label

        // Then: Label should be "RSS"
        assertEquals("RSS", label)
    }

    @Test
    fun rssFeed_hasCorrectIcon() {
        // Given: RSS_FEED destination
        // When: Getting icon
        val icon = AppDestinations.RSS_FEED.icon

        // Then: Icon should be RssFeed icon
        assertEquals(Icons.Default.RssFeed, icon)
    }

    @Test
    fun weather_hasCorrectLabel() {
        // Given: WEATHER destination
        // When: Getting label
        val label = AppDestinations.WEATHER.label

        // Then: Label should be "Weather"
        assertEquals("Weather", label)
    }

    @Test
    fun weather_hasCorrectIcon() {
        // Given: WEATHER destination
        // When: Getting icon
        val icon = AppDestinations.WEATHER.icon

        // Then: Icon should be Cloud icon
        assertEquals(Icons.Default.Cloud, icon)
    }

    @Test
    fun allDestinations_haveUniqueLabels() {
        // Given: All app destinations
        val labels = AppDestinations.entries.map { it.label }

        // Then: All labels should be unique
        assertEquals(labels.size, labels.toSet().size)
    }

    @Test
    fun allDestinations_haveNonEmptyLabels() {
        // Given: All app destinations
        // When: Checking all labels
        val allLabelsValid = AppDestinations.entries.all { it.label.isNotBlank() }

        // Then: All labels should be non-empty
        assertEquals(true, allLabelsValid)
    }

    @Test
    fun valueOf_findsDashboard() {
        // Given: String "DASHBOARD"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("DASHBOARD")

        // Then: Should return DASHBOARD destination
        assertEquals(AppDestinations.DASHBOARD, destination)
    }

    @Test
    fun valueOf_findsDailyContent() {
        // Given: String "DAILY_CONTENT"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("DAILY_CONTENT")

        // Then: Should return DAILY_CONTENT destination
        assertEquals(AppDestinations.DAILY_CONTENT, destination)
    }

    @Test
    fun valueOf_findsAggregator() {
        // Given: String "AGGREGATOR"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("AGGREGATOR")

        // Then: Should return AGGREGATOR destination
        assertEquals(AppDestinations.AGGREGATOR, destination)
    }

    @Test
    fun valueOf_findsRssFeed() {
        // Given: String "RSS_FEED"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("RSS_FEED")

        // Then: Should return RSS_FEED destination
        assertEquals(AppDestinations.RSS_FEED, destination)
    }

    @Test
    fun valueOf_findsWeather() {
        // Given: String "WEATHER"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("WEATHER")

        // Then: Should return WEATHER destination
        assertEquals(AppDestinations.WEATHER, destination)
    }

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_throwsExceptionForInvalidName() {
        // Given: Invalid destination name
        // When: Trying to get enum by invalid name
        // Then: Should throw IllegalArgumentException
        AppDestinations.valueOf("INVALID_DESTINATION")
    }
}
