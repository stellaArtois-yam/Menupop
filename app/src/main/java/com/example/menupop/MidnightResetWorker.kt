package com.example.menupop

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MidnightResetWorker(context: Context,
                          workerParameters: WorkerParameters) : Worker(context, workerParameters){

    val TAG = "WorkManager"
    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("dailyTranslation", 3) //초기화 값
        editor.putInt("dailyReword", 3)
        editor.apply()

        Log.d(TAG, "doWork: ??")

        return Result.success()
    }
}