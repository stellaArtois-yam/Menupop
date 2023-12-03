package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.menupop.BuildConfig
import com.example.menupop.KakaoPayRequestModel
import com.example.menupop.KakaoPayResponseModel
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate

class MainActivityModel {
    val TAG = "MainActivityModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    fun getUserInfo(sharedPreferences: SharedPreferences) : Int {
        val identifier = sharedPreferences.getInt("identifier", 0)

        return identifier
    }

    fun requestUserInformation(identifier : Int, callback: (UserInformationData) -> Unit){
        val call : Call<UserInformationData> = service.requestUserInformation(identifier)

        call.enqueue(object : Callback<UserInformationData>{
            override fun onResponse(call: Call<UserInformationData>, response: Response<UserInformationData>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "isNotSuccessful: ${response.body()}")

                }
            }

            override fun onFailure(call: Call<UserInformationData>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private val kakaopay = Retrofit.Builder()
        .baseUrl("https://kapi.kakao.com")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val kakaoPayService = kakaopay.create(RetrofitService::class.java)
    val cid  = "TC0ONETIME"
    val API_KEY = "KakaoAK " + BuildConfig.KAKAOPAY_ADMIN_KEY
    val approvalUrl = "http://3.135.51.201/approve"
    val cancelUrl = "http://3.135.51.201/cancel"
    val failUrl = "http://3.135.51.201/fail"


    @RequiresApi(Build.VERSION_CODES.O)
    val orderId = LocalDate.now().toString() + hashCode().toString() //주문번호


    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId: String, item: String, quantity: String,
        totalAmount: String, callback: (KakaoPayResponseModel) -> Unit){

        Log.d(TAG, "createPaymentRequest: model")

        val requestModel = KakaoPayRequestModel(cid, orderId, userId,
            item, quantity, totalAmount, "0", approvalUrl, cancelUrl, failUrl)


        val call : Call<KakaoPayResponseModel>
        = kakaoPayService.createPaymentRequest(API_KEY, requestModel)


        call.enqueue(object  : Callback<KakaoPayResponseModel>{
            override fun onResponse(call: Call<KakaoPayResponseModel>, response: Response<KakaoPayResponseModel>
            ) {
               if(response.isSuccessful){
                   Log.d(TAG, "onResponse: ${response.body()}")
                   callback(response.body()!!)
               }else{
                   Log.d(TAG, "is not successful: ${response}")
               }
            }

            override fun onFailure(call: Call<KakaoPayResponseModel>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

}