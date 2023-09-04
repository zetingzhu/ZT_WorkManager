package com.example.zzt.zworkmanager.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.zzt.zworkmanager.MyApplication
import java.util.Arrays
import java.util.concurrent.TimeUnit

/**
 * @author: zeting
 * @date: 2023/8/15
 * 工作单元
 */
class IWork : Worker {
    val TAG = IWork::class.java.simpleName
    var maxCount = 5
    var lastResult: String? = null
    var lastResultArray: Array<String>? = null

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        Log.v(TAG, "work 工作进度 创建 tags:$tags")
    }

    override fun doWork(): Result {
        lastResult = inputData.getString("result")
        lastResultArray = inputData.getStringArray("result")
        val randoms = (0..10).random()
        maxCount += randoms
        Log.w(
            TAG,
            "work 工作进度 开始工作 tags:$tags  maxCount${maxCount} lastResult:${lastResult} lastResultArray:${
                Arrays.toString(
                    lastResultArray
                )
            }"
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
        var resultStr = tags.toString()
        if (lastResult?.isNotEmpty() == true) {
            resultStr = resultStr.plus(" > ls:${lastResult} ")
        }
        if (lastResultArray?.isNotEmpty() == true) {
            resultStr = resultStr.plus(" 》》 lsa:${Arrays.toString(lastResultArray)} ")
        }

        val resultData = Data.Builder().putString("result", resultStr).build()
        Log.e(TAG, "work resultData:$resultData")
        return Result.success(resultData)
    }
}