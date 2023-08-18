package com.example.zzt.zworkmanager.work

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * @author: zeting
 * @date: 2023/8/15
 */
class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAG_OUTPUT: String = "tag-DailyWorker"

    override fun doWork(): Result {

        Log.d(TAG_OUTPUT , "work  DailyWorker")
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 5)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(TAG_OUTPUT)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueue(dailyWorkRequest)
        return Result.success()
    }
}