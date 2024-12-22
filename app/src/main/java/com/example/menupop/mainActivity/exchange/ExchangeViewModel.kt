package com.example.menupop.mainActivity.exchange

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExchangeViewModel : ViewModel() {
    companion object {
        const val TAG = "ExchangeViewModel"
    }

    private val exchangeModel = ExchangeModel()

    private var _exchangeData = MutableLiveData<ExchangeRateResponseDTO>() //data 갱신 시 감지
    val exchangeData : LiveData<ExchangeRateResponseDTO>
        get() = _exchangeData

    var today: String = ""

    private var _targetCurrency = MutableLiveData("")
    val targetCurrency: LiveData<String>
        get() = _targetCurrency

    private var _sourceCurrency = MutableLiveData("")
    val sourceCurrency: LiveData<String>
        get() = _sourceCurrency

    private val _notifiedExchangeRate = MutableLiveData("")
    val notifiedExchangeRate: LiveData<String>
        get() = _notifiedExchangeRate

    private var _targetCurrencyUnit = MutableLiveData("")

    private var _sourceCurrencyUnit = MutableLiveData("")
    val sourceCurrencyUnit : LiveData<String>
        get() = _sourceCurrencyUnit

    fun setSourceUnit(unit: String) {
        _sourceCurrencyUnit.value = unit
        if(_sourceCurrency.value == ""){
            _sourceCurrency.value = "1"
        }
        updateFormattedNumber(_sourceCurrency.value!!)
    }

    fun setTargetUnit(unit: String) {
        _targetCurrencyUnit.value = unit
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun init() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 기준 환율")
        today = LocalDate.now().format(formatter)
    }

    // 기준 화폐로 환율 요청
    suspend fun requestExchangeRate(authKey: String, baseRate: String) {
        val regex = "\\((.*?)\\)".toRegex()
        val baseMatchResult = regex.find(baseRate)
        val baseCurrencies = baseMatchResult?.groupValues?.get(1)

        viewModelScope.launch {
            val result = exchangeModel.requestExchangeRate(authKey, baseCurrencies!!)
            if (result.isSuccess) {
                _exchangeData.value = result.getOrDefault(
                    ExchangeRateResponseDTO(
                        baseCode = "",
                        conversionRates = emptyMap(),
                        documentation = "",
                        result = "failed",
                        termsOfUse = "",
                        timeLastUpdateUnix = 0,
                        timeLastUpdateUtc = "",
                        timeNextUpdateUnix = 0,
                        timeNextUpdateUtc = ""
                    )
                )
            }
        }
    }

    fun exchange(amount: String, targetCurrencyUnit: String) {
        if (targetCurrencyUnit == "선택") return
        val sanitizedAmount = amount.ifEmpty { "1" }

        val regex = "\\((.*?)\\)".toRegex()
        val targetMatchResult = regex.find(targetCurrencyUnit)
        val amounts = sanitizedAmount.replace(",", "").toDouble()
        val targetCurrency = targetMatchResult?.groupValues?.get(1)

        calculateExchange(amounts, targetCurrency!!)
    }


    private fun calculateExchange(amount: Double?, targetCurrency: String) {
        val targetRate = _exchangeData.value!!.conversionRates[targetCurrency]

        if (targetRate != null && amount != null) {
            _targetCurrency.value = addCommasToNumber(amount * targetRate.toDouble()) + _targetCurrencyUnit.value
            val notifiedRate = calculateNotifiedExchangeRate(targetRate)
            _notifiedExchangeRate.value =
                addCommasToNumber(notifiedRate) + _sourceCurrencyUnit.value + " = 1" + _targetCurrencyUnit.value
        }
    }

    private fun calculateNotifiedExchangeRate(targetRate: Double): Double {
        return 1 /targetRate
    }

    fun updateFormattedNumber(input: String) {
        val number = input.replace(",", "")
        val formattedText = formatNumberWithCommas(number)
        _sourceCurrency.value = formattedText
    }

    private fun formatNumberWithCommas(number: String): String {
        val regex = "(\\d)(?=(\\d{3})+\$)".toRegex()
        return number.replace(regex, "$1,")
    }

    private fun addCommasToNumber(number: Double): String {
        return String.format("%,.2f", number)
    }

}