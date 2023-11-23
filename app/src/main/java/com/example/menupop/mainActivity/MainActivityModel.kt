package com.example.menupop.mainActivity

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

class MainActivityModel {
    val TAG = "MainActivityModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    fun getUserInfo(sharedPreferences: SharedPreferences) : Int {
        val identifier = sharedPreferences.getInt("identifier", 0)

        return identifier
    }

    fun requestUserInformation(identifier : Int, callback: (UserInformationData) -> Unit){
        val call : Call<UserInformationData> = service.requestUserInformation(identifier)

        call.enqueue(object : Callback<UserInformationData>{
            override fun onResponse(call: Call<UserInformationData>, response: Response<UserInformationData>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "isNotSuccessful: ${response.body()}")

                }
            }

            override fun onFailure(call: Call<UserInformationData>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }
}