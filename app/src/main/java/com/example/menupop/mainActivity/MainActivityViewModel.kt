package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
    private val _regularTranslationAmount = MutableLiveData<Int>()
    val regularTranslationAmount : LiveData<Int>
        get() = _regularTranslationAmount

    private val _regularTranslationPrice = MutableLiveData<String>()
    val regularTranslationPrice : LiveData<String>
        get() = _regularTranslationPrice

    private val _regularFoodAmount = MutableLiveData<Int>()
    val regularFoodAmount : LiveData<Int>
        get() = _regularFoodAmount

    private val _regularFoodPrice = MutableLiveData<String>()
    val regularFoodPrice : LiveData<String>
        get() = _regularFoodPrice

    private val _regularTotalPrice = MutableLiveData<String>()

    val regularTotalPrice : LiveData<String>
        get() = _regularTotalPrice


    fun addTranslationTicket(){
        Log.d(TAG, "addTranslationTicket: 호출")
       _regularTranslationAmount.value  = _regularTranslationAmount.value!! + 1
        Log.d(TAG, "개수 : ${_regularTranslationAmount.value}")
        val price = _regularTranslationAmount.value!! * 2000
        _regularTranslationPrice.value = "${price}원"
        Log.d(TAG, "price: ${price}")
        val totalPrice = _regularTranslationPrice.value + _regularFoodPrice
        _regularTotalPrice.value = "총 결제 금액 : ${totalPrice}원"
    }

    fun removeTranslationTicket(){

        if(_regularFoodAmount.value!! > 0){
            _regularTranslationAmount.value  = _regularTranslationAmount.value!! - 1
            val price = _regularTranslationAmount.value!! * 2000
            _regularTranslationPrice.value = "${price}원"
            val totalPrice = _regularTranslationPrice.value + _regularFoodPrice
            _regularTotalPrice.value = "총 결제 금액 : ${totalPrice}원"
        }

    }

    fun addFoodTicket(){
        _regularFoodAmount.value = _regularFoodAmount.value!! + 1
        val price = _regularFoodAmount.value!! * 2000
        _regularFoodPrice.value = "${price}원"
        val totalPrice = _regularTranslationPrice.value + _regularFoodPrice
        _regularTotalPrice.value = "총 결제 금액 : ${totalPrice}원"


    }

    fun removeFoodTicket(){
        if(_regularFoodAmount.value!! > 0){
            _regularFoodAmount.value = _regularFoodAmount.value!! - 1
            val price = _regularFoodAmount.value!! * 2000
            _regularFoodPrice.value = "${price}원"
            val totalPrice = _regularTranslationPrice.value + _regularFoodPrice
            _regularTotalPrice.value = "총 결제 금액 : ${totalPrice}원"
        }
    }



    init {
        _regularTranslationAmount.value = 1
        Log.d(TAG, "엥 : ${_regularTranslationAmount.value}")
        _regularFoodAmount.value = 1
        _regularTranslationPrice.value = "2,000원"
        _regularFoodPrice.value = "2,000원"
        _regularTotalPrice.value = "총 결제 금액 : 4,000원"

    }
}