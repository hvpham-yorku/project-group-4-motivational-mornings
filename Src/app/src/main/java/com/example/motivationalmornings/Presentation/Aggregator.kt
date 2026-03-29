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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.AggregatorViewModel
import com.example.motivationalmornings.BusinessLogic.StockQuote
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
    
    // Stock states
    val stockQuotes by viewModel.stockQuotes.collectAsState()
    val stockSymbolInput by viewModel.stockSymbolInput.collectAsState()
    
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
            text = "Aggregator & Stocks",
            style = MaterialTheme.typography.headlineSmall,
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stock Tracker Section
        StockTrackerSection(
            quotes = stockQuotes,
            symbolInput = stockSymbolInput,
            onSymbolChange = { viewModel.onStockSymbolInputChanged(it) },
            onAddStock = { viewModel.addStock() },
            onRemoveStock = { viewModel.removeStock(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "News aggregator",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add links to news section pages. Headlines refresh when you return.",
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
            Text("Add news source")
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
fun StockTrackerSection(
    quotes: List<StockQuote>,
    symbolInput: String,
    onSymbolChange: (String) -> Unit,
    onAddStock: () -> Unit,
    onRemoveStock: (String) -> Unit
) {
    Column {
        Text(
            text = "Market Watch",
            style = MaterialTheme.typography.titleLarge,
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = symbolInput,
                onValueChange = onSymbolChange,
                label = { Text("Add Symbol") },
                placeholder = { Text("e.g. BTC") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onAddStock,
                enabled = symbolInput.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add stock")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (quotes.isEmpty()) {
            Text(
                text = "No stocks tracked yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(quotes, key = { it.symbol }) { quote ->
                    StockCard(quote = quote, onRemove = { onRemoveStock(quote.symbol) })
                }
            }
        }
    }
}

@Composable
fun StockCard(
    quote: StockQuote,
    onRemove: () -> Unit
) {
    val isPositive = quote.change >= 0
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$${"%.2f".format(quote.price)}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}%",
                    color = color,
                    style = MaterialTheme.typography.labelMedium
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
