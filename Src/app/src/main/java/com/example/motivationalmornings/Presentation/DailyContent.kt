package com.example.motivationalmornings.Presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.BusinessLogic.IntentionSuggestion
import com.example.motivationalmornings.BusinessLogic.WeatherViewModel
import coil.compose.AsyncImage
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import java.io.File

@Composable
fun DailyContent(
    modifier: Modifier = Modifier,
    viewModel: DailyContentViewModel = viewModel(
        factory = DailyContentViewModel.provideFactory(LocalContext.current)
    ),
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val imageOfTheDay by viewModel.imageOfTheDay.collectAsState()
    val savedIntentions by viewModel.intentions.collectAsState()
    val allIntentions by viewModel.allIntentions.collectAsState()
    val allQuotes by viewModel.allQuotes.collectAsState()
    val intentionSuggestions by viewModel.intentionSuggestions.collectAsState()
    val weatherInfo by weatherViewModel.weather.collectAsState()

    LaunchedEffect(weatherInfo) {
        val weatherString = weatherInfo?.let { "${it.condition}, ${it.temperatureC}°C" }
        viewModel.updateIntentionSuggestionContext(weatherString)
    }
    val allImages by viewModel.allImages.collectAsState()

    var textFieldValue by remember { mutableStateOf("") }
    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var showManageQuotesDialog by remember { mutableStateOf(false) }
    var showArchiveIntentionsDialog by remember { mutableStateOf(false) }
    var showManageImagesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        QuoteOfTheDayCard(
            quote = quote,
            onAddQuoteClick = { showAddQuoteDialog = true },
            onManageQuotesClick = { showManageQuotesDialog = true },
            onLikeClick = { viewModel.likeQuote() },
            onDislikeClick = { viewModel.dislikeQuote() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        ImageOfTheDayCard(
            image = imageOfTheDay,
            onManageImagesClick = { showManageImagesDialog = true },
            onLikeClick = { viewModel.likeImage() },
            onDislikeClick = { viewModel.dislikeImage() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        IntentionsCard(
            intentions = savedIntentions,
            suggestedIntentions = intentionSuggestions,
            textFieldValue = textFieldValue,
            onIntentionChanged = { textFieldValue = it },
            onSuggestionClick = { textFieldValue = it },
            onSubmit = {
                val weatherString = weatherInfo?.let { "${it.condition}, ${it.temperatureC}°C" }
                viewModel.saveIntention(textFieldValue, weatherString)
                textFieldValue = "" // Clear field after submit
            },
            onViewArchiveClick = { showArchiveIntentionsDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
        WeatherScreen(vm = weatherViewModel)
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
            onDeleteQuote = { viewModel.deleteQuote(it) }
        )
    }
    if (showArchiveIntentionsDialog) {
        ArchiveIntentionsDialog(
            intentions = allIntentions,
            onDismiss = { showArchiveIntentionsDialog = false },
            onSaveReflection = { uid, reflection -> viewModel.saveReflection(uid, reflection) }
        )
    }
    if (showManageImagesDialog) {
        ManageImagesDialog(
            images = allImages,
            onDismiss = { showManageImagesDialog = false },
            onDeleteImage = { viewModel.deleteImage(it) },
            onAddImageFromUri = { uri -> viewModel.addImageFromUri(uri) }
        )
    }
}

// ─── Image of the Day card ────────────────────────────────────────────────────

@Composable
fun ImageOfTheDayCard(
    image: ImageOfTheDay?,
    onManageImagesClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Image of the day",
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
                            text = { Text("Manage images") },
                            onClick = {
                                expanded = false
                                onManageImagesClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                image == null -> {
                    Text(
                        text = "No images available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                image.drawableResId != null -> {
                    // Built-in drawable
                    Image(
                        painter = painterResource(id = image.drawableResId),
                        contentDescription = "Image of the day",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                image.filePath != null -> {
                    // User-uploaded file — use Coil's AsyncImage to load from disk
                    AsyncImage(
                        model = File(image.filePath),
                        contentDescription = "Image of the day",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLikeClick,
                    enabled = image != null
                ) {
                    Text("Like")
                }

                OutlinedButton(
                    onClick = onDislikeClick,
                    enabled = image != null
                ) {
                    Text("Dislike")
                }
            }
        }
    }
}

// ─── Manage Images dialog ─────────────────────────────────────────────────────

@Composable
fun ManageImagesDialog(
    images: List<ImageOfTheDay>,
    onDismiss: () -> Unit,
    onDeleteImage: (ImageOfTheDay) -> Unit,
    onAddImageFromUri: (Uri) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddImageFromUri(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Images") },
        text = {
            Column {
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add image from gallery")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (images.isEmpty()) {
                    Text(
                        text = "No images in pool.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Image pool (${images.size})",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(images, key = { it.uid }) { image ->
                            ImagePoolItem(
                                image = image,
                                onDelete = { onDeleteImage(image) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ImagePoolItem(
    image: ImageOfTheDay,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            image.drawableResId != null -> {
                Image(
                    painter = painterResource(id = image.drawableResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp)
                )
            }
            image.filePath != null -> {
                AsyncImage(
                    model = File(image.filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        val label = when {
            image.filePath != null -> File(image.filePath).name
            image.drawableResId != null -> "Built-in image #${image.uid}"
            else -> "Image #${image.uid}"
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remove image")
        }
    }
}

// ─── Quote of the Day card ────────────────────────────────────────────────────

@Composable
fun QuoteOfTheDayCard(
    modifier: Modifier = Modifier,
    quote: String,
    onAddQuoteClick: () -> Unit,
    onManageQuotesClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        )
    ) {
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
                            onClick = { expanded = false; onAddQuoteClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("View all quotes") },
                            onClick = { expanded = false; onManageQuotesClick() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = quote, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onLikeClick) {
                    Text("Like")
                }

                OutlinedButton(onClick = onDislikeClick) {
                    Text("Dislike")
                }
            }
        }
    }
}

// ─── Add Quote dialog ─────────────────────────────────────────────────────────

@Composable
fun AddQuoteDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var quoteText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Quote") },
        text = {
            OutlinedTextField(
                value = quoteText,
                onValueChange = { quoteText = it },
                label = { Text("Your quote") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(quoteText) }, enabled = quoteText.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─── Manage Quotes dialog ─────────────────────────────────────────────────────

@Composable
fun ManageQuotesDialog(
    quotes: List<QuoteOfTheDay>,
    onDismiss: () -> Unit,
    onDeleteQuote: (QuoteOfTheDay) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Quotes") },
        text = {
            if (quotes.isEmpty()) {
                Text("No quotes saved.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(quotes, key = { it.uid }) { quote ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quote.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteQuote(quote) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete quote")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

// ─── Intentions card ──────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntentionsCard(
    modifier: Modifier = Modifier,
    intentions: List<String>,
    suggestedIntentions: List<IntentionSuggestion> = emptyList(),
    textFieldValue: String,
    onIntentionChanged: (String) -> Unit,
    onSuggestionClick: (String) -> Unit = {},
    onSubmit: () -> Unit,
    onViewArchiveClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Intentions",
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
                            text = { Text("View archive") },
                            onClick = {
                                expanded = false
                                onViewArchiveClick()
                            }
                        )
                    }
                }
            }

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
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                            )
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
            if (suggestedIntentions.isNotEmpty()) {
                Text(
                    text = "Suggestions from your patterns:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedIntentions.forEach { suggestion ->
                        AssistChip(
                            onClick = { onSuggestionClick(suggestion.intention) },
                            label = {
                                Text(
                                    text = suggestion.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
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

// ─── Archive Intentions dialog ────────────────────────────────────────────────

@Composable
fun ArchiveIntentionsDialog(
    intentions: List<Intention>,
    onDismiss: () -> Unit,
    onSaveReflection: (Int, String) -> Unit
) {
    var intentionToReflectOn by remember { mutableStateOf<Intention?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Intentions Archive") },
        text = {
            if (intentions.isEmpty()) {
                Text("No intentions saved yet.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(intentions, key = { it.uid }) { intention ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (intention.time != null) "${intention.date} at ${intention.time}" else intention.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (!intention.weather.isNullOrBlank()) {
                                            Text(
                                                text = "Weather: ${intention.weather}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { intentionToReflectOn = intention },
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        Text(if (intention.reflection.isNullOrBlank()) "Reflect" else "Edit Reflection")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (intention.reflection != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Reflection: ${intention.reflection}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                TextButton(onClick = { intentionToReflectOn = intention }) {
                                    Text(if (intention.reflection != null) "Edit reflection" else "Add reflection")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )

    intentionToReflectOn?.let { intention ->
        AddReflectionDialog(
            intention = intention,
            onDismiss = { intentionToReflectOn = null },
            onConfirm = { reflection ->
                onSaveReflection(intention.uid, reflection)
                intentionToReflectOn = null
            }
        )
    }
}

@Composable
fun AddReflectionDialog(
    intention: Intention,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reflectionText by remember { mutableStateOf(intention.reflection ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reflect on Intention") },
        text = {
            Column {
                Text("Intention: ${intention.text}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    label = { Text("How did it go?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reflectionText) },
                enabled = reflectionText.isNotBlank()
            ) { Text("Save Reflection") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
