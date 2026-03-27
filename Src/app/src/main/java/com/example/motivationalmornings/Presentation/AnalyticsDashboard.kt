package com.example.motivationalmornings.Presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.AnalyticsViewModel
import com.example.motivationalmornings.DatabaseConfig
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.FakeAnalyticsRepository
import com.example.motivationalmornings.Persistence.HardcodedContentRepository
import com.example.motivationalmornings.Persistence.RoomContentRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsDashboard(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Analytics Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        SummaryStat(label = "Total Intentions Set", value = state.totalIntentions.toString())

        Spacer(modifier = Modifier.height(24.dp))

        if (state.topKeywords.isNotEmpty()) {
            Text(
                text = "Top Keywords",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.topKeywords.forEach { (keyword, count) ->
                    AssistChip(
                        onClick = { },
                        label = { Text("$keyword ($count)") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.weatherDistribution.isNotEmpty()) {
            Text(
                text = "Weather Distribution",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            state.weatherDistribution.forEach { (weather, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = weather)
                    Text(text = count.toString(), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.intentionsByDate.isNotEmpty()) {
            Text(
                text = "Intentions per Day",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            state.intentionsByDate.forEach { (date, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = date)
                    Text(text = count.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        }
    }
}

class AnalyticsViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val contentRepository = if (DatabaseConfig.USE_REAL_DATABASE) {
            RoomContentRepository(AppDatabase.getDatabase(context).dailyContentDao())
        } else {
            HardcodedContentRepository()
        }
        val analytics = Analytics(FakeAnalyticsRepository())
        return AnalyticsViewModel(contentRepository, analytics) as T
    }
}
