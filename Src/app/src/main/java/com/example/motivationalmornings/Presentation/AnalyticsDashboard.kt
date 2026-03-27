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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.motivationalmornings.Persistence.Intention
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
    var selectedKeyword by remember { mutableStateOf<String?>(null) }
    var showAllKeywordsDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Keywords",
                    style = MaterialTheme.typography.titleLarge
                )
                if (state.allKeywords.size > 6) {
                    TextButton(onClick = { showAllKeywordsDialog = true }) {
                        Text("View all")
                    }
                }
            }
            Text(
                text = "Tap a keyword to see related intentions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.topKeywords.forEach { (keyword, count) ->
                    AssistChip(
                        onClick = { selectedKeyword = keyword },
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
            state.weatherDistribution.entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }
                        .thenBy { it.key }
                )
                .forEach { (weather, count) ->
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

    if (selectedKeyword != null) {
        KeywordIntentionsDialog(
            keyword = selectedKeyword!!,
            intentions = viewModel.getIntentionsByKeyword(selectedKeyword!!),
            onDismiss = { selectedKeyword = null }
        )
    }

    if (showAllKeywordsDialog) {
        AllKeywordsDialog(
            keywords = state.allKeywords,
            onKeywordClick = {
                selectedKeyword = it
                showAllKeywordsDialog = false
            },
            onDismiss = { showAllKeywordsDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllKeywordsDialog(
    keywords: List<Pair<String, Int>>,
    onKeywordClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Keywords") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keywords.forEach { (keyword, count) ->
                        AssistChip(
                            onClick = { onKeywordClick(keyword) },
                            label = { Text("$keyword ($count)") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun KeywordIntentionsDialog(
    keyword: String,
    intentions: List<Intention>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Intentions with \"$keyword\"") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                if (intentions.isEmpty()) {
                    Text("No intentions found for this keyword.")
                } else {
                    LazyColumn {
                        items(intentions) { intention ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = intention.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = intention.text,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
