package com.example.motivationalmornings.Presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    vm: WeatherViewModel = viewModel()
) {
    val weather by vm.weather.collectAsState()
    val city by vm.city.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    // Load once on first open (default city = Toronto)
    LaunchedEffect(Unit) {
        vm.loadWeather()
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Weather")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { vm.setCity(it) },
            label = { Text("City") },
            placeholder = { Text("e.g., Toronto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { vm.loadWeather() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Loading..." else "Search")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (error != null) {
            Text("Error: $error")
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (weather == null && isLoading) {
            Text("Loading...")
        } else if (weather != null) {
            Text("Temp: ${weather!!.temperatureC} C")
            Text("Wind: ${weather!!.windSpeedKmh} km/h")
            Text("Condition: ${weather!!.condition}")
        }
    }
}