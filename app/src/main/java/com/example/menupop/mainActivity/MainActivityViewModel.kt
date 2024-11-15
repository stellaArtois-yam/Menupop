package com.example.menupop.mainActivity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable

import android.os.Build
import android.util.Log
import android.view.View
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

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean>
        get() = _registerResult

    private val _accountWithdrawal = MutableLiveData<String>()

    val accountWithdrawal: LiveData<String>
        get() = _accountWithdrawal

    // 받은 리워드
    private val _todayRewarded = MutableLiveData<Int>()

    val todayRewarded: LiveData<Int>
        get() = _todayRewarded

    private val _countrySelection = MutableLiveData<String>()

    val countrySelection: LiveData<String> get() = _countrySelection

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


    /**
     * 메인
     */
    private val _isLoaded = MutableLiveData<String>()
        .apply { value = "yet" }
    val isLoaded: LiveData<String>
        get() = _isLoaded

    private val _deletedResult = MutableLiveData<Boolean>()
    val deletedResult: LiveData<Boolean>
        get() = _deletedResult

    private val _userInformation = MutableLiveData<UserInformationDTO>()
    val userInformation: LiveData<UserInformationDTO>
        get() = _userInformation

    private val _profileImage = MutableLiveData<Drawable?>()
    val profileImage: MutableLiveData<Drawable?>
        get() = _profileImage


    suspend fun updateTicketQuantity(ticketType: String, operator: String, quantity: Int) {
        viewModelScope.launch {
            val result = mainActivityModel.updateTicketQuantity(
                _identifier.value!!,
                ticketType,
                operator,
                quantity
            )
            if (result == "success") {
                if (operator == "-") {
                    when (ticketType) {
                        "free_translation_ticket" -> _userInformation.value!!.freeTranslationTicket -= 1
                        "free_food_ticket" -> _userInformation.value!!.freeFoodTicket -= 1
                        "translation_ticket" -> _userInformation.value!!.translationTicket -= 1
                        "food_ticket" -> _userInformation.value!!.foodTicket -= 1
                        "have_rewarded" -> buyTicketUsingReward(quantity)
                        else -> Log.d(TAG, "updateTicketQuantity not match")

                    }
                }
                _changeTicket.value = "success"
                Log.d(TAG, "updateTicketQuantity after use: ${_userInformation.value}")

            } else {
                Log.d(TAG, "updateTicketQuantity result failed: ")
            }
        }
    }

    private fun buyTicketUsingReward(quantity: Int) {
        Log.d(TAG, "buyTicketUsingReward")
        _userInformation.value!!.haveRewarded = _userInformation.value!!.haveRewarded - quantity
        _userInformation.value!!.foodTicket =
            _userInformation.value!!.foodTicket + _rewardFoodAmount.value!!
        _userInformation.value!!.translationTicket =
            _userInformation.value!!.translationTicket + _rewardTranslationAmount.value!!

    }

    suspend fun foodPreferenceRegister(foodName: String, classification: String) {
        viewModelScope.launch {
            val result = mainActivityModel.foodPreferenceRegister(
                _identifier.value!!,
                foodName,
                classification
            )
            _registerResult.value = result == "success"
            searchFood.value?.clear()
            Log.d(TAG, "foodPreferenceRegister: ${searchFood.value}")
        }
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
            _deletedResult.value = result == "success"
        }
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
            _todayRewarded.value = 3 - _userInformation.value!!.dailyReward
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
            Log.d(TAG, "saveSelectedProfile: $result")
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
            Log.d(TAG, "getProfileImage not null")
        } else {
            _profileImage.value = null
            Log.d(TAG, "getProfileImage is null")
        }

    }

    suspend fun rewardedSuccess() {
        viewModelScope.launch {
            val result = mainActivityModel.updateRewardQuantity(_identifier.value!!)
            if (result == "success") {
                _userInformation.value!!.haveRewarded += 1
                _userInformation.value!!.dailyReward -= 1
                _todayRewarded.value = 3 - _userInformation.value!!.dailyReward
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
            _foodPreferenceList.value = foodPreference
            _userInformation.value!!.foodPreference = foodPreference.foodList
        }
    }


    /**
     * 티켓 구매 + 다이얼로그
     */

    private val _paymentType = MutableLiveData<String>()

    private val _regularTranslationAmount = MutableLiveData<Int>()
    val regularTranslationAmount: LiveData<Int>
        get() = _regularTranslationAmount

    private val _rewardTranslationAmount = MutableLiveData<Int>()
    val rewardTranslationAmount: LiveData<Int>
        get() = _rewardTranslationAmount

    private val _regularTranslationPrice = MutableLiveData<String>()
    val regularTranslationPrice: LiveData<String>
        get() = _regularTranslationPrice

    private val _regularFoodAmount = MutableLiveData<Int>()
    val regularFoodAmount: LiveData<Int>
        get() = _regularFoodAmount

    private val _rewardFoodAmount = MutableLiveData<Int>()
    val rewardFoodAmount: LiveData<Int>
        get() = _rewardFoodAmount

    private val _regularFoodPrice = MutableLiveData<String>()
    val regularFoodPrice: LiveData<String>
        get() = _regularFoodPrice

    private val _regularTotalPrice = MutableLiveData<String>()

    val regularTotalPrice: LiveData<String>
        get() = _regularTotalPrice

    private val _totalPriceForPay = MutableLiveData<String>()

    fun updatePaymentType(type: String) {
        Log.d(TAG, "updatePaymentType: $type")
        _paymentType.value = type
    }

    fun adjustRegularTicketQuantity(ticketType: String, operator: String) {
        when {
            ticketType == "translation" && operator == "+"
            -> changeTicketAmount(
                _regularTranslationAmount,
                _regularTranslationPrice,
                _regularFoodAmount
            )

            ticketType == "food" && operator == "+"
            -> changeTicketAmount(_regularFoodAmount, _regularFoodPrice, _regularTranslationAmount)

            ticketType == "translation" && operator == "-" && _regularTranslationAmount.value!! > 0
            -> changeTicketAmount(
                _regularTranslationAmount,
                _regularTranslationPrice,
                _regularFoodAmount,
                -1
            )

            ticketType == "food" && operator == "-" && _regularFoodAmount.value!! > 0
            -> changeTicketAmount(
                _regularFoodAmount,
                _regularFoodPrice,
                _regularTranslationAmount,
                -1
            )

            else -> Log.d(TAG, "adjustRegularTicketQuantity: not match case")
        }
    }

    fun adjustRewardTicketQuantity(ticketType: String, operator: String) {
        when {
            ticketType == "translation" && operator == "+"
            -> incrementRewardAmount(_rewardTranslationAmount)

            ticketType == "food" && operator == "+"
            -> incrementRewardAmount(_rewardFoodAmount)

            ticketType == "translation" && operator == "-"
            -> decrementRewardAmount(_rewardTranslationAmount)

            ticketType == "food" && operator == "-"
            -> decrementRewardAmount(_rewardFoodAmount)

            else -> Log.d(TAG, "adjustRewardTicketQuantity: not match case")

        }
    }


    private fun incrementRewardAmount(amount: MutableLiveData<Int>) {
        amount.value = amount.value!! + 1
    }

    private fun decrementRewardAmount(amount: MutableLiveData<Int>) {
        if (amount.value!! > 0) {
            amount.value = amount.value!! - 1
        }
    }


    private val _isRewardExceeded = MutableLiveData<Boolean>()
    val isRewardExceeded: LiveData<Boolean>
        get() = _isRewardExceeded

    @RequiresApi(Build.VERSION_CODES.O)
    fun rewardPayment() {

        if (_userInformation.value!!.haveRewarded <
            (_rewardFoodAmount.value!! + _rewardTranslationAmount.value!!)) {

            _isRewardExceeded.value = true

        } else {
            _isRewardExceeded.value = false
            val item = itemName(_rewardTranslationAmount, _rewardFoodAmount)
            Log.d(TAG, "rewardPayment itemName: $item")

            val time = LocalDateTime.now().toString()
            viewModelScope.launch {
                savePaymentHistory(
                    _identifier.value!!,
                    Calendar.getInstance().hashCode().toString(),
                    "REWARD",
                    item,
                    0,
                    time
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
        Log.d(TAG, "totalPriceForPay: ${_totalPriceForPay.value}")

        _regularTotalPrice.value = "총 결제 금액 : ${dec.format(totalPrice)}원"
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


    private val _paymentReady = MutableLiveData<KakaoPayReadyResponseDTO>()
    val paymentReady: MutableLiveData<KakaoPayReadyResponseDTO>
        get() = _paymentReady

    private val _pgToken = MutableLiveData<String>()


    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePgToken(token: String) {

        Log.d(TAG, "updatePgToken: $token")
        _pgToken.value = token

        if (_pgToken.value != null) {
            Log.d(TAG, "updatePgToken: not null")
            completePayment(
                _paymentReady.value!!.tid,
                _identifier.value.toString(),
                _pgToken.value!!
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest() {
        Log.d(TAG, "createPaymentRequest: !!!")
        val item = itemName(_regularTranslationAmount, _regularFoodAmount)
        val quantity = countTicket(_regularTranslationAmount, _regularFoodAmount)

        viewModelScope.launch {
            val response = mainActivityModel.createPaymentRequest(
                _identifier.value.toString(), item, quantity.toString(),
                _totalPriceForPay.value!!
            )

            if (response != null) {
                _paymentReady.value = response
            } else {
                //결제 실패 toast 처리
            }

        }
    }

    fun setPaymentResponse() {
        _paymentReady.value = KakaoPayReadyResponseDTO("N/A")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completePayment(tid: String, userId: String, pgToken: String) {
        viewModelScope.launch {
            val result = mainActivityModel.requestApprovePayment(tid, userId, pgToken)
            if (result.approvedAt != null) {
                _paymentReady.value = KakaoPayReadyResponseDTO("N/A")
                // db에 티켓 개수도 수정
                // 구매 이력 저장
                savePaymentHistory(
                    result.partnerUserId!!.toInt(),
                    result.tid!!, result.paymentMethodType!!,
                    result.itemName!!, result.amount!!.total.toInt(), result.approvedAt
                )
            }
        }
    }

    private val _changeTicket = MutableLiveData<String>()

    val changeTicket: LiveData<String>
        get() = _changeTicket

    private val _failedBuyTicket = MutableLiveData<Boolean>()
    val failedBuyTicket: LiveData<Boolean>
        get() = _failedBuyTicket

    fun setChangeTicket() {
        _changeTicket.value = "init"
        Log.d(TAG, "setChangeTicket status: ${_changeTicket.value}")
    }

    private suspend fun requestCancelPayment(tid: String, cancelAmount: String) {
        viewModelScope.launch {
            val response = mainActivityModel.requestCancelPayment(tid, cancelAmount)
            when (response.status) {
                "CANCEL_PAYMENT" -> _failedBuyTicket.value = true
                "FAILED" -> _failedBuyTicket.value = false
            }
        }
    }

    private suspend fun savePaymentHistory(
        identifier: Int, tid: String, paymentType: String, item: String,
        price: Int, approveAt: String
    ) {
        var ticketSaveModel: TicketSaveDTO? = null
        when (_paymentType.value) {
            "regular" -> ticketSaveModel = TicketSaveDTO(
                identifier,
                tid, paymentType, item, price, approveAt,
                _regularTranslationAmount.value!!, _regularFoodAmount.value!!
            )

            "reward" -> ticketSaveModel = TicketSaveDTO(
                identifier,
                tid, paymentType, item, price, approveAt,
                _rewardTranslationAmount.value!!, _rewardFoodAmount.value!!
            )
        }
        viewModelScope.launch {
            val response = mainActivityModel.savePaymentHistory(ticketSaveModel!!)
            if (response.result == "success") {
                Log.d(TAG, "savePaymentHistory: ${response.result}")

                if (_paymentType.value == "regular") {
                    _userInformation.value!!.foodTicket =
                        _userInformation.value!!.foodTicket + _regularFoodAmount.value!!
                    _userInformation.value!!.translationTicket =
                        _userInformation.value!!.translationTicket + _regularTranslationAmount.value!!

                    _changeTicket.value = "success"

                } else if (_paymentType.value == "reward") {
                    Log.d(TAG, "savePaymentHistory reward ")
                    val quantity = countTicket(_rewardTranslationAmount, _rewardFoodAmount)
                    updateTicketQuantity("have_rewarded", "-", quantity)
                }
            } else {
                Log.d(TAG, "savePaymentHistory: failed")

                if (_paymentType.value == "regular") {
                    //결제 취소
                    requestCancelPayment(tid, price.toString())
                    _changeTicket.value = "failed"
                } else {
                    _changeTicket.value = "failed"
                    Log.d(TAG, "savePaymentHistory reward: ")
                }
            }
        }
    }


    fun logout(sharedPreferences: SharedPreferences) {
        mainActivityModel.logout(sharedPreferences)
    }

    fun registerVariableReset() {
        _registerResult.value = false
    }


    fun scheduleMidnightWork(application: Application) {
        val callback: ((Boolean) -> Unit) = {
            if (it) {
                Log.d(TAG, "scheduleMidnightWork: true")
                _userInformation.value!!.freeTranslationTicket = 3
                _userInformation.value!!.dailyReward = 3
                _userInformation.value!!.haveRewarded = 0
                _todayRewarded.value = 0
            } else {
                Log.d(TAG, "scheduleMidnightWork: false")
            }

        }
        mainActivityModel.scheduleMidnightWork(application, callback)
    }

    fun selectionCountry(view: View) {
        Log.d(TAG, "selectionCountry: 실행")
        when (view.id) {
            R.id.country_selection_america -> _countrySelection.value = "america"
            R.id.country_selection_china -> _countrySelection.value = "china"
            R.id.country_selection_hongkong -> _countrySelection.value = "hongkong"
            R.id.country_selection_japan -> _countrySelection.value = "japan"
            R.id.country_selection_taiwan -> _countrySelection.value = "taiwan"
            R.id.country_selection_vietnam -> _countrySelection.value = "vietnam"
        }

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
        _regularTranslationAmount.value = 1
        _regularFoodAmount.value = 1

        _rewardFoodAmount.value = 1
        _rewardTranslationAmount.value = 1


        _regularTranslationPrice.value = "2,000원"
        _regularFoodPrice.value = "2,000원"
        _regularTotalPrice.value = "총 결제 금액 : 4,000원"
        _totalPriceForPay.value = "4000"


//        _foodPreferenceList.value = FoodPreferenceDataClass("init", arrayListOf())
    }


}