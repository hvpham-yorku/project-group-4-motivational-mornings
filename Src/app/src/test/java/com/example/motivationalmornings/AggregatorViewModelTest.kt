package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.AggregatorViewModel
import com.example.motivationalmornings.Persistence.AggregatorArticle
import com.example.motivationalmornings.Persistence.AggregatorWebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AggregatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isEmpty() = runTest {
        val vm = AggregatorViewModel(FakeAggregatorWebScraper(Result.success(emptyList())))
        assertEquals("", vm.sourceUrl.value)
        assertTrue(vm.articles.value.isEmpty())
        assertFalse(vm.isLoading.value)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun onSourceUrlChanged_updatesUrl() = runTest {
        val vm = AggregatorViewModel(FakeAggregatorWebScraper(Result.success(emptyList())))
        vm.onSourceUrlChanged(" https://example.com/world ")
        assertEquals(" https://example.com/world ", vm.sourceUrl.value)
    }

    @Test
    fun loadHeadlines_withEmptyUrl_setsError() = runTest {
        val vm = AggregatorViewModel(FakeAggregatorWebScraper(Result.success(emptyList())))
        vm.onSourceUrlChanged("   ")
        vm.loadHeadlines()
        advanceUntilIdle()
        assertTrue(vm.errorMessage.value!!.contains("Enter", ignoreCase = true))
        assertTrue(vm.articles.value.isEmpty())
    }

    @Test
    fun loadHeadlines_onSuccess_populatesArticles() = runTest {
        val articles = listOf(
            AggregatorArticle("A long enough headline here", "https://example.com/a"),
        )
        val vm = AggregatorViewModel(FakeAggregatorWebScraper(Result.success(articles)))
        vm.onSourceUrlChanged("https://example.com/world")
        vm.loadHeadlines()
        advanceUntilIdle()
        assertEquals(1, vm.articles.value.size)
        assertEquals("A long enough headline here", vm.articles.value[0].title)
        assertNull(vm.errorMessage.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun loadHeadlines_onSuccess_emptyList_setsMessage() = runTest {
        val vm = AggregatorViewModel(FakeAggregatorWebScraper(Result.success(emptyList())))
        vm.onSourceUrlChanged("https://example.com/world")
        vm.loadHeadlines()
        advanceUntilIdle()
        assertTrue(vm.articles.value.isEmpty())
        assertTrue(vm.errorMessage.value!!.contains("No headlines", ignoreCase = true))
    }

    @Test
    fun loadHeadlines_onFailure_clearsArticlesAndSetsError() = runTest {
        val vm = AggregatorViewModel(
            FakeAggregatorWebScraper(Result.failure(Exception("network problem"))),
        )
        vm.onSourceUrlChanged("https://example.com/world")
        vm.loadHeadlines()
        advanceUntilIdle()
        assertTrue(vm.articles.value.isEmpty())
        assertEquals("network problem", vm.errorMessage.value)
    }

    @Test
    fun onSourceUrlChanged_clearsPreviousErrorMessage() = runTest {
        val vm = AggregatorViewModel(
            FakeAggregatorWebScraper(Result.failure(Exception("network problem")))
        )

        vm.onSourceUrlChanged("https://example.com/world")
        vm.loadHeadlines()
        advanceUntilIdle()
        assertEquals("network problem", vm.errorMessage.value)

        vm.onSourceUrlChanged("https://example.com/updated")

        assertNull(vm.errorMessage.value)
        assertEquals("https://example.com/updated", vm.sourceUrl.value)
    }

}

private class FakeAggregatorWebScraper(
    private val result: Result<List<AggregatorArticle>>,
) : AggregatorWebScraper {
    override suspend fun scrapeHeadlines(pageUrl: String): Result<List<AggregatorArticle>> = result
}
