package com.example.menupop.findId

import com.example.menupop.BuildConfig
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class FindIdModel {
   companion object{
       const val TAG = "FindModel"
   }

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_IP)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    suspend fun checkUserId(email : String) : FindIdResponseDTO{
        return withContext(Dispatchers.IO){
            try{
                val response = service.requestFindID(email)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    FindIdResponseDTO(response.code().toString(), "N/A")
                }
            }catch (e : Exception){
                FindIdResponseDTO(e.message!!, "N/A")
            }
        }
    }

}