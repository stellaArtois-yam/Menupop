package com.example.menupop.mainActivity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable

import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.menupop.R
import com.example.menupop.mainActivity.profile.KakaoPayReadyResponseDTO
import com.example.menupop.mainActivity.profile.TicketSaveDTO
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.profile.KakaoPayWebView
import com.example.menupop.mainActivity.profile.ProfileSelectionDTO
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

class MainActivityViewModel(private val application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "mainActivityViewModel"
    }

    private val mainActivityModel = MainActivityModel(application)

    private val _rewardedAd = MutableLiveData<RewardedAd>()
    val rewardedAd: LiveData<RewardedAd>
        get() = _rewardedAd

    private val _searchFood = MutableLiveData<ArrayList<String>>()
    val searchFood: LiveData<ArrayList<String>>
        get() = _searchFood

    private val _foodPreferenceList = MutableLiveData<FoodPreferenceDataClass>()
    val foodPreferenceList: LiveData<FoodPreferenceDataClass>
        get() = _foodPreferenceList

    private val _registerResult = MutableLiveData<Boolean?>()
    val registerResult: LiveData<Boolean?>
        get() = _registerResult

    private val _accountWithdrawal = MutableLiveData<String>()

    val accountWithdrawal: LiveData<String>
        get() = _accountWithdrawal

    private var _isFreeTicket = MutableLiveData(false) // dialog 티켓 상태 결정
    val isFreeTicket: LiveData<Boolean>
        get() = _isFreeTicket


    private val _isLoaded = MutableLiveData<String>() //유저 정보 로드
    val isLoaded: LiveData<String>
        get() = _isLoaded

    private val _deletedResult = MutableLiveData<Boolean?>()
    val deletedResult: LiveData<Boolean?>
        get() = _deletedResult

    private val _userInformation = MutableLiveData<UserInformationDTO>()
    val userInformation: LiveData<UserInformationDTO>
        get() = _userInformation

    private val _profileImage = MutableLiveData<Drawable?>()
    val profileImage: MutableLiveData<Drawable?>
        get() = _profileImage

    fun getProfileList(resources: Resources): ArrayList<ProfileSelectionDTO> {
        val imageNames = resources.getStringArray(R.array.profile)
        val imageList: ArrayList<ProfileSelectionDTO> = ArrayList()

        for (name in imageNames) {
            val imageName = resources.getIdentifier(name, "drawable", application.packageName)
            val image = ResourcesCompat.getDrawable(resources, imageName, null)
            imageList.add(ProfileSelectionDTO(image!!))
        }

        return imageList
    }


    fun checkFoodTicketEmpty(): Boolean {
        if (userInformation.value?.foodTicket!! > 0 || userInformation.value?.freeFoodTicket!! > 0) {
            return false
        }
        return true
    }

    fun setTicketStatus(boolean: Boolean) {
        _isFreeTicket.value = boolean
    }

    suspend fun foodPreferenceRegister(foodName: String, classification: String) {
        viewModelScope.launch {
            val result = mainActivityModel.foodPreferenceRegister(
                _identifier.value!!,
                foodName,
                classification
            )
            Log.d(TAG, "foodPreferenceRegister: ${result.trim() == "success"}")
            _registerResult.value = result.trim() == "success"
            searchFood.value?.clear()
        }
    }

    fun initializeRegisterResult(){
        _registerResult.value = null
    }

    fun checkingTranslationTicket(): Boolean {
        Log.d(
            TAG,
            "checkingTranslationTicket: translationTicket(${userInformation.value!!.translationTicket}), freeTranslationTicket(${_userInformation.value!!.freeTranslationTicket})"
        )
        return userInformation.value!!.translationTicket > 0 || _userInformation.value!!.freeTranslationTicket > 0
    }


    fun deleteFoodPreference(foodName: String) {
        viewModelScope.launch {
            val result = mainActivityModel.deleteFoodPreference(_identifier.value!!, foodName)
            Log.d(TAG, "deleteFoodPreference: $result")
            when(result.trim()){
                "success" -> _deletedResult.value = true
                else -> _deletedResult.value = false
            }
        }
    }

    fun initializeDeleteResult(){
        _deletedResult.value = null
    }

    fun searchFood(query: String) {
        viewModelScope.launch {
            val response = mainActivityModel.searchFood(query)
            if (response.result == "success") {
                val foodPreferenceLists = ArrayList<String>()
                _userInformation.value?.foodPreference?.forEach {
                    foodPreferenceLists.add(it.foodName)
                }

                response.foodList.removeAll(foodPreferenceLists.toSet())
            } else {
                Log.d(TAG, "searchFood: 찾을 수 없음")
            }
            _searchFood.value = response.foodList
            Log.d(TAG, "searchFood: ${response.foodList}")
        }
    }

    private val _identifier = MutableLiveData<Int>()
    val identifier: LiveData<Int>
        get() = _identifier

    fun setIdentifier(identifier: Int) {
        _identifier.value = identifier
    }


    suspend fun requestUserInformation(identifier: Int) {
        viewModelScope.launch {
            _userInformation.value = mainActivityModel.requestUserInformation(identifier)
            _isLoaded.value = _userInformation.value!!.result
            getFoodPreference()
            Log.d(TAG, "requestUserInformation: ${_userInformation.value}")
        }
    }

    suspend fun loadAd(key: String) {
        viewModelScope.launch {
            val ad = mainActivityModel.requestAd(key)
            Log.d(TAG, "loadAd: $ad")
            _rewardedAd.value = ad
        }
    }

    private val _isChangedProfile = MutableLiveData<Boolean>()
    val isChangedProfile: LiveData<Boolean>
        get() = _isChangedProfile

    fun saveSelectedProfile(
        imageName: String,
        sharedPreferences: SharedPreferences,
        resources: Resources
    ) {
        val result = mainActivityModel.saveSelectedProfile(imageName, sharedPreferences)
        if (result == "success") {
            val image = resources.getIdentifier(imageName, "drawable", application.packageName)
            _profileImage.value = ResourcesCompat.getDrawable(resources, image, null)
            _isChangedProfile.value = true
        } else {
            Log.d(TAG, "saveSelectedProfile not success")
        }
    }


    fun getProfileImage(sharedPreferences: SharedPreferences, resources: Resources) {
        val getResult = mainActivityModel.getProfileImage(sharedPreferences)

        if (getResult != null) {
            val image = resources.getIdentifier(getResult, "drawable", application.packageName)
            _profileImage.value = ResourcesCompat.getDrawable(resources, image, null)
            _isChangedProfile.value = false
        } else {
            _profileImage.value = null
        }

    }

    suspend fun rewardedSuccess() {
        viewModelScope.launch {
            val result = mainActivityModel.updateRewardQuantity(_identifier.value!!)
            if (result == "success") {
                _userInformation.value!!.availableReward += 1
                _userInformation.value!!.dailyReward -= 1
            } else {
                Log.d(TAG, "rewardedSuccess failed: ")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun withdrawal(sharedPreferences: SharedPreferences) {
        val email = userInformation.value?.email
        val id = userInformation.value?.id
        val localDate: LocalDate = LocalDate.now()

        viewModelScope.launch {
            val result = mainActivityModel.withdrawal(
                _identifier.value!!,
                email!!,
                id!!,
                localDate.toString()
            )
            _accountWithdrawal.value = result
            if (result == "success") {
                logout(sharedPreferences)
            }
        }
    }

    suspend fun getFoodPreference() {
        viewModelScope.launch {
            val foodPreference = mainActivityModel.getFoodPreference(_identifier.value!!)
            Log.d(TAG, "getFoodPreference: $foodPreference")
            _foodPreferenceList.value = foodPreference
            _userInformation.value!!.foodPreference = foodPreference.foodList
        }
    }


    private val _paymentType = MutableLiveData<String>()

    private val _translationAmount = MutableLiveData<Int>()
    val translationAmount: LiveData<Int>
        get() = _translationAmount

    private val _translationPrice = MutableLiveData<String>()
    val translationPrice: LiveData<String>
        get() = _translationPrice

    private val _foodAmount = MutableLiveData<Int>()
    val foodAmount: LiveData<Int>
        get() = _foodAmount

    private val _foodPrice = MutableLiveData<String>()
    val foodPrice: LiveData<String>
        get() = _foodPrice

    private val _totalPrice = MutableLiveData<String>()

    val totalPrice: LiveData<String>
        get() = _totalPrice

    private var _totalPriceForPay = MutableLiveData<String>()

    private var _usedRewards = MutableLiveData<Int>()

    fun updatePaymentType(type: String) {
        Log.d(TAG, "updatePaymentType: $type")
        _paymentType.value = type
    }

    fun adjustTicketQuantity(ticketType: String, operator: String) {
        when {
            ticketType == "translation" && operator == "+"
            -> changeTicketAmount(
                _translationAmount,
                _translationPrice,
                _foodAmount
            )

            ticketType == "food" && operator == "+"
            -> changeTicketAmount(
                _foodAmount,
                _foodPrice,
                _translationAmount
            )

            ticketType == "translation" && operator == "-" && _translationAmount.value!! > 0
            -> changeTicketAmount(
                _translationAmount,
                _translationPrice,
                _foodAmount,
                -1
            )

            ticketType == "food" && operator == "-" && _foodAmount.value!! > 0
            -> changeTicketAmount(
                _foodAmount,
                _foodPrice,
                _translationAmount,
                -1
            )

            else -> Log.d(TAG, "adjustRegularTicketQuantity: not match case")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun useRewards() {
        // 현재 값과 리워드 정보 확인
        val currentPrice = _totalPriceForPay.value?.toIntOrNull() ?: 0
        val availableReward = _userInformation.value?.availableReward ?: 0

        val deduction = minOf(currentPrice, availableReward * 2000) // 최대 차감 금액
        _usedRewards.value = deduction / 2000  // 실제 사용한 리워드 개수

        val updatedPrice = maxOf(0, currentPrice - deduction)
        _totalPriceForPay.value = updatedPrice.toString()

        if (updatedPrice > 0) {
            createPaymentRequest() //결제 금액이 남으면 카카오페이로 결제
            updatePaymentType("together")
        } else {
            val item = itemName(_translationAmount, _foodAmount)
            updatePaymentType("reward")

            val time = LocalDateTime.now().toString()
            viewModelScope.launch {
                savePaymentHistory(
                    identifier = _identifier.value!!,
                    tid = Calendar.getInstance().hashCode().toString(),
                    paymentType = "REWARD",
                    item = item,
                    price = 0,
                    approveAt = time
                )
            }
        }
    }


    private fun changeTicketAmount(
        ticketAmount: MutableLiveData<Int>,
        ticketPrice: MutableLiveData<String>,
        otherTicketAmount: MutableLiveData<Int>,
        change: Int = 1
    ) {
        val quantity = ticketAmount.value!! + change
        ticketAmount.value = quantity

        val dec = DecimalFormat("#,###")

        val price = quantity * 2000
        ticketPrice.value = "${dec.format(price)}원"

        val otherTicketPrice = otherTicketAmount.value!! * 2000

        val totalPrice = price + otherTicketPrice

        _totalPriceForPay.value = totalPrice.toString()

        _totalPrice.value = "총 결제 금액 : ${dec.format(totalPrice)}원"
    }


    private fun countTicket(
        ticketAmount: MutableLiveData<Int>,
        otherTicketAmount: MutableLiveData<Int>
    ): Int {
        return if (ticketAmount.value!! > 0 && otherTicketAmount.value!! > 0) {
            ticketAmount.value!! + otherTicketAmount.value!!
        } else {
            1
        }
    }

    private fun itemName(
        ticketAmount: MutableLiveData<Int>,
        otherTicketAmount: MutableLiveData<Int>
    ): String {
        return if (ticketAmount.value!! > 0 && otherTicketAmount.value!! > 0) {
            val total = ticketAmount.value!! + otherTicketAmount.value!! - 1
            "번역 티켓 외 $total"

        } else if (ticketAmount.value!! > 0 && otherTicketAmount.value!! == 0) {
            "번역 티켓"
        } else {
            "음식 티켓"
        }
    }

    private val _isUseRewards = MutableLiveData<Boolean?>(null) // 리워드 사용 여부
    val isUseRewards: LiveData<Boolean?>
        get() = _isUseRewards

    private val _cancelBuyTicket = MutableLiveData<Boolean?>(null) // 결제 취소
    val cancelBuyTicket: LiveData<Boolean?>
        get() = _cancelBuyTicket

    private val _isFailedToBuyTicket = MutableLiveData<Boolean?>(null)
    val isFailedToBuyTicket: LiveData<Boolean?> // 결제 실패
        get() = _isFailedToBuyTicket

    private val _paymentReady = MutableLiveData<KakaoPayReadyResponseDTO>()
    val paymentReady: MutableLiveData<KakaoPayReadyResponseDTO> //초기 결제 준비 상태
        get() = _paymentReady

    private val _pgToken = MutableLiveData<String>() // 결제 승인 시 필요

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest() {
        val item = itemName(_translationAmount, _foodAmount)
        val quantity = countTicket(_translationAmount, _foodAmount)
        Log.d(TAG, "createPaymentRequest: 호출")

        viewModelScope.launch {
            val response = mainActivityModel.createPaymentRequest(
                userId = _identifier.value.toString(),
                item = item,
                quantity = quantity.toString(),
                totalAmount = _totalPriceForPay.value!!
            )
            Log.d(KakaoPayWebView.TAG, "createPaymentRequest response : $response")
            if (response.nextRedirectAppUrl != null) {
                _paymentReady.value = response
            } else {
                _paymentReady.value = KakaoPayReadyResponseDTO("N/A")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePgToken(token: String) {
        _pgToken.value = token

        if (_pgToken.value != "N/A") {
            //_pgToken이 업데이트 되면 결제 승인 요청
            requestApprovePayment(
                tid = _paymentReady.value!!.tid,
                userId = _identifier.value.toString(),
                pgToken = _pgToken.value!!
            )
        } else {
            _isFailedToBuyTicket.value = true
        }
    }

    fun initializeKakaoPayVariables() {
        Log.d(TAG, "initializeKakaoPayVariables 호출")
        _paymentReady.value = KakaoPayReadyResponseDTO("N/A")
        _pgToken.value = "N/A"
        _cancelBuyTicket.value = null
        _isFailedToBuyTicket.value = null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun requestApprovePayment(tid: String, userId: String, pgToken: String) {
        viewModelScope.launch {
            val result = mainActivityModel.requestApprovePayment(tid, userId, pgToken)

            if (result.approvedAt!!.isNotEmpty()) {
                // 결제 승인이 완료하면 db에 티켓 개수 수정 및 구매 이력 저장
                val paymentType = if (_paymentType.value == "together") {
                    result.paymentMethodType + "(REWARD)"
                } else {
                    result.paymentMethodType
                }
                savePaymentHistory(
                    identifier = result.partnerUserId!!.toInt(),
                    tid = result.tid!!,
                    paymentType = paymentType!!,
                    item = result.itemName!!,
                    price = result.amount!!.total.toInt(),
                    approveAt = result.approvedAt
                )
            } else {
                _isFailedToBuyTicket.value = true
            }
        }
    }


    fun initializeIsUseRewards() {
        _isUseRewards.value = null
        Log.d(TAG, "initializeIsUseRewards: ${_isUseRewards.value}")
    }

    private suspend fun requestCancelPayment(tid: String, cancelAmount: String) {
        viewModelScope.launch {
            val response = mainActivityModel.requestCancelPayment(tid, cancelAmount)
            when (response.status) {
                "CANCEL_PAYMENT" -> _cancelBuyTicket.value = true //성공
                "FAILED" -> _cancelBuyTicket.value = false //실패
            }
        }
    }

    private suspend fun savePaymentHistory(
        identifier: Int,
        tid: String,
        paymentType: String,
        item: String,
        price: Int,
        approveAt: String
    ) {

        val ticketSaveModel = TicketSaveDTO(
            identifier = identifier,
            tid = tid,
            paymentType = paymentType,
            item = item,
            price = price,
            approvedAt = approveAt,
            translationTicket = _translationAmount.value!!,
            foodTicket = _foodAmount.value!!
        )

        viewModelScope.launch {
            val response = mainActivityModel.savePaymentHistory(ticketSaveModel)
            if (response.result == "success") {

                _userInformation.value!!.foodTicket += _foodAmount.value!!

                _userInformation.value!!.translationTicket += translationAmount.value!!

                when (_paymentType.value) {
                    "kakao" -> _isFailedToBuyTicket.value = false
                    "reward" -> {
                        val quantity = countTicket(_foodAmount, _translationAmount)
                        updateTicketQuantity("available_reward", "-", quantity)
                    }

                    "together" -> updateTicketQuantity(
                        "available_reward",
                        "-",
                        _usedRewards.value!!
                    )
                }
            } else {
                if (_paymentType.value != "reward") {
                    requestCancelPayment(tid, price.toString()) //결제 취소
                } else {
                    _isUseRewards.value = false
                }
            }
        }
    }

    suspend fun updateTicketQuantity(ticketType: String, operator: String, quantity: Int) {
        viewModelScope.launch {
            val result = mainActivityModel.updateTicketQuantity(
                identifier = _identifier.value!!,
                ticketType = ticketType,
                operator = operator,
                quantity = quantity
            )
            if (result == "success") {
                when (ticketType) {
                    "free_translation_ticket" -> _userInformation.value!!.freeTranslationTicket -= 1
                    "free_food_ticket" -> _userInformation.value!!.freeFoodTicket -= 1
                    "translation_ticket" -> _userInformation.value!!.translationTicket -= 1
                    "food_ticket" -> _userInformation.value!!.foodTicket -= 1
                    "available_reward" -> {
                        _userInformation.value!!.availableReward -= quantity

                        when(_paymentType.value){
                            "reward" -> _isUseRewards.value = true
                            "together" -> _isFailedToBuyTicket.value = false
                        }

                        _usedRewards.value = 0
                        _totalPriceForPay.value = "4000"
                    }
                    else -> Log.d(TAG, "updateTicketQuantity not match")
                }

            } else {
                Log.d(TAG, "updateTicketQuantity result failed: ")
            }
        }
    }


    fun logout(sharedPreferences: SharedPreferences) {
        mainActivityModel.logout(sharedPreferences)
    }


    fun scheduleMidnightWork(application: Application) {
        val callback: ((Boolean) -> Unit) = {
            if (it) {
                Log.d(TAG, "scheduleMidnightWork: true")
                _userInformation.value!!.freeTranslationTicket = 3
                _userInformation.value!!.dailyReward = 3
                _userInformation.value!!.availableReward = 0
            } else {
                Log.d(TAG, "scheduleMidnightWork: false")
            }

        }
        mainActivityModel.scheduleMidnightWork(application, callback)
    }


    fun getDisplaySize(width: Float, height: Float): Pair<Int, Int> {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val x: Int
        val y: Int
        if (Build.VERSION.SDK_INT < 30) {
            val size = Point()

            x = (size.x * width).toInt()
            y = (size.y * height).toInt()

        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            x = (rect.width() * width).toInt()
            y = (rect.height() * height).toInt()
        }
        return Pair(x, y)
    }


    init {
        _translationAmount.value = 1
        _foodAmount.value = 1
        _translationPrice.value = "2,000원"
        _foodPrice.value = "2,000원"
        _totalPrice.value = "총 결제 금액 : 4,000원"
        _totalPriceForPay.value = "4000"
    }


}