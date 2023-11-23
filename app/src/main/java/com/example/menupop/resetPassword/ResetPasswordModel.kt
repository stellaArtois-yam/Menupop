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
                    Log.d(TAG, "onResponse: ${response.body()}")
                }else{
                    callback("ServerException")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback("ServerException")
            }

        })

    }
    fun checkEamil(id:String,email:String,callback: (String) -> Unit){
        Log.d(TAG, "checkEamil: 호출됨")
        service.checkEmail(email,id).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "onResponse: ${response}")
                if(response.isSuccessful && response.body() != null){
                    callback(response.body().toString())
                    Log.d(TAG, "onResponse: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
                callback("Exception")
            }

        })
    }
    fun sendVerifyCode(email: String,callback: (String) -> Unit){
        Log.d(TAG, "sendVerifyCode: ${email}")
        service.sendEmailVerifyCode(email).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null){
                    callback(response.body().toString())
                    Log.d(TAG, "onResponse: ${response}")
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

    fun resetPassword(id:String,password:String,callback: (String) -> Unit){
        Log.d(TAG, "resetPassword 호출 ${id} ${password} ")
        service.resetPassword(id,password).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "onResponse: ${response}")
                if(response.isSuccessful && response.body() != null){
                    callback("완료")
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