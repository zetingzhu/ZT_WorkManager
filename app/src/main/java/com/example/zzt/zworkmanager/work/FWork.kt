package com.example.zzt.zworkmanager.work

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.zzt.zworkmanager.MyApplication
import com.example.zzt.zworkmanager.R
import com.zzt.utilcode.util.NotificationUtils
import com.zzt.utilcode.util.NotificationUtils.ChannelConfig
import com.zzt.utilcode.util.Utils
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * @author: zeting
 * @date: 2023/8/15
 * 工作单元
 */
class FWork : Worker {
    val TAG = FWork::class.java.simpleName
    var NOTIFICATION_ID = 9999
    var maxCount = 10

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        Log.v(TAG, "work 工作进度 创建 tags:$tags")
    }

    override fun doWork(): Result {

        Log.v(TAG, "work 工作进度 开始工作 tags:$tags  maxCount${maxCount}")


        // 返回工作结果
        return timeWork()
    }


    fun timeWork(): Result {
        var count = 0;
        while (true) {
            count++
            if (count >= maxCount) {
                break
            }
            Log.v(
                TAG, "work 工作进度 :" + count +
                        "\n  maxCount:${maxCount} tags:$tags " +
                        "ThreadPool:${WorkManager.getInstance(MyApplication.instance.applicationContext).configuration.executor}"
            )
            if (count == 5) {
                return Result.retry()
            }
            try {
                TimeUnit.SECONDS.sleep(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Log.v(TAG, "work 工作进度 Result success ")
        return Result.success()
    }

}