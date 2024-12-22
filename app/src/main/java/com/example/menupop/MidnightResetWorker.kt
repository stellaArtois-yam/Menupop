package com.example.menupop

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.Exception

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

    private suspend fun requestInitialize(identifier : Int) : String?{
        val gson : Gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_IP)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(RetrofitService::class.java)

        try{
            val response = service.midnightWork(identifier)

            return if(response.isSuccessful){
                Log.d(TAG, "requestInitialize response: ${response.body()}")
                response.body()
            }else{
                Log.d(TAG, "requestInitialize not success: ${response.body()}")
                response.body()
            }


        }catch (e : Exception){
            Log.d(TAG, "requestInitialize network error: ${e.message}")
            return null
        }


    }


}