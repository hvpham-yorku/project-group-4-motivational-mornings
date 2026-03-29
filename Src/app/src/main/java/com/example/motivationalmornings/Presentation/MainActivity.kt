package com.example.motivationalmornings.Presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.MainViewModel
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

@Composable
fun MotivationalMorningsApp(viewModel: MainViewModel = viewModel()) {
    val currentDestination by viewModel.currentDestination.collectAsState()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = currentDestination == destination,
                    onClick = { viewModel.setCurrentDestination(destination) }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.DAILY_CONTENT -> DailyContent(Modifier.padding(innerPadding))
                AppDestinations.DASHBOARD -> AnalyticsDashboard(Modifier.padding(innerPadding))
                AppDestinations.AGGREGATOR -> AggregatorScreen(Modifier.padding(innerPadding))
                AppDestinations.RSS_FEED -> RssFeedScreen(Modifier.padding(innerPadding))
            }
        }
    }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    DAILY_CONTENT("Daily", Icons.Default.WbSunny),
    DASHBOARD("Dashboard", Icons.Default.BarChart),
    AGGREGATOR("Aggregator", Icons.Default.Newspaper),
    RSS_FEED("RSS", Icons.Default.RssFeed)
}

@PreviewScreenSizes
@Composable
fun MotivationalMorningsAppPreview() {
    MotivationalMorningsTheme {
        MotivationalMorningsApp()
    }
}
