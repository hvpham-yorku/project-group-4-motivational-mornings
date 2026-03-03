package com.example.motivationalmornings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AggregatorScreen(
    modifier: Modifier = Modifier,
    viewModel: AggregatorViewModel = viewModel()
) {
    val aggregatorText by viewModel.aggregatorText.collectAsState()

    Text(
        text = aggregatorText,
        modifier = modifier
    )
}
