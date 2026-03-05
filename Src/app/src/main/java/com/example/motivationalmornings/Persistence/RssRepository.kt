package com.example.motivationalmornings.Presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.RssItem

@Composable
fun RssFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: RssFeedViewModel = viewModel()
) {
    val rssItems by viewModel.rssItems.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Subscribed Feeds",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (rssItems.isEmpty()) {
            // Empty state
            Text(
                text = "No subscribed feeds. You've unsubscribed from all feeds.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // RSS feed list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(rssItems) { item ->
                    RssItemCard(
                        item = item,
                        onUnsubscribe = { viewModel.unsubscribeFromFeed(item.id) },
                        onClick = {
                            // Later: open link in CustomTab/Browser or navigate to detail screen
                            // For now this is just a stub
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RssItemCard(
    item: RssItem,
    onUnsubscribe: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Content column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.link,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Unsubscribe button
            IconButton(
                onClick = onUnsubscribe,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Unsubscribe from ${item.title}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


/*package com.example.motivationalmornings.Persistence

class RssRepository {
    private val dummyItems = listOf(
        RssItem(
            id = 1,
            title = "Morning Motivation",
            description = "Start your day with a positive quote.",
            link = "https://example.com/morning-motivation"
        ),
        RssItem(
            id = 2,
            title = "Mindfulness Minute",
            description = "A short mindfulness exercise for your commute.",
            link = "https://example.com/mindfulness-minute"
        ),
        RssItem(
            id = 3,
            title = "Gratitude Check",
            description = "Three things to be grateful for today.",
            link = "https://example.com/gratitude-check"
        )
    )

    fun getRssItems(): List<RssItem> = dummyItems
}*/
