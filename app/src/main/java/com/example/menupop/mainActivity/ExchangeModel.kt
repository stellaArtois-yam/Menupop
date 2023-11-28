package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.util.Log
import com.example.menupop.RetrofitService
import com.example.menupop.login.LoginResponseModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ExchangeModel {
    val TAG = "ExchangeModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://v6.exchangerate-api.com/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)
    fun requestExchangeRate(
        authkey: String,
        baseRate: String,
        callback: (ExchangeRateDataClass) -> Unit){
        service.requestExchangeRates(authkey,baseRate).enqueue(object : Callback<ExchangeRateDataClass>{
            override fun onResponse(
                call: Call<ExchangeRateDataClass>,
                response: Response<ExchangeRateDataClass>
            ) {
                if(response.isSuccessful && response.body() != null){
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ExchangeRateDataClass>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.toString()}")
            }

        })
//        service.requestExchangeRate(authkey,"20231123","AP01")
////        service.requestExchangeRate(authkey,"AP01")
//        .enqueue(object : Callback<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>{
//            override fun onResponse(
//                call: Call<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>,
//                response: Response<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>
//            ) {
//                if(response.isSuccessful){
//                    callback(response.body()!!)
//                }
//            }
//
//            override fun onFailure(
//                call: Call<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>,
//                t: Throwable
//            ) {
//                Log.d(TAG, "onFailure: ${t}")
//            }
//
//        })
    }
    fun exchangeRateApplicationStatus(sharedPreferences: SharedPreferences, status : Boolean){
        var editor = sharedPreferences.edit()
        editor.putBoolean("exchangeRateApplicationStatus",status)
        editor.apply()
    }
}