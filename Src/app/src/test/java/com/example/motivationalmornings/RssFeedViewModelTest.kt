package com.example.motivationalmornings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RssRepositoryTest {

    private lateinit var repository: RssRepository

    @Before
    fun setup() {
        repository = RssRepository()
    }

    @Test
    fun getRssItems_returnsThreeItems() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: Should return 3 hardcoded items
        assertEquals(3, items.size)
    }

    @Test
    fun getRssItems_firstItemHasCorrectData() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: First item should have correct properties
        val firstItem = items[0]
        assertEquals(1, firstItem.id)
        assertEquals("Morning Motivation", firstItem.title)
        assertEquals("Start your day with a positive quote.", firstItem.description)
        assertEquals("https://example.com/morning-motivation", firstItem.link)
    }

    @Test
    fun getRssItems_secondItemHasCorrectData() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: Second item should have correct properties
        val secondItem = items[1]
        assertEquals(2, secondItem.id)
        assertEquals("Mindfulness Minute", secondItem.title)
        assertEquals("A short mindfulness exercise for your commute.", secondItem.description)
        assertEquals("https://example.com/mindfulness-minute", secondItem.link)
    }

    @Test
    fun getRssItems_thirdItemHasCorrectData() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: Third item should have correct properties
        val thirdItem = items[2]
        assertEquals(3, thirdItem.id)
        assertEquals("Gratitude Check", thirdItem.title)
        assertEquals("Three things to be grateful for today.", thirdItem.description)
        assertEquals("https://example.com/gratitude-check", thirdItem.link)
    }

    @Test
    fun getRssItems_allItemsHaveUniqueIds() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: All IDs should be unique
        val ids = items.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun getRssItems_noItemsHaveEmptyTitles() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: No items should have empty titles
        assertTrue(items.all { it.title.isNotBlank() })
    }

    @Test
    fun getRssItems_noItemsHaveEmptyLinks() {
        // Given: Repository is initialized
        // When: Getting RSS items
        val items = repository.getRssItems()

        // Then: No items should have empty links
        assertTrue(items.all { it.link.isNotBlank() })
    }
}

@ExperimentalCoroutinesApi
class RssFeedViewModelTest {

    private lateinit var repository: RssRepository
    private lateinit var viewModel: RssFeedViewModel

    @Before
    fun setup() {
        repository = RssRepository()
        viewModel = RssFeedViewModel(repository)
    }

    @Test
    fun initialRssItems_loadsFromRepository() = runTest {
        // Given: ViewModel is initialized
        // When: Getting RSS items
        val items = viewModel.rssItems.first()

        // Then: Should load 3 items from repository
        assertEquals(3, items.size)
    }

    @Test
    fun rssItems_containsExpectedFirstItem() = runTest {
        // Given: ViewModel is initialized
        // When: Getting RSS items
        val items = viewModel.rssItems.value

        // Then: First item should match repository data
        val firstItem = items[0]
        assertEquals("Morning Motivation", firstItem.title)
        assertEquals("Start your day with a positive quote.", firstItem.description)
    }

    @Test
    fun rssItems_containsExpectedSecondItem() = runTest {
        // Given: ViewModel is initialized
        // When: Getting RSS items
        val items = viewModel.rssItems.value

        // Then: Second item should match repository data
        val secondItem = items[1]
        assertEquals("Mindfulness Minute", secondItem.title)
        assertEquals("A short mindfulness exercise for your commute.", secondItem.description)
    }

    @Test
    fun rssItems_containsExpectedThirdItem() = runTest {
        // Given: ViewModel is initialized
        // When: Getting RSS items
        val items = viewModel.rssItems.value

        // Then: Third item should match repository data
        val thirdItem = items[2]
        assertEquals("Gratitude Check", thirdItem.title)
        assertEquals("Three things to be grateful for today.", thirdItem.description)
    }

    @Test
    fun rssItems_allHaveValidUrls() = runTest {
        // Given: ViewModel is initialized
        // When: Getting RSS items
        val items = viewModel.rssItems.value

        // Then: All items should have valid-looking URLs
        assertTrue(items.all { it.link.startsWith("https://") })
    }

    @Test
    fun rssItems_maintainOrderFromRepository() = runTest {
        // Given: ViewModel is initialized
        val repoItems = repository.getRssItems()

        // When: Getting RSS items from ViewModel
        val vmItems = viewModel.rssItems.value

        // Then: Order should match repository
        assertEquals(repoItems[0].id, vmItems[0].id)
        assertEquals(repoItems[1].id, vmItems[1].id)
        assertEquals(repoItems[2].id, vmItems[2].id)
    }
}

class RssItemTest {

    @Test
    fun rssItem_createsWithAllFields() {
        // Given: Creating an RSS item
        val item = RssItem(
            id = 1,
            title = "Test Title",
            description = "Test Description",
            link = "https://test.com"
        )

        // Then: All fields should be set correctly
        assertEquals(1, item.id)
        assertEquals("Test Title", item.title)
        assertEquals("Test Description", item.description)
        assertEquals("https://test.com", item.link)
    }

    @Test
    fun rssItem_supportsEmptyDescription() {
        // Given: Creating an RSS item with empty description
        val item = RssItem(
            id = 1,
            title = "Test",
            description = "",
            link = "https://test.com"
        )

        // Then: Empty description should be allowed
        assertEquals("", item.description)
    }

    @Test
    fun rssItem_equality() {
        // Given: Two identical RSS items
        val item1 = RssItem(1, "Title", "Desc", "Link")
        val item2 = RssItem(1, "Title", "Desc", "Link")

        // Then: They should be equal (data class equality)
        assertEquals(item1, item2)
    }

    @Test
    fun rssItem_inequality_differentId() {
        // Given: Two RSS items with different IDs
        val item1 = RssItem(1, "Title", "Desc", "Link")
        val item2 = RssItem(2, "Title", "Desc", "Link")

        // Then: They should not be equal
        assertTrue(item1 != item2)
    }

    @Test
    fun rssItem_copy_works() {
        // Given: An RSS item
        val original = RssItem(1, "Title", "Desc", "Link")

        // When: Copying with modified title
        val copied = original.copy(title = "New Title")

        // Then: Only title should change
        assertEquals(1, copied.id)
        assertEquals("New Title", copied.title)
        assertEquals("Desc", copied.description)
        assertEquals("Link", copied.link)
    }
}