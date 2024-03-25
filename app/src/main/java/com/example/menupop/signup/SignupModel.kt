package com.example.menupop.signup

import android.util.Log
import com.example.menupop.BuildConfig
import com.example.menupop.SimpleResultDTO
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class SignupModel {
    val TAG = "SignupModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_IP)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)



    fun requestIdDuplication(id : String, callback: (SimpleResultDTO)-> Unit){
        val call: Call<SimpleResultDTO> = service.checkDuplicateId(id)

        call.enqueue(object  : Callback<SimpleResultDTO>{
            override fun onResponse(call: Call<SimpleResultDTO>, response: Response<SimpleResultDTO>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(SimpleResultDTO("isNotSuccessful"))
                }
            }

            override fun onFailure(call: Call<SimpleResultDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback(SimpleResultDTO("onFailure"))
            }
        })
    }

    fun sendUserInformation(id : String, password : String, email :String,
                            identifier : Int, callback: (SimpleResultDTO) -> Unit){
        val call : Call<SimpleResultDTO> = service.saveUserInformation(id, password, email, identifier)
        Log.d(TAG, "$id, $password, $email, $identifier")
        call.enqueue(object  : Callback<SimpleResultDTO>{
            override fun onResponse(call: Call<SimpleResultDTO>, response: Response<SimpleResultDTO>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "onResponse: ${response}")
                    callback(SimpleResultDTO("!isSuccessful"))
                }
            }

            override fun onFailure(call: Call<SimpleResultDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback(SimpleResultDTO("onFailure"))
            }
        })
    }




    fun sendVerifyCode(email: String,callback: (String) -> Unit){
        Log.d(TAG, "sendVerifyCode: ${email}")
        service.sendEmailVerifyCode(email).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null){
                    callback(response.body().toString())
                    Log.d(TAG, "onResponse: ${response}")
                } else{
                    callback("Exception")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback("Exception")
                Log.d(TAG, "onFailure: ${t}")
            }

        })
    }

    fun checkEmailExistence(email:String,callback: (SimpleResultDTO) -> Unit){

        service.checkEmailExistence(email).enqueue(object : Callback<SimpleResultDTO>{

            override fun onResponse(call: Call<SimpleResultDTO>, response: Response<SimpleResultDTO>) {

                if(response.isSuccessful && response.body() != null){
                    callback(response.body()!!)
                    Log.d(TAG, "onResponse: ${response.body()}")
                }else{
                    Log.d(TAG, "!isSuccessful: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<SimpleResultDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
                callback(SimpleResultDTO("Exception"))
            }

        })
    }
}