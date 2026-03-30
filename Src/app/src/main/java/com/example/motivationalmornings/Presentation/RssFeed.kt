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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.RssItem

@Composable
fun RssFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: RssFeedViewModel = viewModel(
        factory = RssFeedViewModel.provideFactory(LocalContext.current)
    )
) {
    val rssItems by viewModel.rssItems.collectAsState()
    val currentFeedUrl by viewModel.currentFeedUrl.collectAsState()
    val subscribedFeeds by viewModel.subscribedFeeds.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = currentFeedUrl,
            onValueChange = { viewModel.onFeedUrlChanged(it) },
            label = { Text(text = "RSS feed URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.subscribeToFeed() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Subscribe")
        }

        if (subscribedFeeds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Subscribed Feeds", style = MaterialTheme.typography.titleSmall)
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subscribedFeeds) { feedUrl ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.loadFeed(feedUrl) },
                            label = { Text(feedUrl.truncateForChipLabel()) }
                        )
                        IconButton(
                            onClick = { viewModel.unsubscribeFromFeed(feedUrl) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Unsubscribe from feed"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subscribedFeeds.isEmpty()) {
            Text(
                text = "No feeds subscribed yet. Enter a valid RSS URL above and tap Subscribe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(rssItems) { item ->
                RssItemCard(
                    item = item,
                    onClick = {
                        // Open the article link in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun RssItemCard(
    item: RssItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = item.link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}