package com.example.zzt.zworkmanager.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.zzt.zworkmanager.MyApplication
import java.util.concurrent.TimeUnit

/**
 * @author: zeting
 * @date: 2023/8/15
 * 工作单元
 */
class H1Work : Worker {
    val TAG = H1Work::class.java.simpleName
    var maxCount = 20

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        Log.v(TAG, "work 工作进度 创建 tags:$tags")
    }

    override fun doWork(): Result {
        Log.v(
            TAG,
            "work 工作进度 开始工作 tags:$tags  maxCount${maxCount} "
        )
        // 返回工作结果
        return timeWork()
    }


    fun timeWork(): Result {
        var count = 0;
        setProgressAsync(workDataOf("pro" to "0%"))
        while (true) {
            count++
            if (count >= maxCount) {
                break
            }
            setProgressAsync(workDataOf("pro" to "${count}%"))
            Log.v(
                TAG, "work 工作进度 :" + count +
                        "\n  maxCount:${maxCount} tags:$tags " +
                        "ThreadPool:${WorkManager.getInstance(MyApplication.instance.applicationContext).configuration.executor}"
            )
            try {
                TimeUnit.SECONDS.sleep(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setProgressAsync(workDataOf("pro" to "100%"))
        try {
            TimeUnit.SECONDS.sleep(1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }


}