package com.example.motivationalmornings

import android.content.Context
import com.example.motivationalmornings.BusinessLogic.AggregatorViewModel
import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AggregatorArticle
import com.example.motivationalmornings.Persistence.FakeAnalyticsRepository
import com.example.motivationalmornings.Persistence.HardcodedContentRepository
import com.example.motivationalmornings.Persistence.weather.WeatherInfo
import com.example.motivationalmornings.Persistence.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Automated counterparts to Activity 2 manual tests in
 * [Documentation/EECS2311-Lab5-JordiNakahara-TakeHomeAssignment.md] (Stories 2 and 5).
 *
 * Story 1, 3, 4, 6 are covered in other test classes; see the doc's "Automated coverage" section.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Lab5Activity2AutomatedCoverageTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dailyContentViewModelWithWeather(
        weatherRepository: WeatherRepository,
    ): DailyContentViewModel {
        val appContext = mock(Context::class.java)
        `when`(appContext.applicationContext).thenReturn(appContext)
        return DailyContentViewModel(
            HardcodedContentRepository(),
            Analytics(FakeAnalyticsRepository()),
            appContext,
            weatherRepository = weatherRepository,
            refreshWidgets = {},
        )
    }

    // --- Story 2 — Weather (manual T2.1, T2.3, T2.4) ---

    @Test
    fun t2_1_citySearch_populatesWeatherState() = runTest {
        val expected = WeatherInfo(temperatureC = 12.0, windSpeedKmh = 15.0, condition = "Cloudy")
        val vm = dailyContentViewModelWithWeather(
            object : WeatherRepository {
                override suspend fun getCurrentWeather(city: String) = expected
            },
        )
        vm.setCity("Toronto")
        vm.loadWeather()
        advanceUntilIdle()
        assertEquals(expected, vm.weather.value)
        assertNull(vm.error.value)
    }

    @Test
    fun t2_3_blankCity_showsEnterCityNameError() = runTest {
        val vm = dailyContentViewModelWithWeather(
            object : WeatherRepository {
                override suspend fun getCurrentWeather(city: String) =
                    WeatherInfo(0.0, 0.0, "Clear")
            },
        )
        vm.setCity("   ")
        vm.loadWeather()
        assertEquals("Enter a city name", vm.error.value)
    }

    @Test
    fun t2_4_networkFailure_showsFailedToLoadWeather() = runTest {
        val vm = dailyContentViewModelWithWeather(
            object : WeatherRepository {
                override suspend fun getCurrentWeather(city: String): WeatherInfo {
                    error("simulated offline")
                }
            },
        )
        vm.setCity("Toronto")
        vm.loadWeather()
        advanceUntilIdle()
        assertEquals("Failed to load weather", vm.error.value)
    }

    // --- Story 5 — Aggregator keyword filter (manual T5.1–T5.4) ---

    @Test
    fun t5_1_filter_keepsOnlyArticlesMatchingTitleOrUrl() = runTest {
        val vm = AggregatorViewModel(dailyContentDao = null)
        val articles = listOf(
            AggregatorArticle("BBC World News headline", "https://bbc.com/world"),
            AggregatorArticle("Sports roundup", "https://espn.com/sports"),
        )
        val filtered = vm.filterArticles(articles, "world")
        assertEquals(1, filtered.size)
        assertEquals("BBC World News headline", filtered[0].title)
    }

    @Test
    fun t5_2_clearFilter_returnsFullList() = runTest {
        val vm = AggregatorViewModel(dailyContentDao = null)
        val articles = listOf(
            AggregatorArticle("Alpha story", "https://example.com/one"),
            AggregatorArticle("Beta story", "https://example.com/two"),
        )
        val narrowed = vm.filterArticles(articles, "alpha")
        assertEquals(1, narrowed.size)
        assertEquals(articles, vm.filterArticles(articles, ""))
        assertEquals(articles, vm.filterArticles(articles, "  ,  , "))
    }

    @Test
    fun t5_3_noMatches_returnsEmptyList() = runTest {
        val vm = AggregatorViewModel(dailyContentDao = null)
        val articles = listOf(
            AggregatorArticle("Morning news", "https://news.com/a"),
        )
        val keywordText = "zzzyyyxxx"
        val filtered = vm.filterArticles(articles, keywordText)
        assertTrue(filtered.isEmpty())
        val hasActiveKeywords = keywordText.split(",").map { it.trim() }.any { it.isNotBlank() }
        val showNoKeywordMatches = filtered.isEmpty() && articles.isNotEmpty() && hasActiveKeywords
        assertTrue(showNoKeywordMatches)
    }

    @Test
    fun t5_4_specialCharacters_noCrash_emptyOrMatchingSubstring() = runTest {
        val vm = AggregatorViewModel(dailyContentDao = null)
        val articles = listOf(
            AggregatorArticle("Price @ market", "https://example.com/p/1"),
        )
        val noMatch = vm.filterArticles(articles, "@#$%")
        assertTrue(noMatch.isEmpty())
        val matchAt = vm.filterArticles(articles, "@")
        assertEquals(1, matchAt.size)
    }
}
