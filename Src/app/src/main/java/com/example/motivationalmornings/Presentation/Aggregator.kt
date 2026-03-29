package com.example.motivationalmornings.Presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.AggregatorViewModel
import com.example.motivationalmornings.Persistence.AggregatorArticle

@Composable
fun AggregatorScreen(
    modifier: Modifier = Modifier,
    viewModel: AggregatorViewModel = viewModel(
        factory = AggregatorViewModel.provideFactory(LocalContext.current),
    ),
) {
    val sourceUrl by viewModel.sourceUrl.collectAsState()
    val subscribedSources by viewModel.subscribedSources.collectAsState()
    val selectedSourceUrl by viewModel.selectedSourceUrl.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshingAll by viewModel.isRefreshingAll.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var keywordText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        keywordText = viewModel.loadKeywords(context)
        viewModel.refreshAllSavedSources()
    }

    val filteredArticles = viewModel.filterArticles(articles, keywordText)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "News aggregator",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add links to news section pages (similar to a world news homepage). " +
                    "Sources are saved; headlines refresh when you return to this screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sourceUrl,
            onValueChange = { viewModel.onSourceUrlChanged(it) },
            label = { Text("Section URL") },
            placeholder = { Text("https://www.cnn.com/world") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.addSource() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add source")
        }

        if (subscribedSources.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sources",
                style = MaterialTheme.typography.titleSmall,
            )
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(subscribedSources, key = { it }) { url ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.selectSource(url) },
                            label = {
                                Text(
                                    url.take(20) + if (url.length > 20) "..." else "",
                                )
                            },
                        )
                        IconButton(
                            onClick = { viewModel.removeSource(url) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove source",
                            )
                        }
                    }
                }
            }
            selectedSourceUrl?.let { shown ->
                Text(
                    text = "Showing: ${shown.take(48)}${if (shown.length > 48) "..." else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = keywordText,
            onValueChange = {
                keywordText = it
                viewModel.saveKeywords(context, it)
            },
            label = { Text("Filter keywords (comma-separated)") },
            placeholder = { Text("york, canada, tech") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (isLoading || isRefreshingAll) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(filteredArticles, key = { it.url }) { article ->
                AggregatorArticleCard(
                    article = article,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}

@Composable
private fun AggregatorArticleCard(
    article: AggregatorArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = article.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
