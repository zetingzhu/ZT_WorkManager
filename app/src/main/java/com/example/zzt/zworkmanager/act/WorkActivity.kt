package com.example.zzt.zworkmanager.act

import android.app.PendingIntent
import android.content.Intent
import android.database.Observable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ArrayCreatingInputMerger
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.OverwritingInputMerger
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
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
import com.example.zzt.zworkmanager.work.H1Work
import com.example.zzt.zworkmanager.work.HWork
import com.example.zzt.zworkmanager.work.IWork
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

/**
 *
 * enqueued|加入队列
running|运行中
succeeded|已成功
failed|失败
blocked|挂起
cancelled|取消
exponential|倍数增加 eg:result.retry() 结束并在 10 秒后重试,接下来依次 20、40、80 秒
linear|线性增加 eg:result.retry() 结束并在 10 秒后重试,接下来依次 20 、30 、40 秒

 *
 * @constructor
 */
class WorkActivity : AppCompatActivity() {
    val TAG = "WorkActivity"
    private lateinit var binding: ActivityWorkBinding

    var notifyId = 111

    var workManager: WorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        workManager = WorkManager.getInstance(baseContext)

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
        mListDialog.add(StartActivityDao("唯一工作", "循环工作", "17"))
        mListDialog.add(StartActivityDao("手动获取循环工作", "", "18"))
        mListDialog.add(StartActivityDao("唯一工作", "一次性", "19"))
        mListDialog.add(StartActivityDao("手动获取一次性工作状态信息", "", "20"))
        mListDialog.add(StartActivityDao("查找已经创建的工作", "", "21"))
        mListDialog.add(StartActivityDao("取消停止工作", "", "22"))
        mListDialog.add(StartActivityDao("开启进度工作", "", "23"))
        mListDialog.add(StartActivityDao("监听进度工作进度值", "", "24"))
        mListDialog.add(StartActivityDao("监听进度工作进度值", "异步", "25"))
        mListDialog.add(StartActivityDao("链接任务", "1,5,3同时执行,4,2在后面执行", "26"))
        mListDialog.add(StartActivityDao("工作链接输入合并", "OverwritingInputMerger ", "27"))
        mListDialog.add(StartActivityDao("工作链接输入合并", "ArrayCreatingInputMerger ", "28"))
        mListDialog.add(StartActivityDao("合并两个工作链，到一起", "", "29"))
        mListDialog.add(StartActivityDao("工作链唯一", "", "30"))
        mListDialog.add(StartActivityDao("源码解读", "", "31"))


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
                    WorkManager.getInstance(baseContext).enqueueUniquePeriodicWork(
                        "写入本地唯一",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        cWork1
                    )
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
                        .addTag("唯一工作,循环：" + TimeUtils.getNowString())
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresCharging(true)
                                .build()
                        )
                        .build()
                    WorkManager.getInstance(baseContext).enqueueUniquePeriodicWork(
                        "bbbbb",
                        ExistingPeriodicWorkPolicy.UPDATE,
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
                    val getWork = WorkManager.getInstance(baseContext)
                        .getWorkInfosForUniqueWorkLiveData("bbbbb")
                    Log.w(TAG, "手动获取某个工作状态信息" + getWork.toString())
                    getWork.observe(this, object : Observer<List<WorkInfo>> {
                        override fun onChanged(t: List<WorkInfo>?) {
                            Log.w(TAG, "工作监听 18:" + t.toString())
                        }
                    })
                }

                "19" -> {
                    val mWork = OneTimeWorkRequestBuilder<AWork>()
                        .addTag("唯一工作，一次性")
                        .build()
                    WorkManager.getInstance(baseContext).enqueueUniqueWork(
                        "aaaaa",
                        ExistingWorkPolicy.REPLACE,
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

                "20" -> {
                    val getWork =
                        WorkManager.getInstance(baseContext)
                            .getWorkInfosForUniqueWorkLiveData("aaaaa")
                    Log.w(TAG, "手动获取某个工作状态信息" + getWork.toString())
                    getWork.observe(this, object : Observer<List<WorkInfo>> {
                        override fun onChanged(t: List<WorkInfo>?) {
                            Log.w(TAG, "工作监听 20:" + t.toString())
                        }
                    })
                }

                "21" -> {
                    //查找工作
                    val workQuery = WorkQuery.Builder
                        /******************************/
//                        .fromTags(listOf("唯一工作，一次性"))
//                        .addStates(
//                            listOf(
//                                WorkInfo.State.RUNNING,
//                                WorkInfo.State.ENQUEUED,
//                                WorkInfo.State.SUCCEEDED,
//                                WorkInfo.State.FAILED,
//                                WorkInfo.State.CANCELLED,
//                                WorkInfo.State.BLOCKED,
//                            )
//                        )
                        /******************************/
//                        .fromStates(listOf(WorkInfo.State.FAILED, WorkInfo.State.CANCELLED))
                        .fromStates(
                            listOf(
                                WorkInfo.State.RUNNING,
                                WorkInfo.State.ENQUEUED,
                                WorkInfo.State.SUCCEEDED,
                                WorkInfo.State.FAILED,
                                WorkInfo.State.CANCELLED,
                                WorkInfo.State.BLOCKED,
                            )
                        )
                        /******************************/
//                        .addUniqueWorkNames(
//                            listOf("aaaaa", "bbbbb")
//                        )
                        .build()
                    val workInfos = workManager?.getWorkInfosLiveData(workQuery)
                    workInfos?.observe(this, object : Observer<List<WorkInfo>> {
                        override fun onChanged(t: List<WorkInfo>?) {
                            Log.w(TAG, "工作监听 21:" + t.toString())
                        }
                    })
                }

                "22" -> {
                    // 取消停止工作
                    // by id
//                    workManager?.cancelWorkById(syncWorker.id)
                    // by name
//                    workManager?.cancelUniqueWork("sync")
                    // by tag
                    workManager?.cancelAllWorkByTag("唯一工作，一次性")
                }

                "23" -> {
                    val mWork = OneTimeWorkRequestBuilder<HWork>()
                        .addTag("进度工作")
                        .build()
                    WorkManager.getInstance(baseContext).enqueueUniqueWork(
                        "HWork",
                        ExistingWorkPolicy.REPLACE,
                        mWork
                    )
                    WorkManager.getInstance(baseContext)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this, object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                Log.e(TAG, "工作监听 23:" + t.toString())
                            }
                        })
                }

                "24" -> {
                    val mLiveData = workManager?.getWorkInfosByTagLiveData("进度工作")
                    mLiveData?.removeObservers(this)
                    mLiveData?.observe(this) { mWorkInfo ->
                        Log.w(TAG, "工作监听 24:$mWorkInfo")
                        if (mWorkInfo != null) {
                            val progress = mWorkInfo[0].progress
                            Log.w(TAG, "工作监听 24 progress:$progress")
                        }
                    }
                }

                "25" -> {
                    val mWork = OneTimeWorkRequestBuilder<H1Work>()
                        .addTag("进度工作")
                        .build()
                    WorkManager.getInstance(baseContext).enqueueUniqueWork(
                        "HWork",
                        ExistingWorkPolicy.REPLACE,
                        mWork
                    )
                    WorkManager.getInstance(baseContext)
                        .getWorkInfoByIdLiveData(mWork.id)
                        .observe(this) { t -> Log.e(TAG, "工作监听 25:" + t.toString()) }
                }

                "26" -> {
                    val mWork1 =
                        OneTimeWorkRequestBuilder<H1Work>().addTag("链接1").addTag("链接任务")
                            .build()
                    val mWork2 =
                        OneTimeWorkRequestBuilder<H1Work>().addTag("链接2").addTag("链接任务")
                            .build()
                    val mWork3 =
                        OneTimeWorkRequestBuilder<H1Work>().addTag("链接3").addTag("链接任务")
                            .build()
                    val mWork4 =
                        OneTimeWorkRequestBuilder<H1Work>().addTag("链接4").addTag("链接任务")
                            .build()
                    val mWork5 =
                        OneTimeWorkRequestBuilder<H1Work>().addTag("链接5").addTag("链接任务")
                            .build()
                    workManager
                        ?.beginWith(listOf(mWork1, mWork5, mWork3))
                        ?.then(mWork4)
                        ?.then(mWork2)
                        ?.enqueue()

                    val mLiveData = workManager?.getWorkInfosByTagLiveData("链接任务")
                    mLiveData?.removeObservers(this)
                    mLiveData?.observe(this) { mWorkInfo ->
                        Log.w(TAG, "工作监听 26:$mWorkInfo")
                    }
                }

                "27" -> {
                    val mWork1 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接1").addTag("链接结果合并")
                            .setInputMerger(OverwritingInputMerger::class.java)
                            .build()
                    val mWork2 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接2").addTag("链接结果合并")
                            .setInputMerger(OverwritingInputMerger::class.java)
                            .build()
                    val mWork3 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接3").addTag("链接结果合并")
                            .setInputMerger(OverwritingInputMerger::class.java)
                            .build()
                    val mWork4 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接4").addTag("链接结果合并")
                            .setInputMerger(OverwritingInputMerger::class.java)
                            .build()
                    val mWork5 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接5").addTag("链接结果合并")
                            .setInputMerger(OverwritingInputMerger::class.java)
                            .build()
                    workManager
                        ?.beginWith(listOf(mWork1, mWork4))
                        ?.then(mWork3)
                        ?.then(mWork2)
                        ?.enqueue()
                }

                "28" -> {
                    val mWork1 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接1").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork2 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接2").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork3 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接3").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork4 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接4").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork5 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接5").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    workManager
                        ?.beginWith(listOf(mWork1, mWork4))
                        ?.then(mWork3)
                        ?.then(mWork2)
                        ?.then(mWork5)
                        ?.enqueue()
                }

                "29" -> {
                    val mWork1 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接1").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork2 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接2").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork3 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接3").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork4 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接4").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork5 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接5").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    // 组合执行
                    val chain1: WorkContinuation? = workManager?.beginWith(mWork1)?.then(mWork2)
                    val chain2: WorkContinuation? = workManager?.beginWith(mWork3)?.then(mWork4)
                    WorkContinuation
                        .combine(listOf(chain1, chain2))
                        .then(mWork5)
                        .enqueue()
                }

                "30" -> {
                    val mWork1 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接1").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork2 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接2").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork3 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接3").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork4 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接4").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()
                    val mWork5 =
                        OneTimeWorkRequestBuilder<IWork>().addTag("链接5").addTag("链接结果合并")
                            .setInputMerger(ArrayCreatingInputMerger::class.java)
                            .build()

                    workManager?.beginUniqueWork(
                        "唯一工作链",
                        ExistingWorkPolicy.KEEP,
                        listOf(mWork1, mWork4)
                    )
                        ?.then(mWork3)
                        ?.then(mWork2)
                        ?.then(mWork5)
                        ?.enqueue()
                }

                "31" -> {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)// 电量不能太低
                        .setRequiresStorageNotLow(true)//  存储不能太少
                        .setRequiresCharging(true)// 充电
                        .setRequiresDeviceIdle(true)// 设置空闲
                        .build()

                    val mWork: WorkRequest = OneTimeWorkRequestBuilder<IWork>()
                        .addTag("源码解读：" + TimeUtils.getNowString())
                        .setInputData(workDataOf("abc" to 123))
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            WorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                    WorkManager.getInstance(this@WorkActivity).enqueue(mWork)
                }
            }
        }
    }
}