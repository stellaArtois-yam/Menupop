package com.example.menupop.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.menupop.KakaoPayApproveResponse
import com.example.menupop.KakaoPayReadyResponse
import com.example.menupop.MidnightResetWorker
import com.example.menupop.TicketSaveDTO
import com.example.menupop.signup.ResultModel
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivityViewModel(private val application: Application) :  AndroidViewModel(application){
    val TAG = "MainActivityViewModel"
    val mainActivityModel = MainActivityModel(application)
    private var callback : ((ResultModel) -> Unit) ?= null
    private var callbackUserInfo :((UserInformationData) ->Unit) ?= null
    private var callbackKakaoReady : ((KakaoPayReadyResponse) -> Unit) ? = null
    private var callbackApprove : ((KakaoPayApproveResponse) -> Unit) ? = null
    private var callbackSearchData : ((FoodPreferenceSearchDataClass) -> Unit) ?= null
    private var callbackResult : ((String) -> Unit) ?= null
    private var callbackFoodPreference : ((FoodPreferenceDataClass) -> Unit) ?= null
    private var callbackAd: ((RewardedAd?) -> Unit)? = null


    private val _rewardedAd = MutableLiveData<RewardedAd>()

    val rewardedAd : LiveData<RewardedAd>
        get() = _rewardedAd

    private val _searchFood = MutableLiveData<ArrayList<String>>()
    val searchFood: LiveData<ArrayList<String>>
        get() = _searchFood

    private val _foodPreferenceList = MutableLiveData<FoodPreferenceDataClass>()
    val foodPreferenceList: LiveData<FoodPreferenceDataClass>
        get() = _foodPreferenceList

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean>
        get() = _registerResult

    private val _accountWithdrawal = MutableLiveData<String>()

    val accountWithdrawal : LiveData<String>
        get() = _accountWithdrawal
    val haveRewarded = MutableLiveData<String>()

    /**
     * 메인
     */
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading : LiveData<Boolean>
        get() = _isLoading

    private val _deletedResult = MutableLiveData<Boolean>()
    val deletedResult : LiveData<Boolean>
        get() = _deletedResult

    private val _userInformation = MutableLiveData<UserInformationData>()
    val userInformation : LiveData<UserInformationData>
        get() = _userInformation

    fun ticketMinus(sharedPreferences: SharedPreferences){
        Log.d(TAG, "ticketMinus: ${_userInformation.value!!.foodTicket}")
        val userInfo = mainActivityModel.getUserInfo(sharedPreferences)
        val identifier = userInfo.get("identifier")
        callbackResult = {result ->
            Log.d(TAG, "ticketMinus: $result")
            if(result.trim() == "success"){
                _userInformation.value?.foodTicket = _userInformation.value?.foodTicket?.minus(1)!!
            }
        }
        mainActivityModel.minusFoodTicket(identifier!!,callbackResult!!)
        Log.d(TAG, "ticketMinus 반역: ${_userInformation.value!!.foodTicket}")
    }

    fun foodPreferenceRegister(sharedPreferences: SharedPreferences,foodName:String,classification:String){
        Log.d(TAG, "foodPreferenceRegister: 호출됨")
        val userInfo = mainActivityModel.getUserInfo(sharedPreferences)
        val identifier = userInfo.get("identifier")
        callbackResult = { result ->
            Log.d(TAG, "foodPreferenceRegister: ${result}")
            _registerResult.value = result == "success"
        }
        mainActivityModel.foodPreferenceRegister(identifier!!,foodName,classification,callbackResult!!)
    }

    fun deleteFoodPreference(sharedPreferences: SharedPreferences,foodName: String){
        callbackResult = {result ->
            Log.d(TAG, "deleteFoodPreference:$result d ${result == "success"}")
            if(result.trim() == "success"){
                Log.d(TAG, "deleteFoodPreference: 성공")
                _deletedResult.value = true
            }else{
                _deletedResult.value= false
            }
        }
        val userInfo = mainActivityModel.getUserInfo(sharedPreferences)
        val identifier = userInfo.get("identifier")

        mainActivityModel.deleteFoodPreference(identifier!!,foodName,callbackResult!!)

    }

    fun searchFood(query : String){
        callbackSearchData = {result ->
            Log.d(TAG, "searchFood: test")
            if(result.result == "success"){
                _searchFood.value = result.foodList
            } else if(result.result == "notFound"){
                Log.d(TAG, "searchFood: 찾을 수 없음")
            }
            Log.d(TAG, "searchFood: $result")
        }
        mainActivityModel.searchFood(query,callbackSearchData!!)

    }


    fun getUserInfo(sharedPreferences: SharedPreferences) : Int{
        val userInfo = mainActivityModel.getUserInfo(sharedPreferences)
        val identifier = userInfo.get("identifier")
        _dailyTranslation.value = userInfo.get("dailyTranslation")
        _dailyReword.value = userInfo.get("dailyReword")
        Log.d(TAG, "getUserInfo: $identifier")
        return identifier!!
    }

    fun requestUserInformation(identifier : Int){
        callbackUserInfo = {response ->
            _userInformation.value = response
            Log.d(TAG, "requestUserInformation: ${response.id}, ${response.email}")
            _isLoading.value = response.id != null && response.email!=null
        }
        mainActivityModel.requestUserInformation(identifier, callbackUserInfo!!)
    }

    fun loadAd(key: String) {
        callbackAd = { ad ->
            Log.d(TAG, "loadAd: ${ad}")
            _rewardedAd.value = ad
        }
        mainActivityModel.requestAd(key, callbackAd!!)
    }
    fun setRewarded(sharedPreferences: SharedPreferences){
        haveRewarded.value = mainActivityModel.setRewarded(sharedPreferences)
    }
    fun rewardedSuccess(sharedPreferences: SharedPreferences){
        val rewarded = mainActivityModel.rewardedPlus(sharedPreferences)
        haveRewarded.value = "${rewarded.toString()} / 3"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun withDrawal(sharedPreferences: SharedPreferences){
        callbackResult = {
            Log.d(TAG, "withDrawal: $it")
            _accountWithdrawal.value = it
            if(it == "success"){
                logout(sharedPreferences)
            }
        }
        val identifier = getUserInfo(sharedPreferences)
        val email = userInformation.value?.email
        val id = userInformation.value?.id
        val localDate: LocalDate = LocalDate.now()
        mainActivityModel.withDrawal(identifier,email!!,id!!,localDate.toString(),callbackResult!!)
    }

    fun getFoodPreference(sharedPreferences: SharedPreferences){
        val identifier = getUserInfo(sharedPreferences)
        callbackFoodPreference = { foodPreferenceDataClass ->
            Log.d(TAG, "getFoodPreference: ${foodPreferenceDataClass}")
            _foodPreferenceList.value = foodPreferenceDataClass
        }
        mainActivityModel.getFoodPreference(identifier,callbackFoodPreference!!)
    }

    fun listReset(){
    }



    /**
     * 티켓 구매 + 다이얼로그
     */

    private val _paymentType = MutableLiveData<String>()

    private val _regularTranslationAmount = MutableLiveData<String>()
    val regularTranslationAmount : LiveData<String>
        get() = _regularTranslationAmount

    private val _regularTranslationPrice = MutableLiveData<String>()
    val regularTranslationPrice : LiveData<String>
        get() = _regularTranslationPrice

    private val _regularFoodAmount = MutableLiveData<String>()
    val regularFoodAmount : LiveData<String>
        get() = _regularFoodAmount

    private val _regularFoodPrice = MutableLiveData<String>()
    val regularFoodPrice : LiveData<String>
        get() = _regularFoodPrice

    private val _regularTotalPrice = MutableLiveData<String>()

    val regularTotalPrice : LiveData<String>
        get() = _regularTotalPrice

    private val _totalPriceForPay = MutableLiveData<String>()

    fun updatePaymentType (type : String){
        Log.d(TAG, "updatePaymentType: $type")
        _paymentType.value = type
    }

    fun addTranslationTicket() {
        changeTicketAmount(_regularTranslationAmount, _regularTranslationPrice, _regularFoodAmount)
    }

    fun removeTranslationTicket() {
        if (_regularTranslationAmount.value!!.toInt() > 0) {
            changeTicketAmount(_regularTranslationAmount, _regularTranslationPrice, _regularFoodAmount, -1)
        }
    }

    fun addFoodTicket() {
        changeTicketAmount(_regularFoodAmount, _regularFoodPrice, _regularTranslationAmount)
    }

    fun removeFoodTicket() {
        if (_regularFoodAmount.value!!.toInt() > 0) {
            changeTicketAmount(_regularFoodAmount, _regularFoodPrice, _regularTranslationAmount, -1)
        }
    }

    private fun changeTicketAmount(ticketAmount: MutableLiveData<String>,
                                   ticketPrice: MutableLiveData<String>,
                                   otherTicketAmount: MutableLiveData<String>,
                                   change: Int = 1) {
        val quantity = ticketAmount.value!!.toInt() + change
        ticketAmount.value = quantity.toString()

        val dec = DecimalFormat("#,###")

        val price = quantity * 2000
        ticketPrice.value = "${dec.format(price)}원"

        val otherTicketPrice = otherTicketAmount.value!!.toInt() * 2000

        val totalPrice = price + otherTicketPrice

        _totalPriceForPay.value = totalPrice.toString()
        Log.d(TAG, "totalPriceForPay: ${_totalPriceForPay.value}")

        _regularTotalPrice.value = "총 결제 금액 : ${dec.format(totalPrice)}원"
    }



    fun countTicket(ticketAmount: MutableLiveData<String>,
                    otherTicketAmount: MutableLiveData<String>): Int{
        if(ticketAmount.value!!.toInt() > 0 && otherTicketAmount.value!!.toInt() >0){
            return 2
        }else{
            return 1
        }
    }

    fun itemName(ticketAmount: MutableLiveData<String>,
                 otherTicketAmount: MutableLiveData<String>) : String{
        if(ticketAmount.value!!.toInt() > 0 && otherTicketAmount.value!!.toInt() >0){
            val total = ticketAmount.value!!.toInt() + otherTicketAmount.value!!.toInt() - 1
            return "번역 티켓 외 $total"

        }else if(ticketAmount.value!!.toInt() > 0 && otherTicketAmount.value!!.toInt() == 0){
            return "번역 티켓"
        }else{
            return "음식 티켓"
        }
    }


    private val _paymentResponse = MutableLiveData<KakaoPayReadyResponse>()
    val paymentResponse : LiveData<KakaoPayReadyResponse>
        get() = _paymentResponse

    private val _pgToken = MutableLiveData<String>()

    private val _userId = MutableLiveData<String>()


    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePgToken(token : String){

        Log.d(TAG, "updatePgToken: $token")
        _pgToken.value = token

        if(_pgToken.value !=null){
            Log.d(TAG, "updatePgToken: not null")
            completePayment(_paymentResponse.value!!.tid, _userId.value!!, _pgToken.value!!)
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId : String){
        Log.d(TAG, "createPaymentRequest: !!!")
        callbackKakaoReady = {response ->

            //여기서 웹뷰로 보내 줘야 함
            if(response!=null){
                _paymentResponse.value = response
            }

        }
        val item = itemName(_regularTranslationAmount, _regularFoodAmount)
        val quantity = countTicket(_regularTranslationAmount, _regularFoodAmount)

        _userId.value = userId

        mainActivityModel.createPaymentRequest(userId, item, quantity.toString(),
            _totalPriceForPay.value!!, callbackKakaoReady!!)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completePayment(tid : String, userId: String, pgToken : String){
        callbackApprove = {response ->
            Log.d(TAG, "completePayment: $response")

            _paymentResponse.value = null

            // db에 티켓 개수도 수정
            // 구매 이력 저장
            savePaymentHistory(response.partner_user_id.toInt(),
                response.tid, response.payment_method_type,
                response.item_name, response.amount.total.toInt(), response.approved_at)

        }
        mainActivityModel.requestApprovePayment(tid, userId, pgToken, callbackApprove!!)
    }

    val _changeTicket = MutableLiveData<Boolean>()

    val changeTicket : LiveData<Boolean>
        get() =_changeTicket

    fun savePaymentHistory(identifier: Int, tid : String, paymentType : String, item : String,
                           price : Int, approveAt : String){
        var ticketSaveModel : TicketSaveDTO ?= null
        callback = {response ->
            Log.d(TAG, "savePaymentHistory: ${response.result}")
            //여기서 클라이언트 티켓 개수 수정

            if(response.result == "success"){

                if(_paymentType.value == "regular"){
                    _userInformation.value!!.foodTicket = _userInformation.value!!.foodTicket + _regularFoodAmount.value!!.toInt()
                    Log.d(TAG, "foodTicket: ${_userInformation.value!!.foodTicket}")

                    _userInformation.value!!.translationTicket = _userInformation.value!!.translationTicket + _regularTranslationAmount.value!!.toInt()
                    Log.d(TAG, "translationTicket: ${_userInformation.value!!.translationTicket}")

                    _changeTicket.value = true

                }else if(_paymentType.value == "reword"){
                    /**
                     * 여기 바꿔야함
                     */
                    _userInformation.value!!.foodTicket = _userInformation.value!!.foodTicket + _regularFoodAmount.value!!.toInt()
                    _userInformation.value!!.translationTicket = _userInformation.value!!.translationTicket + _regularTranslationAmount.value!!.toInt()

                }


            }else{
                Log.d(TAG, "savePaymentHistory: failed")
            }

        }

        if(_paymentType.value == "regular"){
            ticketSaveModel = TicketSaveDTO(identifier,
                tid, paymentType, item, price,approveAt,
                _regularTranslationAmount.value!!.toInt(), _regularFoodAmount.value!!.toInt())
            Log.d(TAG, "ticketSaveModel: $ticketSaveModel")


        }else{

        }


        mainActivityModel.savePaymentHistory(ticketSaveModel!!, callback!!)


    }

    fun logout(sharedPreferences: SharedPreferences){
        mainActivityModel.logout(sharedPreferences)
    }
    fun registerVariableReset(){
        _registerResult.value = false
    }

    private val _dailyTranslation = MutableLiveData<Int>()
    val dailyTranslation : LiveData<Int>
        get() = _dailyTranslation

    private val _dailyReword = MutableLiveData<Int>()
    val dailyReword : LiveData<Int>
        get() = _dailyReword

    fun scheduleMidnightWork(application: Application){
        val callback : ((Boolean) -> Unit) ={
            if(it){
                _dailyTranslation.value = 3
                _dailyReword.value = 3
            }

        }
        mainActivityModel.scheduleMidnightWork(application, callback)


    }



    init {
        _regularTranslationAmount.value = "1"
        _regularFoodAmount.value = "1"
        _regularTranslationPrice.value = "2,000원"
        _regularFoodPrice.value = "2,000원"
        _regularTotalPrice.value = "총 결제 금액 : 4,000원"
        _totalPriceForPay.value = "4000"

    }
}