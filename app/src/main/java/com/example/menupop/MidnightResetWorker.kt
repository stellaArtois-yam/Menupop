package com.example.menupop

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MidnightResetWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    val TAG = "WorkManager"
    override suspend fun doWork(): Result = coroutineScope {
        val sharedPreferences =
            applicationContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if(isMidnight()){
            editor.putInt("dailyTranslation", 3) //초기화 값
            editor.putInt("dailyReward", 3)
            editor.putInt("rewarded", 0)
            editor.commit()


            Log.d(TAG, "몇 번이나 실행되는겨")
        }

        Result.success()
    }

    private fun isMidnight(): Boolean {
        val currentTime = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19) // 자정
            set(Calendar.MINUTE, 14)
            set(Calendar.SECOND, 0)
        }


        return currentTime.get(Calendar.HOUR_OF_DAY) == midnight.get(Calendar.HOUR_OF_DAY)
                && currentTime.get(Calendar.MINUTE) == midnight.get(Calendar.MINUTE)
                && currentTime.get(Calendar.SECOND) == midnight.get(Calendar.SECOND)
    }
}