package com.example.zzt.zworkmanager.work

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.zzt.zworkmanager.MyApplication
import java.util.concurrent.TimeUnit

/**
 * @author: zeting
 * @date: 2023/8/15
 * 工作单元
 */
class GWork : Worker {
    val TAG = GWork::class.java.simpleName
    var NOTIFICATION_ID = 9999
    var maxCount = 10

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        Log.v(TAG, "work 工作进度 创建 tags:$tags")
    }

    override fun doWork(): Result {
        val param = inputData.getString("param") ?: return Result.failure()
        val aaa = inputData.getInt("aaa", 0)
        Log.v(
            TAG,
            "work 工作进度 开始工作 tags:$tags  maxCount${maxCount} \n param:${param} aaa:${aaa}"
        )


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