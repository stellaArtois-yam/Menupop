package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat

class MainActivityViewModel: ViewModel() {
    val TAG = "MainActivityViewModel"
    val mainActivityModel = MainActivityModel()
    private var callback :((UserInformationData) ->Unit) ?= null

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
        callback = {response ->  
            _userInformation.value = response
            Log.d(TAG, "requestUserInformation: ${response.id}, ${response.email}")
            if(response.id != null && response.email!=null){
                _isLoading.value = true
            }else{
                _isLoading.value = false
            }
        }
        mainActivityModel.requestUserInformation(identifier, callback!!)
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

        _regularTotalPrice.value = "총 결제 금액 : ${dec.format(totalPrice)}원"
    }

    fun createPaymentRequest(){

    }

    fun completePayment(){

    }




    init {
        _regularTranslationAmount.value = "1"
        Log.d(TAG, "엥 : ${regularTranslationAmount.value}")
        _regularFoodAmount.value = "1"
        _regularTranslationPrice.value = "2,000원"
        _regularFoodPrice.value = "2,000원"
        _regularTotalPrice.value = "총 결제 금액 : 4,000원"

    }
}