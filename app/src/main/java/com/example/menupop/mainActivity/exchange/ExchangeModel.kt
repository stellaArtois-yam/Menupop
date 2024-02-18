package com.example.menupop.mainActivity.exchange

import android.content.SharedPreferences
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

class ExchangeModel {
    val TAG = "ExchangeModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val exchangeBuilder = Retrofit.Builder()
        .baseUrl("https://v6.exchangerate-api.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()


    private val service = exchangeBuilder.create(RetrofitService::class.java)
    fun requestExchangeRate(
        authKey: String,
        baseRate: String,
        callback: (ExchangeRateResponseDTO) -> Unit){
        service.requestExchangeRates(authKey,baseRate).enqueue(object : Callback<ExchangeRateResponseDTO>{
            override fun onResponse(
                call: Call<ExchangeRateResponseDTO>,
                response: Response<ExchangeRateResponseDTO>
            ) {
                if(response.isSuccessful && response.body() != null){
                    Log.d(TAG, "exchange rate: ${response.body()}")
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponseDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.toString()}")
            }

        })
    }
    fun exchangeRateApplicationStatus(sharedPreferences: SharedPreferences, status : Boolean){
        var editor = sharedPreferences.edit()
        editor.putBoolean("exchangeRateApplicationStatus",status)
        editor.apply()
    }
}