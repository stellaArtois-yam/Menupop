package com.example.menupop

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.Exception
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.log

class MidnightResetWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    val TAG = "WorkManager"


    override suspend fun doWork(): Result = coroutineScope {

        val sharedPreferences = applicationContext.getSharedPreferences("userInfo", MODE_PRIVATE)
        val identifier = sharedPreferences.getInt("identifier", 0)
        Log.d(TAG, "doWork identifier: $identifier")

            withContext(Dispatchers.IO){
                requestInitialize(identifier)
            }


        Result.success()
    }

    suspend fun requestInitialize(identifier : Int) : String?{
        val gson : Gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.135.51.201/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(RetrofitService::class.java)

        try{
            val response = service.midnightWork(identifier).awaitResponse()

            if(response.isSuccessful){
                Log.d(TAG, "requestInitialize response: ${response.body()}")
                return response.body()
            }else{
                Log.d(TAG, "requestInitialize not success: ${response.body()}")
                return response.body()
            }


        }catch (e : Exception){
            Log.d(TAG, "requestInitialize network error: ${e.message}")
            return null
        }


    }


}