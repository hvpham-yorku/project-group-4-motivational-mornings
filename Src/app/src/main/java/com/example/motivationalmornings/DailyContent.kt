package com.example.motivationalmornings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DailyContent(
    modifier: Modifier = Modifier,
    viewModel: DailyContentViewModel = viewModel(factory = DailyContentViewModel.provideFactory())
) {
    val quote by viewModel.quote.collectAsState()
    val imageResId by viewModel.imageResId.collectAsState()
    val savedIntentions by viewModel.intentions.collectAsState()
    var textFieldValue by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        QuoteOfTheDay(quote = quote)
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
        WeatherScreen()
    }
}

@Composable
fun QuoteOfTheDay(modifier: Modifier = Modifier, quote: String) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quote of the day",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
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
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}

@Composable
fun Intentions(
    modifier: Modifier = Modifier,
    intentions: List<String>,  // Changed to List<String>
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

            // Show previously saved intentions
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
                        .height(120.dp) // Fixed height to prevent expansion
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

            // Input section
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
