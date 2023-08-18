package com.example.zzt.zworkmanager.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.zzt.zworkmanager.R

class BTestActivity : AppCompatActivity() {
    val TAG = "Work-B"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btest)

        Log.d(TAG, "onNewIntent id ${intent?.extras?.getInt("id")}")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent id ${intent?.extras?.getInt("id")}")
    }

}