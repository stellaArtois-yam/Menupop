package com.example.menupop.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.menupop.BuildConfig
import com.example.menupop.mainActivity.profile.KakaoPayApproveResponseDTO
import com.example.menupop.mainActivity.profile.KakaoPayRequestDTO
import com.example.menupop.mainActivity.profile.KakaoPayReadyResponseDTO
import com.example.menupop.MidnightResetWorker
import com.example.menupop.RetrofitService
import com.example.menupop.mainActivity.profile.TicketSaveDTO
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceSearchDTO
import com.example.menupop.SimpleResultDTO
import com.example.menupop.mainActivity.profile.KakaoPayCancelResponseDTO
import com.example.menupop.mainActivity.profile.KakaoPayPriceDTO
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivityModel(val application :Application) {
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



    fun getProfileImage(sharedPreferences: SharedPreferences) : String?{
        val image = sharedPreferences.getString("profileImage", "")

        if(image != ""){
            return image
        }

        return null
    }

    fun updateTicketQuantity(identifier: Int,
                             ticketType : String,
                             operator : String,
                             quantity : Int,
                             callback: (String) ->Unit){
        service.updateTicketQuantity(identifier, ticketType, operator, quantity)
            .enqueue(object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d(TAG, "update ticket quantity onResponse: ${response.body()}")
                    if(response.isSuccessful){
                        callback(response.body()!!)
                    }else{
                        Log.d(TAG, "update ticket is not successful")
                        //여기도 예외 처리 필요
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(TAG, "update ticket quantity onFailure: ${t.message}")
                    //ticket 사용이 안된 것이므로 다시 결제하라는 toast나 뭐가 떠야함
                }
            })
    }

    fun updateRewardQuantity(identifier: Int, callback: (String) ->Unit){
        service.updateRewardQuantity(identifier).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "update Reward onResponse: ${response.body()}")
                if(response.isSuccessful){
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "update Reward is not successful")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "update reward onFailure: ${t.message}")
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
                Log.d(TAG, "get food preference onFailure: ${t}")
            }

        })

    }


    fun savePaymentHistory(ticketSaveModel: TicketSaveDTO, callback: (SimpleResultDTO) -> Unit){

        val call : Call<SimpleResultDTO> = service.savePaymentHistory(ticketSaveModel.identifier,
            ticketSaveModel.tid, ticketSaveModel.paymentType, ticketSaveModel.item, ticketSaveModel.price,
            ticketSaveModel.approvedAt, ticketSaveModel.translationTicket, ticketSaveModel.foodTicket)


        call.enqueue(object : Callback<SimpleResultDTO>{
            override fun onResponse(call: Call<SimpleResultDTO>, response: Response<SimpleResultDTO>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful: ${response}")
                    callback(SimpleResultDTO("failed"))
                }

            }
            override fun onFailure(call: Call<SimpleResultDTO>, t: Throwable) {
                Log.d(TAG, "save payment history onFailure: ${t.message}")
                callback(SimpleResultDTO("failed"))
            }
        })
    }



    fun requestUserInformation(identifier : Int, callback: (UserInformationDTO) -> Unit){
        val call : Call<UserInformationDTO> = service.requestUserInformation(identifier)

        call.enqueue(object : Callback<UserInformationDTO>{
            override fun onResponse(call: Call<UserInformationDTO>, response: Response<UserInformationDTO>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "isNotSuccessful: ${response.body()}")
                    callback(
                        UserInformationDTO("isNotSuccessful",
                        null, null, null, null,
                        0, 0, 0, 0,
                        0, 0)
                    )

                }
            }

            override fun onFailure(call: Call<UserInformationDTO>, t: Throwable) {
                Log.d(TAG, "request user info onFailure: ${t.message}")
                callback(UserInformationDTO(t.message,
                    null, null, null, null,
                    0, 0, 0, 0,
                        0, 0))
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

    fun requestCancelPayment(tid : String, cancelAmount : String, callback : (KakaoPayCancelResponseDTO) -> Unit){

        var map  = HashMap<String, String>()
        map.put("cid", cid)
        map.put("tid", tid)
        map.put("cancel_amount", cancelAmount)
        map.put("cancel_tax_free_amount", "0")


        val call : Call<KakaoPayCancelResponseDTO> = kakaoPayService.requestCancelPayment(API_KEY, map)

        call.enqueue(object : Callback<KakaoPayCancelResponseDTO>{
            override fun onResponse(
                call: Call<KakaoPayCancelResponseDTO>,
                response: Response<KakaoPayCancelResponseDTO>
            ) {
                Log.d(TAG, "cancel onResponse: ${response}")
                if(response.isSuccessful){
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "kakaopay cancel is not successful")
                    callback(
                        KakaoPayCancelResponseDTO("null", "null", "null",
                        "FAILED", "null", "null",
                            "null", KakaoPayPriceDTO("0"), "null")
                    )
                }
            }

            override fun onFailure(call: Call<KakaoPayCancelResponseDTO>, t: Throwable) {
                Log.d(TAG, "kakaopay cancel onFailure: ${t.message}")
                callback(  KakaoPayCancelResponseDTO("null", "null", "null",
                    "FAILED", "null", "null",
                    "null", KakaoPayPriceDTO("0"), "null"))
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId: String, item: String, quantity: String,
                             totalAmount: String, callback: (KakaoPayReadyResponseDTO) -> Unit){

        val requestModel = HashMap<String, String>()

        val kakaoPayRequestModel = KakaoPayRequestDTO(cid, orderId, userId, item, quantity, totalAmount, "0", approvalUrl, cancelUrl, failUrl)

        val fields = kakaoPayRequestModel.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field.get(kakaoPayRequestModel)?.toString() ?: ""
            requestModel[field.name] = value
        }

        val call : Call<KakaoPayReadyResponseDTO>
                = kakaoPayService.createPaymentRequest(API_KEY, requestModel)

        call.enqueue(object  : Callback<KakaoPayReadyResponseDTO>{
            override fun onResponse(call: Call<KakaoPayReadyResponseDTO>, response: Response<KakaoPayReadyResponseDTO>
            ) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful: ${response}")
                }
            }

            override fun onFailure(call: Call<KakaoPayReadyResponseDTO>, t: Throwable) {
                Log.d(TAG, "create payment request onFailure: ${t.message}")
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
                Log.d(TAG, "food register onFailure: ${t}")
            }

        })
    }


    fun searchFood(query : String, callback:(FoodPreferenceSearchDTO) -> Unit){
        service.searchFood(query).enqueue(object : Callback<FoodPreferenceSearchDTO>{
            override fun onResponse(
                call: Call<FoodPreferenceSearchDTO>,
                response: Response<FoodPreferenceSearchDTO>
            ) {
                if(response.isSuccessful){
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<FoodPreferenceSearchDTO>, t: Throwable) {
                Log.d(TAG, "search food onFailure: ${t}")
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
                Log.d(TAG, "delete food preference onFailure: ${t.message}")
                callback("failed")
            }

        })

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun requestApprovePayment(tid : String, userId: String, pgToken : String, callback : (KakaoPayApproveResponseDTO) -> Unit){

        val requestModel = HashMap<String, String>()
        requestModel.put("cid", cid)
        requestModel.put("tid", tid)
        requestModel.put("partner_order_id", orderId)
        requestModel.put("partner_user_id", userId)
        requestModel.put("pg_token", pgToken)
        Log.d(TAG, "requestModel: $requestModel")

        val call : Call<KakaoPayApproveResponseDTO>
                = kakaoPayService.requestApprovePayment(API_KEY, requestModel)

        call.enqueue(object : Callback<KakaoPayApproveResponseDTO>{
            override fun onResponse(call: Call<KakaoPayApproveResponseDTO>, response: Response<KakaoPayApproveResponseDTO>
            ) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not successful : $response")
                }
            }

            override fun onFailure(call: Call<KakaoPayApproveResponseDTO>, t: Throwable) {
                Log.d(TAG, "request approve payment onFailure: ${t.message}")
            }
        })
    }

    fun setDelayTime() : Long {

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 24) // 자정
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()

        if(midnight.before(currentTime)){
            midnight.add(Calendar.HOUR_OF_DAY, 24)
        }

        val delay = midnight.timeInMillis - currentTime.timeInMillis
        Log.d(TAG, "delay : $delay")


        Log.d(TAG, "WorkManager dueDate: ${midnight.timeInMillis}")
        Log.d(TAG, "WorkManager currentTime: ${currentTime.timeInMillis}")

        return delay
    }


    fun scheduleMidnightWork(application: Application, callback: (Boolean) -> Unit) {
        val delay = setDelayTime()

        val midnightWorkRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork("midnightWork",
                ExistingWorkPolicy.KEEP ,
                midnightWorkRequest)

        Log.d(TAG, "workManager first: ${midnightWorkRequest.id}")

        WorkManager.getInstance(application.applicationContext)
            .getWorkInfosForUniqueWorkLiveData("midnightWork")
            .observeForever{
                Log.d(TAG, "workInfo Size: ${it.size}")
                Log.d(TAG, "workInfo info: ${it[0]}")

                if (it != null && it[0].state == WorkInfo.State.SUCCEEDED) {
                    Log.d(TAG, "WorkManager success? : ${it[0].state}")
                    callback(true)

                    val delay = setDelayTime()

                    var againWorkRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build()

                    WorkManager.getInstance(application.applicationContext)
                        .enqueueUniqueWork("midnightWork",
                            ExistingWorkPolicy.KEEP,
                            againWorkRequest)

                    Log.d(TAG, "WorkManager enqueue again: ${againWorkRequest.id}")

                }else if(it == null){
                    Log.d(TAG, "WorkManager null")
                }else{
                    Log.d(TAG, "${it[0].id}: ${it[0].state}")
                }
            }
    }


    fun withdrawal(identifier: Int,email:String,id:String,date:String,callback: (String) -> Unit){
        service.withdrawal(identifier, email, id, date).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null){
                    callback(response.body()!!)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "withdrawal onFailure: ${t}")
                callback("failed")
            }

        })
    }



    fun saveSelectedProfile(drawable : String, sharedPreferences: SharedPreferences, callback: (String) -> Unit){
        if(sharedPreferences.edit().putString("profileImage", drawable).commit()){
            callback("success")
        }else{
            callback("failed")
        }

    }

    fun requestAd(key:String,callback: ((RewardedAd) -> Unit)){
        val adRequest: AdRequest = AdRequest.Builder().build()
        RewardedAd.load(application, key,
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    Log.d(TAG, loadAdError.toString())
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    callback(ad)
                    Log.d(TAG, "Ad was loaded.")
                }
            })
    }



    fun logout(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.remove("identifier")
        editor.commit()
    }

}