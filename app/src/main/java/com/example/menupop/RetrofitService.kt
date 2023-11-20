package com.example.menupop

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {

    @POST("checkUserIdDuplication.php")
    @FormUrlEncoded
    fun checkDuplicateId(@Field("userId") id: String): Call<String>

    @POST("Signup.php")
    @FormUrlEncoded
    fun saveUserInformation
                (@Field("id") id: String,
                 @Field("password") password: String,
                 @Field("email") email :String,
                 @Field("identifier") identifier : Int)
    : Call<String>

    @POST("login.php")
    @FormUrlEncoded
    fun requestLogin(@Field("id") id: String,@Field("password") password : String): Call<LoginResponseModel>

    @POST("SendEmailVerifyCode.php")
    @FormUrlEncoded
    fun sendEmailVerifyCode(@Field("email") email: String): Call<String>

    @POST("checkEmail.php")
    @FormUrlEncoded
    fun checkEmail(@Field("email") email : String,@Field("id") id : String) : Call<String>

    @POST("ResetPassword.php")
    @FormUrlEncoded
    fun resetPassword(@Field("id") id : String,@Field("password") password : String) : Call<String>
}