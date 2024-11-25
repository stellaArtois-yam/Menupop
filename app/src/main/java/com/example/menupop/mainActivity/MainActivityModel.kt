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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MainActivityModel(val application: Application) {
    companion object {
        const val TAG = "MainActivityModel"
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_IP)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val service = retrofit.create(RetrofitService::class.java)


    fun getProfileImage(sharedPreferences: SharedPreferences): String? {
        val image = sharedPreferences.getString("profileImage", "")

        if (image != "") {
            return image
        }

        return null
    }

    suspend fun updateTicketQuantity(
        identifier: Int,
        ticketType: String,
        operator: String,
        quantity: Int
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    service.updateTicketQuantity(identifier, ticketType, operator, quantity)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    Log.d(TAG, "update ticket is not successful")
                    response.code().toString()
                }

            } catch (e: Exception) {
                //ticket 사용이 안된 것이므로 다시 결제하라는 toast나 뭐가 떠야함
                e.message!!
            }
        }
    }

    suspend fun updateRewardQuantity(identifier: Int) : String{
        return withContext(Dispatchers.IO) {
            val response = service.updateRewardQuantity(identifier)
            try {
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    Log.d(TAG, "update Reward is not successful")
                    response.code().toString()
                }
            } catch (e: Exception) {
                Log.d(TAG, "update reward onFailure: ${e.message}")
                e.message!!
            }
        }
    }


    suspend fun getFoodPreference(identifier: Int): FoodPreferenceDataClass {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getFoodPreference(identifier)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    Log.d(TAG, "Response not successful: $response")
                    FoodPreferenceDataClass("failed", arrayListOf())
                }
            } catch (e: Exception) {
                Log.d(TAG, "getFoodPreference failed: ${e.message}")
                FoodPreferenceDataClass(e.message!!, arrayListOf())
            }
        }

    }


    suspend fun savePaymentHistory(ticketSaveModel: TicketSaveDTO) :SimpleResultDTO {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.savePaymentHistory(
                    ticketSaveModel.identifier,
                    ticketSaveModel.tid,
                    ticketSaveModel.paymentType,
                    ticketSaveModel.item,
                    ticketSaveModel.price,
                    ticketSaveModel.approvedAt,
                    ticketSaveModel.translationTicket,
                    ticketSaveModel.foodTicket
                )

                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    Log.d(TAG, "is not successful: $response")
                    (SimpleResultDTO("failed"))
                }
            } catch (e: Exception) {
                Log.d(TAG, "save payment history onFailure: ${e.message}")
                (SimpleResultDTO(e.message!!))
            }
        }
    }


    suspend fun requestUserInformation(identifier: Int): UserInformationDTO {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.requestUserInformation(identifier)
                if (response.result == "success") {
                    Log.d(TAG, "requestUseInfo onResponse: $response")
                    response
                } else {
                    Log.d(TAG, "isNotSuccessful: $response")
                    UserInformationDTO(
                        "isNotSuccessful",
                        null, null, null, 0,
                        0, 0, 0, 0,
                        0
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "request user info onFailure: ${e.message}")
                UserInformationDTO(
                    e.message,
                    null, null, null, 0,
                    0, 0, 0, 0,
                    0
                )
            }
        }

    }


    private val kakaopay = Retrofit.Builder()
        .baseUrl("https://kapi.kakao.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val kakaoPayService = kakaopay.create(RetrofitService::class.java)
    private val cid = "TC0ONETIME"
    private val apiKey = "KakaoAK " + BuildConfig.KAKAOPAY_ADMIN_KEY
    private val approvalUrl = BuildConfig.SERVER_IP + "KakaoPayApprove"
    private val cancelUrl = BuildConfig.SERVER_IP + "KakaoPayCancel"
    private val failUrl = BuildConfig.SERVER_IP + "KakaoPayFail"


    @RequiresApi(Build.VERSION_CODES.O)
    val orderId = LocalDate.now().toString().replace("-", "") + hashCode().toString() //주문번호

    suspend fun requestCancelPayment(
        tid: String,
        cancelAmount: String
    ) : KakaoPayCancelResponseDTO {
        val map = HashMap<String, String>()
        map["cid"] = cid
        map["tid"] = tid
        map["cancel_amount"] = cancelAmount
        map["cancel_tax_free_amount"] = "0"

        return withContext(Dispatchers.IO) {
            try {
                val response = kakaoPayService.requestCancelPayment(apiKey, map)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    KakaoPayCancelResponseDTO(
                        "null", "null", "null",
                        response.message(), "null", "null",
                        "null", KakaoPayPriceDTO("0"), "null"
                    )
                }
            } catch (e: Exception) {
                KakaoPayCancelResponseDTO(
                    "null", "null", "null",
                    e.message!!, "null", "null",
                    "null", KakaoPayPriceDTO("0"), "null"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createPaymentRequest(
        userId: String, item: String, quantity: String,
        totalAmount: String
    ): KakaoPayReadyResponseDTO {

        val requestModel = HashMap<String, String>()

        val kakaoPayRequestModel = KakaoPayRequestDTO(
            cid,
            orderId,
            userId,
            item,
            quantity,
            totalAmount,
            "0",
            approvalUrl,
            cancelUrl,
            failUrl
        )

        val fields = kakaoPayRequestModel.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field.get(kakaoPayRequestModel)?.toString() ?: ""
            requestModel[field.name] = value
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = kakaoPayService.createPaymentRequest(apiKey, requestModel)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    KakaoPayReadyResponseDTO("isNotSuccessful")
                }
            } catch (e: Exception) {
                KakaoPayReadyResponseDTO("N/A")
            }
        }
    }

    suspend fun foodPreferenceRegister(
        identifier: Int,
        foodName: String,
        classification: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.foodPreferenceRegister(identifier, foodName, classification)
                Log.d(TAG, "food register response: $response")
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    response.code().toString()
                }
            } catch (e: Exception) {
                Log.d(TAG, "food register onFailure: ${e.message}")
                e.message!!
            }
        }
    }


    suspend fun searchFood(query: String): FoodPreferenceSearchDTO {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.searchFood(query)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    FoodPreferenceSearchDTO("isNotSuccessful", arrayListOf())
                }
            } catch (e: Exception) {
                FoodPreferenceSearchDTO("failed", arrayListOf())
            }
        }
    }


    suspend fun deleteFoodPreference(identifier: Int, foodName: String): String {
        return withContext(Dispatchers.IO) {
            val response = service.deleteFoodPreference(identifier, foodName)
            try {
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    response.code().toString()
                }
            } catch (e: Exception) {
                e.message!!
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun requestApprovePayment(
        tid: String,
        userId: String,
        pgToken: String
    ): KakaoPayApproveResponseDTO {

        val requestModel = HashMap<String, String>()
        requestModel["cid"] = cid
        requestModel["tid"] = tid
        requestModel["partner_order_id"] = orderId
        requestModel["partner_user_id"] = userId
        requestModel["pg_token"] = pgToken
        Log.d(TAG, "requestModel: $requestModel")

        return withContext(Dispatchers.IO) {
            try {
                val response = kakaoPayService.requestApprovePayment(apiKey, requestModel)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    KakaoPayApproveResponseDTO(
                        "N/A", null,
                        null, null, null,
                        null, null, null, null
                    )
                }
            } catch (e: Exception) {
                KakaoPayApproveResponseDTO(
                    "N/A", null,
                    null, null, null,
                    null, null, null, null
                )
            }
        }

    }

    private fun setDelayTime(): Long {

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 24) // 자정
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val currentTime = Calendar.getInstance()

        if (midnight.before(currentTime)) {
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
            .enqueueUniqueWork(
                "midnightWork",
                ExistingWorkPolicy.KEEP,
                midnightWorkRequest
            )

//        Log.d(TAG, "workManager first: ${midnightWorkRequest.id}")

        WorkManager.getInstance(application.applicationContext)
            .getWorkInfosForUniqueWorkLiveData("midnightWork")
            .observeForever {
//                Log.d(TAG, "workInfo Size: ${it.size}")
//                Log.d(TAG, "workInfo info: ${it[0]}")

                if (it != null && it[0].state == WorkInfo.State.SUCCEEDED) {
//                    Log.d(TAG, "WorkManager success? : ${it[0].state}")
                    callback(true)

                    val delay = setDelayTime()

                    val againWorkRequest = OneTimeWorkRequestBuilder<MidnightResetWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build()

                    WorkManager.getInstance(application.applicationContext)
                        .enqueueUniqueWork(
                            "midnightWork",
                            ExistingWorkPolicy.KEEP,
                            againWorkRequest
                        )

//                    Log.d(TAG, "WorkManager enqueue again: ${againWorkRequest.id}")

                } else if (it == null) {
                    Log.d(TAG, "WorkManager null")
                } else {
//                    Log.d(TAG, "${it[0].id}: ${it[0].state}")
                }
            }
    }


    suspend fun withdrawal(
        identifier: Int,
        email: String,
        id: String,
        date: String
    ) : String {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.withdrawal(identifier, email, id, date)
                if (response.isSuccessful) {
                    response.body()!!
                } else {
                    response.code().toString()
                }
            } catch (e: Exception) {
                e.message!!
            }
        }
    }


    fun saveSelectedProfile(
        drawable: String,
        sharedPreferences: SharedPreferences
    ): String {
        if (sharedPreferences.edit().putString("profileImage", drawable).commit()) {
            return "success"
        }
        return "failed"
    }


    suspend fun requestAd(key: String): RewardedAd = suspendCoroutine { continuation ->
        val adRequest: AdRequest = AdRequest.Builder().build()

        RewardedAd.load(application, key, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // 실패 시 예외를 발생시켜서 코루틴이 예외를 처리하도록 함
                Log.d(TAG, loadAdError.toString())
                continuation.resumeWithException(Exception("Failed to load ad"))
            }

            override fun onAdLoaded(ad: RewardedAd) {
                // 광고 로딩 성공 시 광고 객체를 반환
                Log.d(TAG, "Ad was loaded.")
                continuation.resume(ad)
            }
        })
    }


    fun logout(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.remove("identifier")
        editor.commit()
    }

}