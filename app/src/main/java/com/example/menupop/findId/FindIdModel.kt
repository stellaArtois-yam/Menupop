package com.example.menupop.findId

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

class FindIdModel {

    val TAG = "FindModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    fun checkUserId(email : String, callback : (FindIdResponseModel) -> Unit){
        val call : Call<FindIdResponseModel> = service.requestFindID(email)

        call.enqueue(object : Callback<FindIdResponseModel> {
            override fun onResponse(call: Call<FindIdResponseModel>, response: Response<FindIdResponseModel>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(FindIdResponseModel("failed", "none"))
                }
            }
            override fun onFailure(call: Call<FindIdResponseModel>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback(FindIdResponseModel("failed", "none"))
            }
        })
    }


}