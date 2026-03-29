package com.example.motivationalmornings.BusinessLogic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Presentation.MainActivity
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.dailyContentDao()

        val quotes = dao.getAllQuotes().first()
        val quote = if (quotes.isEmpty()) {
            "Start your day with a positive thought!"
        } else {
            val sortedQuotes = quotes.sortedBy { it.uid }
            val dayIndex = (LocalDate.now().toEpochDay() % sortedQuotes.size).toInt()
            sortedQuotes[dayIndex].text
        }

        showNotification(quote)
        return Result.success()
    }

    private fun showNotification(quote: String) {
        val channelId = "daily_motivation_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Daily Motivation",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for daily motivational quotes and images"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.imageotd) // Using a placeholder, should ideally be a small icon
            .setContentTitle("Motivational Morning")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = LocalDateTime.now()
            var nextRun = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
            
            if (nextRun.isBefore(now)) {
                nextRun = nextRun.plusDays(1)
            }

            val delay = Duration.between(now, nextRun).toMinutes()

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_motivation_notification",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("daily_motivation_notification")
        }
    }
}
