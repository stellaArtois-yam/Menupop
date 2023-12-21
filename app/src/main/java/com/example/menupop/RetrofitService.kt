package com.example.menupop

import com.example.menupop.findId.FindIdResponseDTO
import com.example.menupop.login.LoginResponseModel
import com.example.menupop.mainActivity.exchange.ExchangeRateResponseDTO
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceSearchDTO
import com.example.menupop.mainActivity.profile.KakaoPayApproveResponseDTO
import com.example.menupop.mainActivity.profile.KakaoPayReadyResponseDTO
import com.example.menupop.mainActivity.UserInformationDTO
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

    @POST("checkUserIdDuplication.php")
    @FormUrlEncoded
    fun checkDuplicateId(@Field("userId") id: String)
                        : Call<SimpleResultDTO>


    @POST("Signup.php")
    @FormUrlEncoded
    fun saveUserInformation
                (@Field("id") id: String,
                 @Field("password") password: String,
                 @Field("email") email :String,
                 @Field("identifier") identifier : Int)
                : Call<SimpleResultDTO>

    @POST("GetUserInformation.php")
    @FormUrlEncoded
    fun requestUserInformation(@Field("identifier") identifier: Int)
                                : Call<UserInformationDTO>

    @POST("FindId.php")
    @FormUrlEncoded
    fun requestFindID(@Field("email") email: String): Call<FindIdResponseDTO>


    @POST("SignupCheckEmail.php")
    @FormUrlEncoded
    fun checkEmailExistence(@Field("email") email: String): Call<SimpleResultDTO>

    @POST("login.php")
    @FormUrlEncoded
    fun requestLogin(@Field("id") id: String,
                     @Field("password") password : String)
                    : Call<LoginResponseModel>

    @POST("SendEmailVerifyCode.php")
    @FormUrlEncoded
    fun sendEmailVerifyCode(@Field("email") email: String): Call<String>

    @POST("checkEmail.php")
    @FormUrlEncoded
    fun checkEmail(@Field("email") email : String,
                   @Field("id") id : String)
                    : Call<String>

    @POST("ResetPassword.php")
    @FormUrlEncoded
    fun resetPassword(@Field("id") id : String,
                      @Field("password") password : String)
                        : Call<String>


    @POST("socialLogin.php")
    @FormUrlEncoded
    fun socialLoginRequest(@Field("email") email : String,
                           @Field("identifier") identifier: Int)
                            : Call<LoginResponseModel>


    @POST("socialAccountMergeLocalAccount.php")
    @FormUrlEncoded
    fun socialAccountMergeLocalAccount(@Field("identifier") identifier : Int)
                                        : Call<LoginResponseModel>

    @GET("v6/{authKey}/latest/{baseRate}")
    fun requestExchangeRates(@Path("authKey") authKey:String,
                             @Path("baseRate")baseRate : String)
                            : Call<ExchangeRateResponseDTO>


    @POST("v1/payment/ready")
    @FormUrlEncoded
    fun createPaymentRequest(@Header("Authorization") apiKey : String,
                             @FieldMap map : HashMap<String,String>)
                            : Call<KakaoPayReadyResponseDTO>
    @POST("SavePaymentHistory.php")
    @FormUrlEncoded
    fun savePaymentHistory(@Field("identifier") identifier : Int,
                           @Field("tid") tid : String,
                           @Field("paymentType") paymentType: String,
                           @Field("item") item : String,
                           @Field("price") price : Int,
                           @Field("approvedAt") approvedAt : String,
                           @Field("translationTicket") translationTicket : Int,
                           @Field("foodTicket") foodTicket : Int) : Call<SimpleResultDTO>


    @POST("v1/payment/approve")
    @FormUrlEncoded
    fun requestApprovePayment(@Header("Authorization") apiKey : String,
                              @FieldMap map : HashMap<String, String>)
                                :Call<KakaoPayApproveResponseDTO>
    @GET("searchFood.php")
    fun searchFood(@Query("query") searchText :String) : Call<FoodPreferenceSearchDTO>

    @POST("FoodPreferenceRegister.php")
    @FormUrlEncoded
    fun foodPreferenceRegister(@Field("identifier") identifier: Int , @Field("foodName") foodName : String , @Field("classification") classification : String) : Call<String>
    @POST("GetFoodPreference.php")
    @FormUrlEncoded
    fun getFoodPreference(@Field("identifier") identifier: Int) : Call<FoodPreferenceDataClass>

    @POST("DeleteFoodPreference.php")
    @FormUrlEncoded
    fun deleteFoodPreference(@Field("identifier") identifier: Int,@Field("foodName") foodName: String) : Call<String>
    @POST("MinusFoodTicket.php")
    @FormUrlEncoded
    fun minusFoodTicket(@Field("identifier") identifier: Int) : Call<String>
    @FormUrlEncoded
    @POST("MinusTranslationTicket.php")
    fun minusTranslationTicket(@Field("identifier") identifier: Int) : Call<String>
    @POST("Withdrawal.php")
    @FormUrlEncoded
    fun withdrawal(@Field("identifier") identifier: Int,
                   @Field("email") email: String,
                   @Field("id") id : String,
                   @Field("date") date:String) : Call<String>

    @POST("/")
    @FormUrlEncoded
    fun requestTranslation(@Field("text") text : String,
                           @Field("language") language: String): Call<String>

}
