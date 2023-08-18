package com.example.zzt.zworkmanager

import android.app.Application
import androidx.work.Configuration
import java.util.concurrent.Executors

/**
 * @author: zeting
 * @date: 2023/8/15
 */
class MyApplication : Application()
//    , Configuration.Provider
{
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

//    override fun getWorkManagerConfiguration(): Configuration {
//        return Configuration.Builder()
//            .setExecutor(Executors.newFixedThreadPool(2))
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//    }


}