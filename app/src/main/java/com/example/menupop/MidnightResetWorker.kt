package com.example.menupop

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.log

class MidnightResetWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    val TAG = "WorkManager"


    override suspend fun doWork(): Result = coroutineScope {


        val sharedPreferences =
            applicationContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("dailyTranslation", 3) //초기화 값
        editor.putInt("dailyReward", 3)
        editor.putInt("haveReward", 0)
        editor.putInt("todayRewarded", 0)
        editor.commit()


        Log.d(TAG, "몇 번이나 실행되는겨")


        Result.success()
    }


}