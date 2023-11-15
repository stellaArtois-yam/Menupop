package com.example.menupop

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class LoginModel  {
    private val TAG = "LoginModel"
    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

     suspend fun requestLogin(id:String, password:String) : LoginResponseModel=


         withContext(Dispatchers.IO) {
             Log.d(TAG, "requestLogin: 호출")
             // 네트워크 요청을 IO 스레드에서 수행
             val call: Call<LoginResponseModel> = service.requestLogin(id, password)
             return@withContext try {
                 val response = call.execute()
                 Log.d(TAG, "requestLogin: ${response}")
                 if (response.isSuccessful) {
                     response.body() ?: LoginResponseModel(0, 0,"failed")
                 } else {
                     LoginResponseModel(0, 0,"failed")
                 }
             } catch (e: Exception) {
                 Log.d(TAG, "requestLogin: ${e}")
                 // 네트워크 호출 중 예외 발생 시
                 LoginResponseModel(0, 0,"failed")
             }
         }

    }

