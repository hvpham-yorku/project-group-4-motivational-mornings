package com.example.motivationalmornings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.motivationalmornings.data.ContentRepository
import com.example.motivationalmornings.ui.theme.MotivationalMorningsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComposeScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun greeting_displaysHelloMessage() {
        composeRule.setContent { Greeting("Jordi") }

        composeRule.onNodeWithText("Hello Jordi!").assertIsDisplayed()
    }

    @Test
    fun aggregatorScreen_displaysDefaultText() {
        composeRule.setContent { AggregatorScreen() }

        composeRule.onNodeWithText("Aggregator Screen").assertIsDisplayed()
    }

    @Test
    fun rssFeedScreen_displaysDefaultText() {
        composeRule.setContent { RssFeedScreen() }

        composeRule.onNodeWithText("RSS Feed Screen").assertIsDisplayed()
    }

    @Test
    fun dailyContent_showsQuote_and_submitEnablesWhenTextNotBlank() {
        val intentions = MutableStateFlow("")
        val repo = object : ContentRepository {
            override fun getQuote(): Flow<String> = flowOf("A test quote.")
            override fun getImageResId(): Flow<Int> = flowOf(R.drawable.ic_launcher_background)
            override fun getIntentions(): Flow<String> = intentions
            override suspend fun saveIntention(intention: String) {
                intentions.value = intention
            }
        }
        val vm = DailyContentViewModel(repo)

        composeRule.setContent { DailyContent(viewModel = vm) }

        composeRule.onNodeWithText("Quote of the day").assertIsDisplayed()
        composeRule.onNodeWithText("A test quote.").assertIsDisplayed()

        composeRule.onNodeWithText("Submit").assertIsNotEnabled()
        // OutlinedTextField has no explicit label; use the current value to target the node.
        composeRule.onNodeWithText("").performTextInput("My intention")
        composeRule.onNodeWithText("Submit").assertIsEnabled()

        composeRule.onNodeWithText("Submit").performClick()
        composeRule.waitForIdle()

        assertEquals("My intention", intentions.value)
    }

    @Test
    fun app_scaffold_navigatesToRssFeed() {
        composeRule.setContent {
            MotivationalMorningsTheme(dynamicColor = false) {
                MotivationalMorningsApp(viewModel = MainViewModel())
            }
        }

        composeRule.onNodeWithText("RSS Feed").performClick()
        composeRule.onNodeWithText("RSS Feed Screen").assertIsDisplayed()
    }
}

