package com.example.zzt.zworkmanager.work

import android.content.Context
import android.util.Log
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.zzt.zworkmanager.MyApplication
import com.example.zzt.zworkmanager.R
import com.zzt.utilcode.util.FileIOUtils
import com.zzt.utilcode.util.FileUtils
import com.zzt.utilcode.util.NotificationUtils
import com.zzt.utilcode.util.NotificationUtils.ChannelConfig
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author: zeting
 * @date: 2023/8/15
 * 工作单元
 */
class CWork : Worker {
    val TAG = CWork::class.java.simpleName
    var NOTIFICATION_ID = 9999
    var maxCount = 10

    constructor(context: Context, workerParams: WorkerParameters) : super(context, workerParams) {
        writeLogToSdCard(TAG, "work 工作进度 创建 tags:$tags")
    }

    override fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationUtils.getNotification(
            ChannelConfig.DEFAULT_CHANNEL_CONFIG
        ) { param ->
            param.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("title tags:$tags")
                .setContentText("content text: $NOTIFICATION_ID")
                .setAutoCancel(true)
        }

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override fun doWork(): Result {
        val randoms = (0..10).random()
        maxCount += randoms

        writeLogToSdCard(TAG, "work 工作进度 开始工作 tags:$tags  maxCount${maxCount}")

        // 工作
        timeWork()

        // 返回工作结果
        return Result.success()
    }


    fun timeWork() {
        var count = 0;
        while (true) {
            count++
            if (count >= maxCount) {
                break
            }
            writeLogToSdCard(
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
    }

    private val EXECUTOR = Executors.newSingleThreadExecutor()


    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile
        if (!FileUtils.createOrExistsDir(file.parentFile)) {
            return false
        }
        return try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun writeFileFromString(
        file: File?,
        content: String?,
        append: Boolean
    ): Boolean {
        if (file == null || content == null) return false
        if (!createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }
        var bw: BufferedWriter? = null
        return try {
            bw = BufferedWriter(FileWriter(file, append))
            bw.write(content)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                bw?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun writeLogToSdCard(tag: String, contents: String) {
        EXECUTOR.execute {
            val d = Date()
            val format = simpleDateFormat?.format(d)
            val date = format?.substring(0, 10)
            val currentLogFilePath = getCurrentLogFilePath(d)
            if (!createOrExistsFile(currentLogFilePath, date ?: "")) {
                return@execute
            }
            writeFileFromString(File(currentLogFilePath), "$format >> $contents", true);
        }
    }

    private fun createOrExistsFile(filePath: String, date: String): Boolean {
        val file = File(filePath)
        if (file.exists()) return file.isFile
        return if (!FileUtils.createOrExistsDir(file.parentFile)) false else try {
            val isCreate = file.createNewFile()
            if (isCreate) {
                FileIOUtils.writeFileFromString(filePath, date)
            }
            isCreate
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    var simpleDateFormat: SimpleDateFormat? = null
    private fun getSdf(): SimpleDateFormat? {
        if (simpleDateFormat == null) {
            simpleDateFormat = SimpleDateFormat("yyyy_MM_dd HH:mm:ss.SSS ", Locale.getDefault())
        }
        return simpleDateFormat
    }

    private fun getCurrentLogFilePath(d: Date): String {
        val format = getSdf()?.format(d)
        val date = format!!.substring(0, 10)
        val externalFilesDir = applicationContext.getExternalFilesDir(null)
        // 设置文件路径
        val fileDir = File(externalFilesDir?.absolutePath, "zztLogW")
        return fileDir.absolutePath + File.separator + "util_" + date + ".txt"
    }


}