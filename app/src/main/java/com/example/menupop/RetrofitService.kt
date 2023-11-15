package com.example.menupop

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {

    @POST("checkDuplicationId.php")
    fun checkDuplicateId(@Query("id") id: String): Call<Boolean>
    @POST("login.php")
    @FormUrlEncoded
    fun requestLogin(@Field("id") id: String,@Field("password") password : String): Call<LoginResponseModel>
}