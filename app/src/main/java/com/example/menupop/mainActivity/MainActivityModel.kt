package com.example.menupop.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
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


    fun getUserInfo(sharedPreferences: SharedPreferences) : HashMap<String, Int>{
        val hashmap = HashMap<String, Int>()

        val identifier = sharedPreferences.getInt("identifier", 0)
        val dailyTranslation = sharedPreferences.getInt("dailyTranslation", 0)
        val dailyReward = sharedPreferences.getInt("dailyReward", 0)
        val rewarded = sharedPreferences.getInt("rewarded", 0)
        val todayRewarded = sharedPreferences.getInt("todayRewarded", 0)

        hashmap.put("identifier", identifier)
        hashmap.put("dailyTranslation", dailyTranslation)
        hashmap.put("dailyReward", dailyReward)
        hashmap.put("rewarded", rewarded)
        hashmap.put("todayRewarded", todayRewarded)

        return hashmap
    }

    fun getProfileImage(sharedPreferences: SharedPreferences) : String?{
        val image = sharedPreferences.getString("profileImage", "")

        if(image != ""){
            return image
        }

        return null
    }

    fun minusTranslationTicket(identifier: Int,callback: (String) -> Unit){
        service.minusTranslationTicket(identifier).enqueue(object : Callback<String>{
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

    fun minusFreeFoodTicket(identifier: Int,callback: (String) -> Unit){
        service.minusFreeFoodTicket(identifier).enqueue(object : Callback<String>{
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
                    Log.d(TAG, "is not successful: $response")
                }

            }
            override fun onFailure(call: Call<SimpleResultDTO>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
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

                }
            }

            override fun onFailure(call: Call<UserInformationDTO>, t: Throwable) {
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
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }


    fun scheduleMidnightWork(application: Application, callback: (Boolean) -> Unit) {

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16) // 자정
            set(Calendar.MINUTE, 8)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()
        val delay = midnight.timeInMillis - currentTime.timeInMillis

        val midnightWorkRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()


        WorkManager.getInstance(application.applicationContext).enqueue(midnightWorkRequest)

        Log.d(TAG, "workManager first: ${WorkManager
            .getInstance(application.applicationContext)
            .getWorkInfoByIdLiveData(midnightWorkRequest.id)}")

        WorkManager.getInstance(application.applicationContext)
            .getWorkInfoByIdLiveData(midnightWorkRequest.id)
            .observeForever{
                if(it != null && it.state == WorkInfo.State.SUCCEEDED){
                    Log.d(TAG, "WorkManager success? : ${it.state}")
                    callback(true)

                }else{
                    Log.d(TAG, "WorkManager: ${it.state}")
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
                Log.d(TAG, "onFailure: ${t}")
                callback("failed")
            }

        })
    }
    fun rewardedPlus(sharedPreferences: SharedPreferences) : Pair<Int,Int>{
        var rewarded = sharedPreferences.getInt("rewarded",0)
        var dailyReward = sharedPreferences.getInt("dailyReward", 0)
        var todayRewarded = sharedPreferences.getInt("todayRewarded", 0)
        rewarded += 1
        dailyReward -=1
        todayRewarded += 1

        Log.d(TAG, "rewardedPlus: rewarded_$rewarded dailyReward_$dailyReward")

        val edit = sharedPreferences.edit()
        edit.putInt("rewarded",rewarded)
        edit.putInt("dailyReward", dailyReward)
        edit.putInt("todayRewarded", todayRewarded)
        edit.commit()

        return Pair<Int,Int>(todayRewarded,dailyReward)
    }
    fun setRewarded(sharedPreferences: SharedPreferences, haveReward : Int) {
        Log.d(TAG, "setRewarded: $haveReward")
        sharedPreferences.edit().putInt("rewarded",haveReward).commit()
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