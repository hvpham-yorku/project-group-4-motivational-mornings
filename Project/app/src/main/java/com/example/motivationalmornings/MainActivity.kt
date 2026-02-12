package com.example.motivationalmornings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
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
fun MotivationalMorningsApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

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
                    onClick = { currentDestination = it }
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
                AppDestinations.DAILY_CONTENT -> DailyContent(modifier = Modifier.padding(innerPadding))
                AppDestinations.AGGREGATOR -> AggregatorScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.RSS_FEED -> RssFeedScreen(modifier = Modifier.padding(innerPadding))
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
    AGGREGATOR("Aggregator", Icons.Default.List),
    RSS_FEED("RSS Feed", Icons.Default.Favorite),
}

@Composable
fun DailyContent(modifier: Modifier = Modifier) {
    Text(
        text = "Daily Content Screen",
        modifier = modifier
    )
}

@Composable
fun AggregatorScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Aggregator Screen",
        modifier = modifier
    )
}

@Composable
fun RssFeedScreen(modifier: Modifier = Modifier) {
    Text(
        text = "RSS Feed Screen",
        modifier = modifier
    )
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
