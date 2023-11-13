package com.example.menupop

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("checkUserIdDuplication.php")
    suspend fun checkUserIdDuplication(@Query("userId") userId: String): Response<SignupResponseModel>

}