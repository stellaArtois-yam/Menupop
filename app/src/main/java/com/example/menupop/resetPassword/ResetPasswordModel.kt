package com.example.menupop.resetPassword

import android.util.Log
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ResetPasswordModel {
    val TAG = "ResetPasswordModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)



    fun checkId(id:String,callback: (String) -> Unit){
        service.checkDuplicateId(id).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    callback(response.body().toString())
                }else{
                    callback("ServerException")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback("ServerException")
            }

        })

    }
    fun sendVerifyCode(email: String,callback: (String) -> Unit){
        service.sendEmailVerifyCode(email).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null){
                    callback(response.body().toString())
                } else{
                    callback("Exception")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback("Exception")
                Log.d(TAG, "onFailure: ${t}")
            }

        })
    }
}