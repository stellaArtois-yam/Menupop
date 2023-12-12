package com.example.menupop.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.menupop.BuildConfig
import com.example.menupop.KakaoPayApproveResponse
import com.example.menupop.KakaoPayRequestDTO
import com.example.menupop.KakaoPayReadyResponse
import com.example.menupop.MidnightResetWorker
import com.example.menupop.RetrofitService
import com.example.menupop.TicketSaveDTO
import com.example.menupop.signup.ResultModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivityModel {
    val TAG = "MainActivityModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    fun getUserInfo(sharedPreferences: SharedPreferences) : HashMap<String, Int> {
        val hashmap = HashMap<String, Int>()
        val identifier = sharedPreferences.getInt("identifier", 0)
        val dailyTranslation = sharedPreferences.getInt("dailyTranslation", 0)
        val dailyReword = sharedPreferences.getInt("dailyReword", 0)

        hashmap.put("identifier", identifier)
        hashmap.put("dailyTranslation", dailyTranslation)
        hashmap.put("dailyReword", dailyReword)

        return hashmap
    }

    fun minusFoodTicket(identifier: Int,callback: (String) -> Unit){
        service.minusFoodTicket(identifier).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "onResponse:  minusTicket $response")
                if(response.isSuccessful){

                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
                callback("failed")
            }

        })
    }

    fun getFoodPreference(identifier: Int,callback : (FoodPreferenceDataClass)->Unit){
        Log.d(TAG, "getFoodPreference: 호출")
        service.getFoodPreference(identifier).enqueue(object : Callback<FoodPreferenceDataClass>{
            override fun onResponse(
                call: Call<FoodPreferenceDataClass>,
                response: Response<FoodPreferenceDataClass>
            ) {
                Log.d(TAG, "onResponse: ${response}")
                if(response.isSuccessful){
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<FoodPreferenceDataClass>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
            }

        })

    }

    fun savePaymentHistory(ticketSaveModel: TicketSaveDTO, callback: (ResultModel) -> Unit){

        val call : Call<ResultModel> = service.savePaymentHistory(ticketSaveModel.identifier,
            ticketSaveModel.tid, ticketSaveModel.paymentType, ticketSaveModel.item, ticketSaveModel.price,
            ticketSaveModel.approvedAt, ticketSaveModel.translationTicket, ticketSaveModel.foodTicket)


        call.enqueue(object : Callback<ResultModel>{
            override fun onResponse(call: Call<ResultModel>, response: Response<ResultModel>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful: $response")
                }

            }
            override fun onFailure(call: Call<ResultModel>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
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
        .baseUrl("https://kapi.kakao.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val kakaoPayService = kakaopay.create(RetrofitService::class.java)
    val cid  = "TC0ONETIME"
    val API_KEY = "KakaoAK " + BuildConfig.KAKAOPAY_ADMIN_KEY
    val approvalUrl = "http://3.135.51.201/KakaoPayApprove"
    val cancelUrl = "http://3.135.51.201/KakaoPayCancel"
    val failUrl = "http://3.135.51.201/KakaoPayFail"


    @RequiresApi(Build.VERSION_CODES.O)
    val orderId = LocalDate.now().toString().replace("-", "") + hashCode().toString() //주문번호


    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId: String, item: String, quantity: String,
                             totalAmount: String, callback: (KakaoPayReadyResponse) -> Unit){

        val requestModel = HashMap<String, String>()

        val kakaoPayRequestModel = KakaoPayRequestDTO(cid, orderId, userId, item, quantity, totalAmount, "0", approvalUrl, cancelUrl, failUrl)

        val fields = kakaoPayRequestModel.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field.get(kakaoPayRequestModel)?.toString() ?: ""
            requestModel[field.name] = value
        }

        val call : Call<KakaoPayReadyResponse>
                = kakaoPayService.createPaymentRequest(API_KEY, requestModel)

        call.enqueue(object  : Callback<KakaoPayReadyResponse>{
            override fun onResponse(call: Call<KakaoPayReadyResponse>, response: Response<KakaoPayReadyResponse>
            ) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful: ${response}")
                }
            }

            override fun onFailure(call: Call<KakaoPayReadyResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun foodPreferenceRegister(identifier: Int,foodName:String,classification:String,callback : (String) -> Unit){
        Log.d(TAG, "foodPreferenceRegister: 호출됨")
        service.foodPreferenceRegister(identifier,foodName,classification).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response}")
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
            }

        })
    }
    fun searchFood(query : String, callback:(FoodPreferenceSearchDataClass) -> Unit){
//        callback(FoodPreferenceSearchDataClass("success", arrayListOf("낙지","오징어","ㅋㅋ","음식")))
        service.searchFood(query).enqueue(object : Callback<FoodPreferenceSearchDataClass>{
            override fun onResponse(
                call: Call<FoodPreferenceSearchDataClass>,
                response: Response<FoodPreferenceSearchDataClass>
            ) {
                if(response.isSuccessful){
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<FoodPreferenceSearchDataClass>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t}")
            }

        })
    }

    fun deleteFoodPreference(identifier: Int,foodName: String,callback: (String) -> Unit){
        service.deleteFoodPreference(identifier,foodName).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    callback(response.body()!!)
                    Log.d(TAG, "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback("failed")
            }

        })

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun requestApprovePayment(tid : String, userId: String, pgToken : String, callback : (KakaoPayApproveResponse) -> Unit){

        val requestModel = HashMap<String, String>()
        requestModel.put("cid", cid)
        requestModel.put("tid", tid)
        requestModel.put("partner_order_id", orderId)
        requestModel.put("partner_user_id", userId)
        requestModel.put("pg_token", pgToken)
        Log.d(TAG, "requestModel: $requestModel")

        val call : Call<KakaoPayApproveResponse>
                = kakaoPayService.requestApprovePayment(API_KEY, requestModel)

        call.enqueue(object : Callback<KakaoPayApproveResponse>{
            override fun onResponse(call: Call<KakaoPayApproveResponse>, response: Response<KakaoPayApproveResponse>
            ) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful : $response")
                }
            }

            override fun onFailure(call: Call<KakaoPayApproveResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }


    fun scheduleMidnightWork(application: Application, callback: (Boolean) -> Unit) {
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 24) // 자정
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()
        val delay = midnight.timeInMillis - currentTime.timeInMillis

        val midnightWorkRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("midnightWork")
            .build()

        WorkManager.getInstance(application.applicationContext).enqueue(midnightWorkRequest)

        WorkManager.getInstance(application.applicationContext)
            .getWorkInfoByIdLiveData(midnightWorkRequest.id)
            .observeForever{
                if(it != null && it.state == WorkInfo.State.SUCCEEDED){
                    Log.d(TAG, "scheduleMidnightWork: success")
                    callback(true)
                }
            }
        //Application Context를 사용하되, 액티비티나 프래그먼트 Context 사용 x
        //직접 참조 시 ViewModel 생명주기가 View보다 long 독립적인 각각의 생명주기가 꼬일 수 있고,
        // View가 모두 종료돼도 ViewModel이 계속해서 참조 시 메모리 누수 발생 가능
    }



    fun logout(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()
    }

}