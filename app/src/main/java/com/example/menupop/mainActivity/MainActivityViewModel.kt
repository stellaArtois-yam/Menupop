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
     * 환율 정보
     */
    private val _exchangeRateMap = MutableLiveData<ExchangeRateData>()
    private val _isExchangeAvailable = MutableLiveData<Boolean>()
    val isExchangeAvailable : LiveData<Boolean>
        get() = _isExchangeAvailable

    private val _targetCurrency = MutableLiveData<Int>()
    val targetCurrency : LiveData<Int>
        get() = _targetCurrency



}