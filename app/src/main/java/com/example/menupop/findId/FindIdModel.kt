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

    fun checkUserId(email : String, callback : (FindIdResponseDTO) -> Unit){
        val call : Call<FindIdResponseDTO> = service.requestFindID(email)

        call.enqueue(object : Callback<FindIdResponseDTO> {
            override fun onResponse(call: Call<FindIdResponseDTO>, response: Response<FindIdResponseDTO>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(FindIdResponseDTO("failed", "none"))
                }
            }
            override fun onFailure(call: Call<FindIdResponseDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback(FindIdResponseDTO("failed", "none"))
            }
        })
    }


}