package com.example.menupop.mainActivity

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.menupop.login.LoginResponseModel

class ExchangeViewModel : ViewModel() {
    val TAG = "ExchangeViewModel"
    private  var callback:((ArrayList<ExchangeDataClass.ExchangeDataClassItem>) -> Unit) ?= null
    val isPossible = MutableLiveData<Boolean>()
    var data = ArrayList<ExchangeDataClass.ExchangeDataClassItem>()
    val exchangeModel : ExchangeModel = ExchangeModel()
    private var _result = MutableLiveData<String>()
    val result :LiveData<String>
        get() = _result

    private var _formattedNumber = MutableLiveData<String>()
    val formattedNumber:LiveData<String>
        get() = _formattedNumber
    var checkCurrency = false
    fun requestExchangeRate(authKey : String){
        callback = {
            exchangeDataClassItems -> data = exchangeDataClassItems
            Log.d(TAG, "requestExchangeRate: ${data}")
//            isPossible.value = data != null
        }
        _result.value = "0"
        _formattedNumber.value = "1000"
        exchangeModel.requestExchangeRate(authKey, callback!!)

    }
    fun selection(base : String,target : String){
        checkCurrency = base != "선택" && target != "선택"
    }
    fun exchange(amount : String,baseCurrency: String, targetCurrency: String){
        if(amount.isEmpty()){
            return
        } else if(!checkCurrency){
            return
        }
        val regex = "\\((.*?)\\)".toRegex()
        val baseMatchResult = regex.find(baseCurrency)
        val targetMatchResult = regex.find(targetCurrency)
        val amounts = amount.replace(",","").toDouble()
        val baseCurrencys = baseMatchResult?.groupValues?.get(1)
        val targetCurrencys = targetMatchResult?.groupValues?.get(1)
        Log.d(TAG, "exchange: ${amounts} ${baseCurrencys} ${targetCurrencys}")
        calculateExchange(amounts,baseCurrencys!!,targetCurrencys!!)

    }
    fun calculateExchange(amount: Double?, baseCurrency: String, targetCurrency: String) {
        Log.d(TAG, "calculateExchange: ${amount}")
        val baseRate = data.find { it.cur_unit == baseCurrency }?.deal_bas_r?.replace(",","")
        val targetRate = data.find { it.cur_unit == targetCurrency }?.deal_bas_r?.replace(",","")
        Log.d(TAG, "calculateExchange:${baseRate} ${targetRate} ")

        if (baseRate != null && targetRate != null && amount != null) {
            _result.value = String.format("%.1f",amount / baseRate.toDouble() * targetRate.toDouble())
            Log.d(TAG, "calculateExchange: ${(amount / baseRate.toDouble() * targetRate.toDouble()).toString()}")
        }
    }

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


}