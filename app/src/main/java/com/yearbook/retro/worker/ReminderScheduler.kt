package com.yearbook.retro.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val UNIQUE_WORK_NAME = "YearbookDailyReminderWork"

    /**
     * Schedules periodic reminder to run daily at 8:00 PM local time.
     */
    fun scheduleDailyReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    /**
     * Triggers an immediate one-time test run for verification and testing.
     */
    fun triggerImmediateTest(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val oneTimeWork = OneTimeWorkRequestBuilder<DailyReminderWorker>().build()
        workManager.enqueueUniqueWork("ImmediateTestReminder", ExistingWorkPolicy.REPLACE, oneTimeWork)
    }
}
