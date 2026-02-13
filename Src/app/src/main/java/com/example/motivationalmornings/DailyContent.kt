package com.example.motivationalmornings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val intentions by viewModel.intentions.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        QuoteOfTheDay(quote = quote)
        Spacer(modifier = Modifier.height(16.dp))
        ImageOfTheDay(imageResId = imageResId)
        Spacer(modifier = Modifier.height(16.dp))
        Intentions(intentions = intentions)
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
            // You can replace this with a real image loader like Coil
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Image of the day placeholder"
            )
        }
    }
}

@Composable
fun Intentions(modifier: Modifier = Modifier, intentions: String) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Intentions",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = intentions,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
