package com.example.motivationalmornings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RssFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: RssFeedViewModel = viewModel()
) {
    val rssFeedText by viewModel.rssFeedText.collectAsState()

    Text(
        text = rssFeedText,
        modifier = modifier
    )
}
