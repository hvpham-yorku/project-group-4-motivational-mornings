package com.example.motivationalmornings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.ui.theme.MotivationalMorningsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotivationalMorningsTheme {
                MotivationalMorningsApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MotivationalMorningsApp(viewModel: MainViewModel = viewModel()) {
    val currentDestination by viewModel.currentDestination.collectAsState()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { viewModel.setCurrentDestination(it) }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.DAILY_CONTENT -> DailyContent(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.AGGREGATOR -> AggregatorScreen(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.RSS_FEED -> RssFeedScreen(
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.WEATHER -> WeatherScreen(
                    modifier = Modifier.padding(innerPadding)
                )

            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    DAILY_CONTENT("Daily Content", Icons.Default.DateRange),
    AGGREGATOR("Aggregator", Icons.AutoMirrored.Filled.List),
    RSS_FEED("RSS Feed", Icons.Default.Favorite),

    WEATHER("Weather", Icons.Default.Favorite),

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MotivationalMorningsTheme {
        Greeting("Android")
    }
}
