package com.example.motivationalmornings.Presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.motivationalmornings.Persistence.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class MotivationalWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.dailyContentDao()
        
        val today = LocalDate.now().toString()
        // Fetch intentions for today
        val intentions = dao.getIntentionsByDate(today).first()
        
        // Fetch quote of the day
        val quotes = dao.getAllQuotes().first()
        val quote = if (quotes.isEmpty()) {
            "The best way to predict the future is to create it."
        } else {
            val sortedQuotes = quotes.sortedBy { it.uid }
            val dayIndex = (LocalDate.now().toEpochDay() % sortedQuotes.size).toInt()
            sortedQuotes[dayIndex].text
        }

        provideContent {
            MotivationalWidgetContent(quote, intentions)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun MotivationalWidgetContent(quote: String, intentions: List<String>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ColorProvider(Color.White))
                .padding(12.dp)
                .clickable(actionStartActivity(MainActivity::class.java)),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Motivational Mornings",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorProvider(Color.Black)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                
                Button(
                    text = "Refresh",
                    onClick = actionRunCallback<RefreshAction>()
                )
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "Quote of the Day:",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF0099CC)) // holo_blue_dark
                )
            )
            Text(
                text = "\"$quote\"",
                style = TextStyle(color = ColorProvider(Color.Black))
            )
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            Text(
                text = "Today's Intentions:",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF669900)) // holo_green_dark
                )
            )
            
            if (intentions.isEmpty()) {
                Text(
                    text = "No intentions set yet.",
                    style = TextStyle(color = ColorProvider(Color.Gray))
                )
            } else {
                intentions.take(3).forEach { intention ->
                    Text(
                        text = "• $intention",
                        style = TextStyle(color = ColorProvider(Color.Black)),
                        maxLines = 1
                    )
                }
                if (intentions.size > 3) {
                    Text(
                        text = "... and ${intentions.size - 3} more",
                        style = TextStyle(color = ColorProvider(Color.Gray))
                    )
                }
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        MotivationalWidget().update(context, glanceId)
    }
}

class MotivationalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MotivationalWidget()
}
