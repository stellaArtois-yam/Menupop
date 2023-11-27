package com.example.menupop

import com.example.menupop.findId.FindIdResponseModel
import com.example.menupop.login.LoginResponseModel
import com.example.menupop.mainActivity.ExchangeDataClass
import com.example.menupop.mainActivity.ExchangeModel
import com.example.menupop.mainActivity.UserInformationData
import retrofit2.Call
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

    @POST("GetUserInformation.php")
    @FormUrlEncoded
    fun requestUserInformation(@Field("identifier") identifier: Int) : Call<UserInformationData>

    @POST("CheckUserId.php")
    @FormUrlEncoded
    fun requestFindID(@Field("email") email: String): Call<FindIdResponseModel>

    @POST("SignupCheckEmail.php")
    @FormUrlEncoded
    fun checkEmailExistence(@Field("email") email: String): Call<String>

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
    @POST("socialLogin.php")
    @FormUrlEncoded
    fun socialLoginRequest(@Field("email") email : String,@Field("identifier") identifier: Int) : Call<LoginResponseModel>
    @POST("socialAccountMergeLocalAccount.php")
    @FormUrlEncoded
    fun socialAccountMergeLocalAccount(@Field("identifier") identifier : Int) : Call<LoginResponseModel>
    @GET("exchangeJSON")
//    fun requestExchangeRate(@Query("authkey") authKey:String,@Query("data") data:String) : Call<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>
    fun requestExchangeRate(@Query("authkey") authKey:String,@Query("searchdate") searchDate:String,@Query("data") data:String) : Call<ArrayList<ExchangeDataClass.ExchangeDataClassItem>>

}