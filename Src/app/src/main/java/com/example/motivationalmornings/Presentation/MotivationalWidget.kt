package com.example.motivationalmornings.Presentation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.action.actionStartActivity
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.Intention
import java.time.LocalDate

class MotivationalWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val status = prefs[StatusKey] ?: ""
            
            MotivationalWidgetContent(context, status)
        }
    }

    @Composable
    private fun MotivationalWidgetContent(context: Context, status: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(android.R.color.white)
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Motivational Mornings",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(android.R.color.black)
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "Set today's intention:",
                style = TextStyle(color = ColorProvider(android.R.color.darker_gray))
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Button(
                    text = "Productive",
                    onClick = actionRunCallback<SaveIntentionAction>(
                        actionParametersOf(IntentionKey to "I will be productive")
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Button(
                    text = "Kind",
                    onClick = actionRunCallback<SaveIntentionAction>(
                        actionParametersOf(IntentionKey to "I will be kind")
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            // Android home screen widgets have strict limitations on direct text input for security.
            // The standard pattern is to provide a button that opens the app's input field.
            Button(
                text = "Type Custom Intention",
                onClick = actionStartActivity(Intent(context, MainActivity::class.java))
            )

            if (status.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                Text(
                    text = status,
                    style = TextStyle(
                        color = ColorProvider(android.R.color.holo_green_dark),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class MotivationalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MotivationalWidget()
}

val StatusKey = stringPreferencesKey("status")
val IntentionKey = ActionParameters.Key<String>("intention")

class SaveIntentionAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intentionText = parameters[IntentionKey] ?: return
        
        val database = AppDatabase.getDatabase(context)
        val dao = database.dailyContentDao()
        dao.insertIntention(
            Intention(
                text = intentionText,
                date = LocalDate.now().toString()
            )
        )

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                set(StatusKey, "Saved: $intentionText")
            }
        }
        
        MotivationalWidget().update(context, glanceId)
    }
}
