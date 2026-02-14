package com.example.motivationalmornings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    vm: WeatherViewModel = viewModel()
) {
    val weather by vm.weather.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        vm.loadWeather()
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Weather")

        if (weather == null) {
            Text("Loading...")
        } else {
            Text("Temp: ${weather!!.temperatureC} C")
            Text("Wind: ${weather!!.windSpeedKmh} km/h")
            Text("Condition: ${weather!!.condition}")
        }
    }
}
