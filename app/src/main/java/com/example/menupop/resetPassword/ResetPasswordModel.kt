package com.example.menupop.resetPassword

import android.util.Log
import com.example.menupop.BuildConfig
import com.example.menupop.RetrofitService
import com.example.menupop.SimpleResultDTO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ResetPasswordModel {
    companion object{
        const val TAG = "ResetPasswordModel"
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


    suspend fun checkId(id:String) : SimpleResultDTO{
        return withContext(Dispatchers.IO){
            try{
                val response = service.checkDuplicateId(id)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    SimpleResultDTO("error : ${response.code()}")
                }
            }catch (e : Exception){
               SimpleResultDTO("error : ${e.message}")
            }
        }


    }
    suspend fun checkEmail(id:String,email:String) : String{
        return withContext(Dispatchers.IO){
            try{
                val response = service.checkEmail(email, id)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    response.code().toString()
                }
            }catch (e : Exception){
                e.message!!
            }
        }
    }
    suspend fun sendVerifyCode(email: String) : String{
        return withContext(Dispatchers.IO) {
            try {
                val response = service.sendEmailVerifyCode(email)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    response.code().toString()
                }
            } catch (e: Exception) {
                "onFailure: ${e.message}"
            }
        }
    }

    suspend fun resetPassword(id:String, password:String, salt : String) :String{
        return withContext(Dispatchers.IO){
            try {
                val response = service.resetPassword(id, password, salt)
                Log.d(TAG, "resetPassword: ${response.body()}")
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    response.code().toString()
                }
            }catch (e :Exception){
                "onFailure: ${e.message}"
            }
        }
    }

}