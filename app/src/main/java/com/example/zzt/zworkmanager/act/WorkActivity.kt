package com.example.zzt.zworkmanager.act

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.zzt.zworkmanager.R
import com.example.zzt.zworkmanager.databinding.ActivityWorkBinding
import com.example.zzt.zworkmanager.work.AWork
import com.example.zzt.zworkmanager.work.BWork
import com.example.zzt.zworkmanager.work.CWork
import com.example.zzt.zworkmanager.work.DWork
import com.example.zzt.zworkmanager.work.DailyWorker
import com.example.zzt.zworkmanager.work.EWork
import com.example.zzt.zworkmanager.work.FWork
import com.example.zzt.zworkmanager.work.GWork
import com.zzt.adapter.StartActivityRecyclerAdapter
import com.zzt.entity.StartActivityDao
import com.zzt.utilcode.util.LogUtils
import com.zzt.utilcode.util.NotificationUtils
import com.zzt.utilcode.util.PathUtils
import com.zzt.utilcode.util.TimeUtils
import java.io.File
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class WorkActivity : AppCompatActivity() {
    val TAG = "WorkActivity"
    private lateinit var binding: ActivityWorkBinding

    var notifyId = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        initView()

        Log.d(TAG, "onNewIntent id ${intent?.extras?.getInt("id")}")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent id ${intent?.extras?.getInt("id")}")
    }

    private fun initView() {
        val mConfig = LogUtils.getConfig()
        // 设置开启文件
        mConfig.isLog2FileSwitch = true
        // 设置文件路径
        val fileDir = File(PathUtils.getExternalAppFilesPath(), "zztLog")
        mConfig.setDir(fileDir)
        // 监听写入情况
        mConfig.setOnFileOutputListener { filePath, content ->
//            Log.d(TAG, "filePath:$filePath content:$content")
        }
        // 写入日志
        LogUtils.eTag(TAG, "启动文件日志>" + fileDir.absolutePath)

//        binding.wbView.loadUrl("https://static.xtrendspeed.com/product/register_banner/banner.html?t=1692099300049&deviceId=eead221f-ccc9-4a82-914b-6443c2256d0e&sourceId=10&device=1&v=1.7.2.154&language=zh-CN&market=debug&exchangeId=7&timeZoneOffset=28800&remoteLoginTips=1&sensorsId=&ipcc=SG&auth=36d573768302aa331ec47aa5b3ee5dc3&theme=1")


        val mListDialog: MutableList<StartActivityDao> = ArrayList()
        mListDialog.add(StartActivityDao("往本地文件写日志", " ", "8"))
        mListDialog.add(StartActivityDao("发送通知", " ", "3"))
        mListDialog.add(StartActivityDao("修改 WorkManager 线程池", "修改 WorkManager 线程池", "2"))
        mListDialog.add(StartActivityDao("普通运行一次任务", "OneTimeWorkRequest", "1"))
        mListDialog.add(StartActivityDao("一日一次任务", "每天凌晨5点开始", "4"))
        mListDialog.add(StartActivityDao("加急一次任务", "加急", "5"))
        mListDialog.add(StartActivityDao("定期任务", "PeriodicWorkRequest", "6"))
        mListDialog.add(StartActivityDao("定期+灵活周期任务", "", "7"))
        mListDialog.add(StartActivityDao("超长任务", "", "9"))
        mListDialog.add(StartActivityDao("延迟一次性工作", "", "10"))
        mListDialog.add(StartActivityDao("工作的约束条件", "给一次性工作加上条件", "11"))
        mListDialog.add(StartActivityDao("工作的约束条件", "给定期工作加上条件", "12"))
        mListDialog.add(StartActivityDao("监听工作状态", "", "13"))
        mListDialog.add(StartActivityDao("重试策略", "LINEAR 重试 10,20,30,40", "14"))
        mListDialog.add(StartActivityDao("重试策略", "EXPONENTIAL 重试10,20,40,80", "15"))
        mListDialog.add(StartActivityDao("入参", "", "16"))
        mListDialog.add(StartActivityDao("唯一工作", "", "17"))
        mListDialog.add(StartActivityDao("手动获取某个工作状态信息", "", "18"))


        StartActivityRecyclerAdapter.setAdapterData(
            binding.rvList,
            RecyclerView.VERTICAL,
            mListDialog
        ) { itemView: View?, position: Int, data: StartActivityDao ->
            when (data.arouter) {
                "8" -> {
                    LogUtils.vTag(TAG, ">> 开始往文件中写日志了")
                }

                "2" -> {
                    // 初始化自己的 WorkManager 线程池，默认4个
                    val configuration = Configuration.Builder()
                        .setExecutor(Executors.newFixedThreadPool(2))
                        .setMinimumLoggingLevel(android.util.Log.DEBUG)
                        .build()
                    WorkManager.initialize(this@WorkActivity, configuration)
                }

                "3" -> {
                    NotificationUtils.notify(notifyId++) { param ->
                        intent.putExtra("id", notifyId);
                        param.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("title")
                            .setContentText("content text: $notifyId")
                            .setContentIntent(
                                PendingIntent.getActivity(
                                    this,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            )
                            .setAutoCancel(true)
                    }
                }

                "4" -> {
                    val TAG_OUTPUT: String = "tag-WorkActivity"

                    val constraints = Constraints.Builder()
                        .setRequiresCharging(true)
                        .build()

                    //This is my new defaultval
                    var currentDate = Calendar.getInstance()
                    val dueDate = Calendar.getInstance()
                    // Set Execution around 05:00:00 AM
                    dueDate.set(Calendar.HOUR_OF_DAY, 5)
                    dueDate.set(Calendar.MINUTE, 0)
                    dueDate.set(Calendar.SECOND, 0)
                    if (dueDate.before(currentDate)) {
                        dueDate.add(Calendar.HOUR_OF_DAY, 24)
                    }
                    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
                    Log.d(TAG, "DailyWorker :$timeDiff")
                    val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                        .setConstraints(constraints)
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .addTag(TAG_OUTPUT)
                        .build()

                    WorkManager.getInstance(baseContext).enqueue(dailyWorkRequest)
                }

                "1" -> {
                    val aWork: WorkRequest = OneTimeWorkRequestBuilder<AWork>()
                        .addTag("普通：" + TimeUtils.getNowString())
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(aWork)
                }

                "5" -> {
                    val bWork = OneTimeWorkRequestBuilder<BWork>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .addTag("加急：" + TimeUtils.getNowString())
                        .build()
                    WorkManager.getInstance(baseContext).enqueue(bWork)

                }

                "6" -> {
                    // 定期任务 可以定义的最短重复间隔是 15 分钟
                    val cWork1 = PeriodicWorkRequestBuilder<CWork>(15, TimeUnit.MINUTES)
                        .addTag("定期>固定周期：" + TimeUtils.getNowString())
                        .build()
                    WorkManager.getInstance(baseContext).enqueue(cWork1)
                }

                "7" -> {
                    // 重复间隔必须大于或等于 PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS，
                    // 而灵活间隔必须大于或等于 PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS。
                    val cWork2 = PeriodicWorkRequestBuilder<CWork>(
                        15, TimeUnit.MINUTES, // 重复间隔
                        5, TimeUnit.MINUTES// 而灵活间
                    )
                        .addTag("定期+灵活周期：" + TimeUtils.getNowString())
                        .build()
                    WorkManager.getInstance(baseContext).enqueue(cWork2)
                }

                "9" -> {
                    // 超长任务
                    val dWork = PeriodicWorkRequestBuilder<DWork>(15, TimeUnit.MINUTES)
                        .addTag("超长任务：" + TimeUtils.getNowString())
                        .build()
                    WorkManager.getInstance(baseContext).enqueue(dWork)
                }

                "10" -> {
                    val aWork3: WorkRequest = OneTimeWorkRequestBuilder<AWork>()
                        .addTag("延迟一次性工作：" + TimeUtils.getNowString())
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(aWork3)
                }

                "11" -> {
                    val mBuilder = Constraints.Builder()
                    mBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
                    mBuilder.setRequiresBatteryNotLow(true)// 电量不能太低
                    mBuilder.setRequiresStorageNotLow(true)//  存储不能太少
                    mBuilder.setRequiresCharging(true)// 充电
                    mBuilder.setRequiresDeviceIdle(true)// 设置空闲
                    val constraints = mBuilder.build()
                    val aWork4: WorkRequest = OneTimeWorkRequestBuilder<AWork>()
                        .addTag("约束一次性工作：" + TimeUtils.getNowString())
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(aWork4)
                }

                "12" -> {
                    val mBuilder = Constraints.Builder()
                    mBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
                    mBuilder.setRequiresBatteryNotLow(true)// 电量不能太低
                    mBuilder.setRequiresStorageNotLow(true)//  存储不能太少
                    mBuilder.setRequiresCharging(true)// 充电
                    mBuilder.setRequiresDeviceIdle(true)// 设置空闲
                    val constraints = mBuilder.build()
                    val dWork = PeriodicWorkRequestBuilder<DWork>(15, TimeUnit.MINUTES)
                        .addTag("定期约束条件：" + TimeUtils.getNowString())
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(baseContext).enqueue(dWork)
                }

                "13" -> {
                    val mBuilder = Constraints.Builder()
                    mBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
                    val constraints = mBuilder.build()
                    val eWork: WorkRequest = OneTimeWorkRequestBuilder<EWork>()
                        .addTag("工作监听：" + TimeUtils.getNowString())
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(eWork)
                    WorkManager.getInstance(this@WorkActivity)
                        .getWorkInfoByIdLiveData(eWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.w(TAG, "工作监听 t:" + t.toString())
                            }
                        })

                }

                "14" -> {
                    val mBuilder = Constraints.Builder()
                    mBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
                    val constraints = mBuilder.build()
                    val mWork: WorkRequest = OneTimeWorkRequestBuilder<FWork>()
                        .addTag("重试工作 linear：" + TimeUtils.getNowString())
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            WorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(mWork)
                    WorkManager.getInstance(this@WorkActivity)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.e(TAG, "工作监听 t:" + t.toString())
                            }
                        })
                }

                "15" -> {
                    val mBuilder = Constraints.Builder()
                    mBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
                    val constraints = mBuilder.build()
                    val mWork: WorkRequest = OneTimeWorkRequestBuilder<FWork>()
                        .addTag("重试工作 exponential：" + TimeUtils.getNowString())
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            WorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(mWork)
                    WorkManager.getInstance(this@WorkActivity)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.e(TAG, "工作监听 t:" + t.toString())
                            }
                        })
                }

                "16" -> {
                    val output1: Data = Data.Builder()
                        .putString("param", "1111")
                        .putInt("aaa", 123)
                        .build()
                    val output2: Data = workDataOf(
                        "param" to "111",
                        "aaa" to 123
                    )
                    val mWork: WorkRequest = OneTimeWorkRequestBuilder<GWork>()
                        .addTag("入参：" + TimeUtils.getNowString())
                        .setInputData(output2)
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(mWork)
                    WorkManager.getInstance(this@WorkActivity)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.e(TAG, "工作监听 t:" + t.toString())
                            }
                        })
                }

                "17" -> {
                    val mWork = PeriodicWorkRequestBuilder<CWork>(15, TimeUnit.MINUTES)
                        .addTag("唯一工作：" + TimeUtils.getNowString())
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresCharging(true)
                                .build()
                        )
                        .build()
                    WorkManager.getInstance(baseContext).enqueueUniquePeriodicWork(
                        "bbbbb",
                        ExistingPeriodicWorkPolicy.KEEP,
                        mWork
                    )
                    WorkManager.getInstance(baseContext)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.e(TAG, "工作监听 t:" + t.toString())
                            }
                        })
                }

                "18" -> {
                    val getWork =
                        WorkManager.getInstance(baseContext).getWorkInfosForUniqueWork("sync")
                    Log.w(TAG, "手动获取某个工作状态信息" + getWork.toString())
                }
            }
        }
    }
}