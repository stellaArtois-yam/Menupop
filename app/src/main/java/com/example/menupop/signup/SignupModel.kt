package com.example.menupop.signup

import com.example.menupop.BuildConfig
import com.example.menupop.SimpleResultDTO
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class SignupModel {
    companion object{
        const val TAG = "SignupModel"
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

    suspend fun requestIdDuplication(id : String): SimpleResultDTO{
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

    suspend fun sendUserInformation(id : String,
                                    password : String,
                                    salt : String,
                                    email :String,
                                    identifier : Int) : SimpleResultDTO{
        return withContext(Dispatchers.IO){
            try{
                val response = service.saveUserInformation(id, password, salt, email, identifier)
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

    suspend fun sendVerifyCode(email: String) : String{
        return withContext(Dispatchers.IO){
            try{
                val response = service.sendEmailVerifyCode(email)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    response.code().toString()
                }
            }catch (e : Exception){
                "onFailure: ${e.message}"
            }
        }
    }

    suspend fun checkEmailExistence(email:String) : SimpleResultDTO{
        return withContext(Dispatchers.IO){
            try{
                val response = service.checkEmailExistence(email)
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
}