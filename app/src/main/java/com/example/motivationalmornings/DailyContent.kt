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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DailyContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        QuoteOfTheDay()
        Spacer(modifier = Modifier.height(16.dp))
        ImageOfTheDay()
        Spacer(modifier = Modifier.height(16.dp))
        Intentions()
    }
}

@Composable
fun QuoteOfTheDay(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quote of the day",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"The best way to predict the future is to create it.\"",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ImageOfTheDay(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Image of the day",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            // You can replace this with a real image loader like Coil
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Image of the day placeholder"
            )
        }
    }
}

@Composable
fun Intentions(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Intentions",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "My intentions for today are...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
