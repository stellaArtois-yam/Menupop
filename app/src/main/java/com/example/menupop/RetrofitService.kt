package com.example.menupop

import com.example.menupop.findId.FindIdResponseDTO
import com.example.menupop.login.LoginResponseModel
import com.example.menupop.mainActivity.exchange.ExchangeRateResponseDTO
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceSearchDTO
import com.example.menupop.mainActivity.profile.KakaoPayApproveResponseDTO
import com.example.menupop.mainActivity.profile.KakaoPayReadyResponseDTO
import com.example.menupop.mainActivity.UserInformationDTO
import com.example.menupop.mainActivity.profile.KakaoPayCancelResponseDTO
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

    @POST("CheckUserIdDuplication.php")
    @FormUrlEncoded
    suspend fun checkDuplicateId(@Field("userId") id: String): Response<SimpleResultDTO>


    @POST("Signup.php")
    @FormUrlEncoded
    suspend fun saveUserInformation(
        @Field("id") id: String,
        @Field("password") password: String,
        @Field("salt") salt : String,
        @Field("email") email: String,
        @Field("identifier") identifier: Int
    ): Response<SimpleResultDTO>

    @POST("GetUserInformation.php")
    @FormUrlEncoded
    suspend fun requestUserInformation(@Field("identifier") identifier: Int): UserInformationDTO

    @POST("FindId.php")
    @FormUrlEncoded
    suspend fun requestFindID(@Field("email") email: String): Response<FindIdResponseDTO>


    @POST("SignupCheckEmail.php")
    @FormUrlEncoded
    suspend fun checkEmailExistence(@Field("email") email: String): Response<SimpleResultDTO>

    @POST("Login.php")
    @FormUrlEncoded
    suspend fun requestLogin(
        @Field("id") id: String,
        @Field("password") password: String
    ): Response<LoginResponseModel>
    @POST("GetSalt.php")
    @FormUrlEncoded
    suspend fun getSalt(
        @Field("id") id : String
    ) : Response<SaltDTO>

    @POST("SendEmailVerifyCode.php")
    @FormUrlEncoded
    suspend fun sendEmailVerifyCode(@Field("email") email: String): Response<String>

    @POST("CheckEmail.php")
    @FormUrlEncoded
    suspend fun checkEmail(
        @Field("email") email: String,
        @Field("id") id: String
    ): Response<String>

    @POST("ResetPassword.php")
    @FormUrlEncoded
    suspend fun resetPassword(
        @Field("id") id: String,
        @Field("password") password: String,
        @Field("salt") salt : String
    ): Response<String>


    @POST("SocialLogin.php")
    @FormUrlEncoded
    suspend fun socialLoginRequest(
        @Field("email") email: String,
        @Field("identifier") identifier: Int
    ): Response<LoginResponseModel>


    @POST("SocialAccountMergeLocalAccount.php")
    @FormUrlEncoded
    suspend fun socialAccountMergeLocalAccount(@Field("identifier") identifier: Int): Response<LoginResponseModel>

    @GET("v6/{authKey}/latest/{baseRate}")
    suspend fun requestExchangeRates(
        @Path("authKey") authKey: String,
        @Path("baseRate") baseRate: String
    ): Response<ExchangeRateResponseDTO>


    @POST("v1/payment/ready")
    @FormUrlEncoded
    suspend fun createPaymentRequest(
        @Header("Authorization") apiKey: String,
        @FieldMap map: HashMap<String, String>
    ): Response<KakaoPayReadyResponseDTO>

    @POST("SavePaymentHistory.php")
    @FormUrlEncoded
    suspend fun savePaymentHistory(
        @Field("identifier") identifier: Int,
        @Field("tid") tid: String,
        @Field("paymentType") paymentType: String,
        @Field("item") item: String,
        @Field("price") price: Int,
        @Field("approvedAt") approvedAt: String,
        @Field("translationTicket") translationTicket: Int,
        @Field("foodTicket") foodTicket: Int
    ): Response<SimpleResultDTO>


    @POST("v1/payment/approve")
    @FormUrlEncoded
    suspend fun requestApprovePayment(
        @Header("Authorization") apiKey: String,
        @FieldMap map: HashMap<String, String>
    ): Response<KakaoPayApproveResponseDTO>

    @POST("v1/payment/cancel")
    @FormUrlEncoded
    suspend fun requestCancelPayment(
        @Header("Authorization") apiKey: String,
        @FieldMap map: HashMap<String, String>
    ): Response<KakaoPayCancelResponseDTO>


    @GET("searchFood.php")
    suspend fun searchFood(@Query("query") searchText: String): Response<FoodPreferenceSearchDTO>

    @POST("FoodPreferenceRegister.php")
    @FormUrlEncoded
    suspend fun foodPreferenceRegister(
        @Field("identifier") identifier: Int,
        @Field("foodName") foodName: String,
        @Field("classification") classification: String
    ): Response<String>

    @POST("GetFoodPreference.php")
    @FormUrlEncoded
    suspend fun getFoodPreference(@Field("identifier") identifier: Int): Response<FoodPreferenceDataClass>

    @POST("DeleteFoodPreference.php")
    @FormUrlEncoded
    suspend fun deleteFoodPreference(
        @Field("identifier") identifier: Int,
        @Field("foodName") foodName: String
    ): Response<String>


    @POST("Withdrawal.php")
    @FormUrlEncoded
    suspend fun withdrawal(
        @Field("identifier") identifier: Int,
        @Field("email") email: String,
        @Field("id") id: String,
        @Field("date") date: String
    ): Response<String>


    @POST("UpdateTicketQuantity.php")
    @FormUrlEncoded
    suspend fun updateTicketQuantity(
        @Field("identifier") identifier: Int,
        @Field("ticketType") ticketType: String,
        @Field("operator") operator: String,
        @Field("quantity") quantity: Int
    ): Response<String>

    @POST("UpdateRewardQuantity.php")
    @FormUrlEncoded
    suspend fun updateRewardQuantity(@Field("identifier") identifier: Int): Response<String>

    @POST("MidnightWork.php")
    @FormUrlEncoded
    suspend fun midnightWork(@Field("identifier") identifier: Int): Response<String>


    @POST("/")
    @FormUrlEncoded
    suspend fun requestTranslation(
        @Field("text") text: String,
        @Field("language") language: String
    ): Response<String>
    @POST("UseTranslationTicket.php")
    @FormUrlEncoded
    suspend fun useTranslationTicket(
        @Field("identifier") identifier : Int) : Response<String>
}
