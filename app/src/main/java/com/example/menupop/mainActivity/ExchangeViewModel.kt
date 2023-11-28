package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExchangeViewModel : ViewModel() {
    val TAG = "ExchangeViewModel"
    private  var callback:((ExchangeRateDataClass) -> Unit) ?= null
    val isPossible = MutableLiveData<Boolean>()
    lateinit var data : ExchangeRateDataClass
    val exchangeModel : ExchangeModel = ExchangeModel()
    private var _result = MutableLiveData<String>()
    val result :LiveData<String>
        get() = _result

    private var _formattedNumber = MutableLiveData<String>()
    val formattedNumber:LiveData<String>
        get() = _formattedNumber
    fun init(){
        _result.value = "0"
        _formattedNumber.value = "1000"
    }
    fun requestExchangeRate(authKey : String,baseRate:String){
        callback = {
            exchangeDataClassItems -> data = exchangeDataClassItems
            Log.d(TAG, "requestExchangeRate: ${data}")
//            isPossible.value = data != null
        }
        val regex = "\\((.*?)\\)".toRegex()
        val baseMatchResult = regex.find(baseRate)
        val baseCurrencys = baseMatchResult?.groupValues?.get(1)
        exchangeModel.requestExchangeRate(authKey,baseCurrencys!! ,callback!!)

    }
    fun exchange(amount : String,targetCurrency: String,standardRate : String){
        if(amount.isEmpty()){
            return
        } else if(targetCurrency == "선택"){
            return
        } else if(!Regex("^\\d+$").matches(standardRate)){
            return
        }
        val regex = "\\((.*?)\\)".toRegex()
        val targetMatchResult = regex.find(targetCurrency)
        val amounts = amount.replace(",","").toDouble()
        val targetCurrencys = targetMatchResult?.groupValues?.get(1)
        Log.d(TAG, "exchange: ${amounts} ${targetCurrencys}")
        if(standardRate.isEmpty()||standardRate == "0"){
            calculateExchange(amounts,targetCurrencys!!)
            return
        }
        customExchangeRate(amounts,standardRate.toDouble())

    }
    fun customExchangeRate(amount: Double?,standardRate: Double) {
//
        if (amount != null) {
            _result.value = addCommasToNumber(amount  * standardRate)
            Log.d(TAG, "customExchangeRate: ${(amount * standardRate).toString()}")
        }
    }
    fun calculateExchange(amount: Double?,targetCurrency: String) {
        val targetRate = data.conversionRates.get(targetCurrency)
//
        if (targetRate != null && amount != null) {
            _result.value = addCommasToNumber(amount  * targetRate.toDouble())
            Log.d(TAG, "calculateExchange: ${(amount * targetRate.toDouble()).toString()}")
        }
    }
    //https://v6.exchangerate-api.com

    fun updateFormattedNumber(input: String) {
        Log.d(TAG, "updateFormattedNumber: ${input}")
        val number = input.replace(",", "")
        val formattedText = formatNumberWithCommas(number)
        _formattedNumber.value = formattedText
    }

    private fun formatNumberWithCommas(number: String): String {
        val regex = "(\\d)(?=(\\d{3})+\$)".toRegex()
        return number.replace(regex, "$1,")
    }
    fun exchangeRateApplicationStatus(sharedPreferences: SharedPreferences,status : Boolean){
        exchangeModel.exchangeRateApplicationStatus(sharedPreferences,status)

    }
    fun addCommasToNumber(number: Double): String {
        return String.format("%,.1f", number)
    }


}