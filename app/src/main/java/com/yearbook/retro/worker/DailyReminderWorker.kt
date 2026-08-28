package com.yearbook.retro.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yearbook.retro.MainActivity
import com.yearbook.retro.R
import com.yearbook.retro.YearbookApp
import com.yearbook.retro.data.model.DailyDropStatus
import com.yearbook.retro.media.DateStampRenderer
import kotlinx.coroutines.flow.firstOrNull

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "yearbook_daily_reminders"
        const val NOTIFICATION_ID = 1984
        const val EXTRA_YEARBOOK_ID = "extra_yearbook_id"
    }

    override suspend fun doWork(): Result {
        val app = context.applicationContext as? YearbookApp ?: return Result.success()
        val authRepo = app.container.authRepository
        val yearbookRepo = app.container.yearbookRepository

        val currentUser = authRepo.getCurrentUser() ?: return Result.success()
        val pendingBooks = yearbookRepo.getPendingYearbooks(currentUser.uid).firstOrNull() ?: emptyList()

        val pending = pendingBooks.firstOrNull { it.second == DailyDropStatus.PENDING }
        if (pending != null) {
            val book = pending.first
            showReminderNotification(book.id, book.title)
        }

        return Result.success()
    }

    private fun showReminderNotification(yearbookId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Yearbook Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to contribute today's photo to your shared yearbooks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_YEARBOOK_ID, yearbookId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_leather_book_cover)
            .setContentTitle("Drop Today's Photo \uD83D\uDCD6✨")
            .setContentText("Don't forget to capture your daily memory for '$title' before midnight!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your friends are waiting! Drop today's memory into '$title' to keep your retro album streak alive.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
