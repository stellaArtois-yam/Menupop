package com.example.menupop.mainActivity.exchange

import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ExchangeModel {
    companion object{
        const val TAG = "ExchangeModel"
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val exchangeBuilder = Retrofit.Builder()
        .baseUrl("https://v6.exchangerate-api.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()


    private val service = exchangeBuilder.create(RetrofitService::class.java)
    suspend fun requestExchangeRate(
        authKey: String,
        baseRate: String) : Result<ExchangeRateResponseDTO>{
        return withContext(Dispatchers.IO){
            try{
                val response = service.requestExchangeRates(authKey,baseRate)
                if(response.isSuccessful && response.body() != null){
                    Result.success(response.body()!!)
                }else{
                    Result.failure(Exception("API 호출 실패: ${response.message()}"))
                }
            }catch (e : Exception){
                Result.failure(e)
            }
        }
    }
}