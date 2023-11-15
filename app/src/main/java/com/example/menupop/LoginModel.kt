package com.example.menupop

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class LoginModel  {
    private val TAG = "LoginModel"
    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)
fun requestLogin(id: String, password: String, callback: (LoginResponseModel) -> Unit) {
    val call: Call<LoginResponseModel> = service.requestLogin(id, password)

    call.enqueue(object : Callback<LoginResponseModel> {
        override fun onResponse(call: Call<LoginResponseModel>, response: Response<LoginResponseModel>) {
            if (response.isSuccessful) {
                callback(response.body()!!)
            } else {
                callback(LoginResponseModel(0, 0, "failed"))
            }
        }

        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
            callback(LoginResponseModel(0, 0, "failed"))
        }
    })
}
    fun saveUserIdentifier(sharedPreferences: SharedPreferences,identifier : Int){
        var editor = sharedPreferences.edit()
        editor.putInt("identifier",identifier)
        editor.apply()
    }
}

