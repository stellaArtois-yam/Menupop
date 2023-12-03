package com.example.menupop

import com.example.menupop.findId.FindIdResponseModel
import com.example.menupop.login.LoginResponseModel
import com.example.menupop.mainActivity.ExchangeRateDataClass
import com.example.menupop.mainActivity.UserInformationData
import com.example.menupop.signup.ResultModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitService {

    @POST("checkUserIdDuplication.php")
    @FormUrlEncoded
    fun checkDuplicateId(@Field("userId") id: String): Call<ResultModel>

    @POST("Signup.php")
    @FormUrlEncoded
    fun saveUserInformation
                (@Field("id") id: String,
                 @Field("password") password: String,
                 @Field("email") email :String,
                 @Field("identifier") identifier : Int)
    : Call<ResultModel>

    @POST("GetUserInformation.php")
    @FormUrlEncoded
    fun requestUserInformation(@Field("identifier") identifier: Int) : Call<UserInformationData>

    @POST("FindId.php")
    @FormUrlEncoded
    fun requestFindID(@Field("email") email: String): Call<FindIdResponseModel>

    @POST("SignupCheckEmail.php")
    @FormUrlEncoded
    fun checkEmailExistence(@Field("email") email: String): Call<ResultModel>

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

    @GET("v6/{authKey}/latest/{baseRate}")
    fun requestExchangeRates(@Path("authKey") authKey:String,@Path("baseRate")baseRate : String) : Call<ExchangeRateDataClass>


    @POST("v1/payment/ready")
    @Headers("Content-Type: application/json")
    @FormUrlEncoded
    fun createPaymentRequest(@Header("Authorization") apiKey: String,
                             @FieldMap kakaoPayRequestModel: KakaoPayRequestModel )  //실패 시 redirect url
                            : Call<KakaoPayResponseModel>

    @POST("v1/payment/approve")
    @FormUrlEncoded
    fun requestApprovePayment(@Header("Authorization") apiKey : String,
                              @Field("cid") cid : String,
                              @Field("tid") tid : String,
                              @Field("partner_order_id") orderId : String,
                              @Field("partner_user_id") userId : String,
                              @Field("pg_token") pgToken : String)


}