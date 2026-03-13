package com.example.motivationalmornings.Presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.DailyContentViewModel
import com.example.motivationalmornings.Persistence.QuoteOfTheDay

@Composable
fun DailyContent(
    modifier: Modifier = Modifier,
    viewModel: DailyContentViewModel = viewModel(
        factory = DailyContentViewModel.provideFactory(LocalContext.current)
    )
) {
    val quote by viewModel.quote.collectAsState()
    val imageResId by viewModel.imageResId.collectAsState()
    val savedIntentions by viewModel.intentions.collectAsState()
    val allQuotes by viewModel.allQuotes.collectAsState()
    var textFieldValue by remember { mutableStateOf("") }
    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var showManageQuotesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        QuoteOfTheDay(
            quote = quote,
            onAddQuoteClick = { showAddQuoteDialog = true },
            onManageQuotesClick = { showManageQuotesDialog = true }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ImageOfTheDay(imageResId = imageResId)
        Spacer(modifier = Modifier.height(16.dp))
        Intentions(
            intentions = savedIntentions,
            textFieldValue = textFieldValue,
            onIntentionChanged = { textFieldValue = it },
            onSubmit = {
                viewModel.saveIntention(textFieldValue)
                textFieldValue = "" // Clear field after submit
            }
        )

        // ✅ Weather goes here (inside the Column, AFTER Intentions)
        Spacer(modifier = Modifier.height(16.dp))
        WeatherScreen()
    }

    if (showAddQuoteDialog) {
        AddQuoteDialog(
            onDismiss = { showAddQuoteDialog = false },
            onConfirm = { newQuote ->
                viewModel.saveQuote(newQuote)
                showAddQuoteDialog = false
            }
        )
    }

    if (showManageQuotesDialog) {
        ManageQuotesDialog(
            quotes = allQuotes,
            onDismiss = { showManageQuotesDialog = false },
            onDeleteQuote = { quote ->
                viewModel.deleteQuote(quote)
            }
        )
    }
}

@Composable
fun QuoteOfTheDay(
    modifier: Modifier = Modifier,
    quote: String,
    onAddQuoteClick: () -> Unit,
    onManageQuotesClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quote of the day",
                    style = MaterialTheme.typography.headlineSmall
                )
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add custom quote") },
                            onClick = {
                                expanded = false
                                onAddQuoteClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View all quotes") },
                            onClick = {
                                expanded = false
                                onManageQuotesClick()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Quote") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter your quote") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ManageQuotesDialog(
    quotes: List<QuoteOfTheDay>,
    onDismiss: () -> Unit,
    onDeleteQuote: (QuoteOfTheDay) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Quotes") },
        text = {
            if (quotes.isEmpty()) {
                Text("No quotes saved yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(quotes) { quote ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quote.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            IconButton(onClick = { onDeleteQuote(quote) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete quote"
                                )
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
fun ImageOfTheDay(modifier: Modifier = Modifier, imageResId: Int) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Image of the day",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Image of the day placeholder",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun Intentions(
    modifier: Modifier = Modifier,
    intentions: List<String>,
    textFieldValue: String,
    onIntentionChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Intentions",
                style = MaterialTheme.typography.headlineSmall
            )

            if (intentions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saved Intentions:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    items(intentions) { intention ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = intention,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    text = "No intentions saved yet",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Set today's intention:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = onIntentionChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What are your intentions today?") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSubmit,
                    enabled = textFieldValue.isNotBlank()
                ) {
                    Text("Add Intention")
                }
            }
        }
    }
}