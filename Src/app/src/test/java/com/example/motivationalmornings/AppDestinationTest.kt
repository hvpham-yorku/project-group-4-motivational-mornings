package com.example.motivationalmornings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationsTest {

    @Test
    fun appDestinations_hasCorrectNumberOfEntries() {
        // Given: AppDestinations enum
        // When: Getting all entries
        val entries = AppDestinations.entries

        // Then: Should have exactly 4 destinations
        assertEquals(4, entries.size)
    }

    @Test
    fun home_hasCorrectLabel() {
        // Given: HOME destination
        // When: Getting label
        val label = AppDestinations.HOME.label

        // Then: Label should be "Home"
        assertEquals("Home", label)
    }

    @Test
    fun home_hasCorrectIcon() {
        // Given: HOME destination
        // When: Getting icon
        val icon = AppDestinations.HOME.icon

        // Then: Icon should be Home icon
        assertEquals(Icons.Default.Home, icon)
    }

    @Test
    fun dailyContent_hasCorrectLabel() {
        // Given: DAILY_CONTENT destination
        // When: Getting label
        val label = AppDestinations.DAILY_CONTENT.label

        // Then: Label should be "Daily Content"
        assertEquals("Daily Content", label)
    }

    @Test
    fun dailyContent_hasCorrectIcon() {
        // Given: DAILY_CONTENT destination
        // When: Getting icon
        val icon = AppDestinations.DAILY_CONTENT.icon

        // Then: Icon should be DateRange icon
        assertEquals(Icons.Default.DateRange, icon)
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

        // Then: Icon should be List icon
        assertEquals(Icons.AutoMirrored.Filled.List, icon)
    }

    @Test
    fun rssFeed_hasCorrectLabel() {
        // Given: RSS_FEED destination
        // When: Getting label
        val label = AppDestinations.RSS_FEED.label

        // Then: Label should be "RSS Feed"
        assertEquals("RSS Feed", label)
    }

    @Test
    fun rssFeed_hasCorrectIcon() {
        // Given: RSS_FEED destination
        // When: Getting icon
        val icon = AppDestinations.RSS_FEED.icon

        // Then: Icon should be Favorite icon
        assertEquals(Icons.Default.Favorite, icon)
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
    fun valueOf_findsHome() {
        // Given: String "HOME"
        // When: Getting enum by name
        val destination = AppDestinations.valueOf("HOME")

        // Then: Should return HOME destination
        assertEquals(AppDestinations.HOME, destination)
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

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_throwsExceptionForInvalidName() {
        // Given: Invalid destination name
        // When: Trying to get enum by invalid name
        // Then: Should throw IllegalArgumentException
        AppDestinations.valueOf("INVALID_DESTINATION")
    }
}
