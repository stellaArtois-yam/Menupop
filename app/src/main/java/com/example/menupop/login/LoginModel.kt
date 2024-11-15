package com.example.menupop.login

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.example.menupop.BuildConfig
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class LoginModel(val application: Application) {
    companion object{
        const val TAG = "LoginModel"
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
    suspend fun requestLogin(id: String, password: String) : LoginResponseModel{
        return withContext(Dispatchers.IO) {
            try {
                val response = service.requestLogin(id, password)
                response
            } catch (e: Exception) {
                LoginResponseModel(0, 0, "failed")
            }
        }
    }

    suspend fun socialLoginRequest(email: String) : LoginResponseModel {
        return withContext(Dispatchers.IO){
            try{
                val response = service.socialLoginRequest(email, email.hashCode())
                Log.d(TAG, "socialLoginRequest: ${response.body()}")
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    LoginResponseModel(0, 0, "failed")
                }
            }catch (e: Exception){
                LoginResponseModel(0, 0, "failed")
            }
        }
    }

    fun saveUserIdentifier(sharedPreferences: SharedPreferences, identifier: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("identifier", identifier)
        editor.apply()
    }

    suspend fun socialAccountMergeLocalAccount(identifier: Int) : LoginResponseModel{
        return withContext(Dispatchers.IO){
            try{
                val response = service.socialAccountMergeLocalAccount(identifier)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    LoginResponseModel(0, 0, "failed")
                }
            }catch (e: Exception){
                LoginResponseModel(0, 0, "failed")
            }
        }
    }

    fun requestNaverSocialLogin(oauthLoginCallback: OAuthLoginCallback) {
        NaverIdLoginSDK.authenticate(application.applicationContext, oauthLoginCallback)
    }


}

