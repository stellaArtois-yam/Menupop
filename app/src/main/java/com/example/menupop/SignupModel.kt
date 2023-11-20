package com.example.menupop

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.math.log

class SignupModel {
    val TAG = "SignupModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)



    fun requestIdDuplication(id : String, callback: (String)-> Unit){
        val call: Call<String> = service.checkDuplicateId(id)

        call.enqueue(object  : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback("!isSuccessful")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback("onFailure")
            }
        })
    }

    fun sendUserInformation(id : String, password : String, email :String,
                            identifier : Int, callback: (String) -> Unit){
        val call : Call<String> = service.saveUserInformation(id, password, email, identifier)
        Log.d(TAG, "Model $id, $password, $email, $identifier")
        call.enqueue(object  : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response}")
                    callback("!isSuccessful")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback("onFailure")
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
}