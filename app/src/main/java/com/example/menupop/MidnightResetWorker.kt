package com.example.menupop

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class MidnightResetWorker(context: Context,
                          workerParameters: WorkerParameters) : Worker(context, workerParameters){
    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("dailyTranslation", 3) //초기화 값
        editor.putInt("dailyReword", 3)
        editor.apply()

        return Result.success()
    }
}