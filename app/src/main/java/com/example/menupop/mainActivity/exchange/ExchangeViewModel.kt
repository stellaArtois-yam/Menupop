package com.example.menupop.mainActivity.exchange

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExchangeViewModel : ViewModel() {
    val TAG = "ExchangeViewModel"

    private var callback: ((ExchangeRateResponseDTO) -> Unit)? = null
    val exchangeModel = ExchangeModel()

    val isPossible = MutableLiveData<Boolean>()

    lateinit var data: ExchangeRateResponseDTO

    private var _targetCurrency = MutableLiveData<String>()
    val targetCurrency: LiveData<String>
        get() = _targetCurrency

    private var _sourceCurrency = MutableLiveData<String>()
    val sourceCurrency: LiveData<String>
        get() = _sourceCurrency

    private var _targetCurrencyUnit = MutableLiveData<String>()
    val targetCurrencyUnit : LiveData<String>
        get() = _targetCurrencyUnit

    private var _sourceCurrencyUnit = MutableLiveData<String>()
    val sourceCurrencyUnit : LiveData<String>
        get() = _sourceCurrencyUnit

    fun setSourceUnit(unit : String){
        _sourceCurrencyUnit.value= unit
    }

    fun setTargetUnit(unit : String){
        _targetCurrencyUnit.value = unit
    }

    fun init() {
        _targetCurrency.value = "0"
        _sourceCurrency.value = "1000"
        _notifiedExchangeRate.value = "0"
        _sourceCurrencyUnit.value = ""
        _targetCurrencyUnit.value = ""
    }

    /**
     * 환율 정보 요청
     */
    fun requestExchangeRate(authKey: String, baseRate: String) {
        callback = { exchangeDataClassItems ->
            data = exchangeDataClassItems
            Log.d(TAG, "requestExchangeRate: ${data}")
        }
        val regex = "\\((.*?)\\)".toRegex()
        val baseMatchResult = regex.find(baseRate)
        val baseCurrencys = baseMatchResult?.groupValues?.get(1)
        exchangeModel.requestExchangeRate(authKey, baseCurrencys!!, callback!!)

    }


    /**
     * 고시 환율 변환
     */
    private val _notifiedExchangeRate = MutableLiveData<String>()
    val notifiedExchangeRate: LiveData<String>
        get() = _notifiedExchangeRate

    fun exchange(amount: String, targetCurrency: String) {
        if (amount.isEmpty()) {
            return
        } else if (targetCurrency == "선택") {
            return
        }

        val regex = "\\((.*?)\\)".toRegex()
        val targetMatchResult = regex.find(targetCurrency)
        val amounts = amount.replace(",", "").toDouble()
        val targetCurrency = targetMatchResult?.groupValues?.get(1)

        Log.d(TAG, "exchange: ${amounts} ${targetCurrency}")

        calculateExchange(amounts, targetCurrency!!)

    }


    fun calculateExchange(amount: Double?, targetCurrency: String) {
        val targetRate = data.conversionRates.get(targetCurrency)

        if (targetRate != null && amount != null) {
            _targetCurrency.value = addCommasToNumber(amount * targetRate.toDouble())
            var notifiedRate = calculateNotifiedExchangeRate(targetCurrency, targetRate)

            if(targetCurrency == "JPY" || targetCurrency == "VND"){
                _notifiedExchangeRate.value = addCommasToNumber(notifiedRate) + "원 (100)"
            }else{
                _notifiedExchangeRate.value =  addCommasToNumber(notifiedRate) + "원"
            }

        }
    }

    fun calculateNotifiedExchangeRate(targetCurrency: String, targetRate: Double): Double {
        if (targetCurrency == "VND" || targetCurrency == "JPY") {
            var result = 100 / targetRate
            Log.d(TAG, "VND/JPY: (100) $result")
            return result
        } else {
            var result = 1 / targetRate
            Log.d(TAG, "others: $result")
            return result
        }
    }

    fun updateFormattedNumber(input: String, unit : String?) {
        val number = input.replace(",", "")
        val formattedText = formatNumberWithCommas(number)
        _sourceCurrency .value = formattedText + unit
    }

    private fun formatNumberWithCommas(number: String): String {
        val regex = "(\\d)(?=(\\d{3})+\$)".toRegex()
        return number.replace(regex, "$1,")
    }

    fun exchangeRateApplicationStatus(sharedPreferences: SharedPreferences, status: Boolean) {
        exchangeModel.exchangeRateApplicationStatus(sharedPreferences, status)

    }

    fun addCommasToNumber(number: Double): String {

        return String.format("%,.2f", number)
    }


}