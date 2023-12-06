package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.menupop.KakaoPayApproveResponse
import com.example.menupop.KakaoPayReadyResponse
import com.example.menupop.TicketSaveModel
import com.example.menupop.signup.ResultModel
import java.text.DecimalFormat
import java.util.Objects

class MainActivityViewModel: ViewModel() {
    val TAG = "MainActivityViewModel"
    val mainActivityModel = MainActivityModel()
    private var callback : ((ResultModel) -> Unit) ?= null
    private var callbackUserInfo :((UserInformationData) ->Unit) ?= null
    private var callbackKakaoReady : ((KakaoPayReadyResponse) -> Unit) ? = null
    private var callbackApprove : ((KakaoPayApproveResponse) -> Unit) ? = null
    private var callbackSearchData : ((FoodPreferenceSearchDataClass) -> Unit) ?= null

    private val _searchFood = MutableLiveData<ArrayList<String>>()
    val searchFood: LiveData<ArrayList<String>>
        get() = _searchFood

    /**
     * 메인
     */
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading : LiveData<Boolean>
        get() = _isLoading

    private val _userInformation = MutableLiveData<UserInformationData>()
    val userInformation : LiveData<UserInformationData>
        get() = _userInformation

    fun searchFood(query : String){
        callbackSearchData = {result ->
            if(result.result == "success"){
                _searchFood.value = result.foodList
            }
            Log.d(TAG, "searchFood: $result")
        }
        mainActivityModel.searchFood(query,callbackSearchData!!)

    }
    fun getUserTaste(sharedPreferences: SharedPreferences){
        val identifier = mainActivityModel.getUserInfo(sharedPreferences)
    }


    fun getUserInfo(sharedPreferences: SharedPreferences) : Int{
        val identifier = mainActivityModel.getUserInfo(sharedPreferences)
        return identifier
    }
    
    fun requestUserInformation(identifier : Int){
        callbackUserInfo = {response ->
            _userInformation.value = response
            Log.d(TAG, "requestUserInformation: ${response.id}, ${response.email}")
            _isLoading.value = response.id != null && response.email!=null
        }
        mainActivityModel.requestUserInformation(identifier, callbackUserInfo!!)
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

        // db에 티켓 개수도 수정
        // 구매 이력 저장
            savePaymentHistory(response.partner_user_id.toInt(),
                response.tid, response.payment_method_type,
                response.item_name, response.amount.total.toInt(), response.approved_at)

        }
        mainActivityModel.requestApprovePayment(tid, userId, pgToken, callbackApprove!!)
    }

    fun savePaymentHistory(identifier: Int, tid : String, paymentType : String, item : String,
                           price : Int, approveAt : String){
        var ticketSaveModel : TicketSaveModel ?= null
        callback = {response ->
            Log.d(TAG, "savePaymentHistory: ${response.result}")
            //여기서 클라이언트 티켓 개수 수정

            if(response.result == "success"){

                if(_paymentType.value == "regular"){

                    _userInformation.value!!.foodTicket = _userInformation.value!!.foodTicket + _regularFoodAmount.value!!.toInt()
                    _userInformation.value!!.translationTicket = _userInformation.value!!.translationTicket + _regularTranslationAmount.value!!.toInt()

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
            ticketSaveModel = TicketSaveModel(identifier,
                tid, paymentType, item, price,approveAt,
                _regularTranslationAmount.value!!.toInt(), _regularFoodAmount.value!!.toInt())
            Log.d(TAG, "ticketSaveModel: $ticketSaveModel")


        }else{

        }


        mainActivityModel.savePaymentHistory(ticketSaveModel!!, callback!!)


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