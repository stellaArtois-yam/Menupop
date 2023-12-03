package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.menupop.KakaoPayResponseModel
import java.text.DecimalFormat

class MainActivityViewModel: ViewModel() {
    val TAG = "MainActivityViewModel"
    val mainActivityModel = MainActivityModel()
    private var callbackUserInfo :((UserInformationData) ->Unit) ?= null
    private var callbackKakaoReady : ((KakaoPayResponseModel) -> Unit) ? = null

    /**
     * 메인
     */
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading : LiveData<Boolean>
        get() = _isLoading

    private val _userInformation = MutableLiveData<UserInformationData>()
    val userInformation : LiveData<UserInformationData>
        get() = _userInformation


    fun getUserInfo(sharedPreferences: SharedPreferences) : Int{
        val identifier = mainActivityModel.getUserInfo(sharedPreferences)
        return identifier
    }
    
    fun requestUserInformation(identifier : Int){
        callbackUserInfo = {response ->
            _userInformation.value = response
            Log.d(TAG, "requestUserInformation: ${response.id}, ${response.email}")
            if(response.id != null && response.email!=null){
                _isLoading.value = true
            }else{
                _isLoading.value = false
            }
        }
        mainActivityModel.requestUserInformation(identifier, callbackUserInfo!!)
    }



    /**
     * 티켓 구매 + 다이얼로그
     */
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
            Log.d(TAG, "itemName: 번역 티켓 외 $total")
            return "번역 티켓 외 $total"

        }else if(ticketAmount.value!!.toInt() > 0 && otherTicketAmount.value!!.toInt() == 0){
            Log.d(TAG, "itemName: 번역 티켓")
            return "번역 티켓"
        }else{
            Log.d(TAG, "itemName: 음식 티켓")
            return "음식 티켓"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId : String){
        Log.d(TAG, "createPaymentRequest: 호출")
        callbackKakaoReady = {response ->

            Log.d(TAG, "createPaymentRequest: $response")

        }
        val item = itemName(_regularTranslationAmount, _regularFoodAmount)
        val quantity = countTicket(_regularTranslationAmount, _regularFoodAmount)
        mainActivityModel.createPaymentRequest(userId, item, quantity.toString(),
            _totalPriceForPay.value!!, callbackKakaoReady!!)
        
    }

    fun completePayment(){

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