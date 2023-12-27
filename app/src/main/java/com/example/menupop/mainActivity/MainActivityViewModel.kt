package com.example.menupop.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.menupop.R
import com.example.menupop.mainActivity.profile.KakaoPayApproveResponseDTO
import com.example.menupop.mainActivity.profile.KakaoPayReadyResponseDTO
import com.example.menupop.mainActivity.profile.TicketSaveDTO
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceSearchDTO
import com.example.menupop.SimpleResultDTO
import com.example.menupop.mainActivity.profile.ProfileSelectionDTO
import com.google.android.gms.ads.rewarded.RewardedAd
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.coroutines.coroutineContext

class MainActivityViewModel(private val application: Application) :  AndroidViewModel(application){
    val TAG = "MainActivityViewModel"
    val mainActivityModel = MainActivityModel(application)

    private var callback : ((SimpleResultDTO) -> Unit) ?= null
    private var callbackUserInfo :((UserInformationDTO) ->Unit) ?= null
    private var callbackKakaoReady : ((KakaoPayReadyResponseDTO) -> Unit) ? = null
    private var callbackApprove : ((KakaoPayApproveResponseDTO) -> Unit) ? = null
    private var callbackSearchData : ((FoodPreferenceSearchDTO) -> Unit) ?= null
    private var callbackResult : ((String) -> Unit) ?= null
    private var callbackFoodPreference : ((FoodPreferenceDataClass) -> Unit) ?= null
    private var callbackAd: ((RewardedAd?) -> Unit)? = null


    private val _rewardedAd = MutableLiveData<RewardedAd>()

    val rewardedAd : LiveData<RewardedAd>
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

    val accountWithdrawal : LiveData<String>
        get() = _accountWithdrawal


    // 가지고 있는 리워드
    private val _haveRewarded = MutableLiveData<Int>()

    val haveRewarded : LiveData<Int>
        get() = _haveRewarded

    // 받은 리워드
    private val _todayRewarded = MutableLiveData<Int>()

    val todayRewarded : LiveData<Int>
        get() = _todayRewarded

    fun getProfileList(resources : Resources) : ArrayList<ProfileSelectionDTO>{
        val imageNames = resources.getStringArray(R.array.profile)
        var imageList : ArrayList<ProfileSelectionDTO> = ArrayList()

        for(name in imageNames){
            val imageName = resources.getIdentifier(name, "drawable", application.packageName)
            val image = resources.getDrawable(imageName)
            imageList.add(ProfileSelectionDTO(image))
        }

        return imageList
    }



    /**
     * 메인
     */
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading : LiveData<Boolean>
        get() = _isLoading

    private val _deletedResult = MutableLiveData<Boolean>()
    val deletedResult : LiveData<Boolean>
        get() = _deletedResult

    private val _userInformation = MutableLiveData<UserInformationDTO>()
    val userInformation : LiveData<UserInformationDTO>
        get() = _userInformation

    private val _checkingTranslationTicket = MutableLiveData<Boolean>()

    private val _profileImage = MutableLiveData<Drawable>()
    val profileImage : LiveData<Drawable>
        get() = _profileImage

    val checkingTranslationTicket : LiveData<Boolean>
        get() =_checkingTranslationTicket

    fun freeFoodTicketMinus(){
        Log.d(TAG, "ticketMinus: ${_userInformation.value!!.foodTicket}")

        callbackResult = {result ->
            Log.d(TAG, "ticketMinus: $result")
            if(result.trim() == "success"){
                _userInformation.value?.freeFoodTicket = _userInformation.value?.freeFoodTicket?.minus(1)!!
            }
        }
        mainActivityModel.minusFreeFoodTicket(_identifier.value!!,callbackResult!!)
        Log.d(TAG, "ticketMinus 반역: ${_userInformation.value!!.foodTicket}")
    }

    fun foodTicketMinus(){
        Log.d(TAG, "ticketMinus: ${_userInformation.value!!.foodTicket}")


        callbackResult = {result ->
            Log.d(TAG, "ticketMinus: $result")
            if(result.trim() == "success"){
                _userInformation.value?.foodTicket = _userInformation.value?.foodTicket?.minus(1)!!
            }
        }
        mainActivityModel.minusFoodTicket(_identifier.value!!,callbackResult!!)
        Log.d(TAG, "ticketMinus 반역: ${_userInformation.value!!.foodTicket}")
    }
    fun translationTicketMinus(){
        Log.d(TAG, "ticketMinus: ${_userInformation.value!!.foodTicket}")

        callbackResult = {result ->
            Log.d(TAG, "ticketMinus: $result")
            if(result.trim() == "success"){
                _userInformation.value?.translationTicket= _userInformation.value?.translationTicket?.minus(1)!!
            }
        }
        mainActivityModel.minusTranslationTicket(_identifier.value!!,callbackResult!!)
        Log.d(TAG, "ticketMinus 반역: ${_userInformation.value!!.foodTicket}")
    }

    fun foodPreferenceRegister(foodName:String,classification:String){
        Log.d(TAG, "foodPreferenceRegister: 호출됨")

        callbackResult = { result ->
            Log.d(TAG, "foodPreferenceRegister: ${result}")
            _registerResult.value = result == "success"
        }
        mainActivityModel.foodPreferenceRegister(_identifier.value!!,foodName,classification,callbackResult!!)
    }

    fun checkingTranslationTicket(){
        _checkingTranslationTicket.value = userInformation.value?.translationTicket!! > 0

    }

    fun deleteFoodPreference(foodName: String){
        callbackResult = {result ->
            Log.d(TAG, "deleteFoodPreference:$result d ${result == "success"}")
            if(result.trim() == "success"){
                Log.d(TAG, "deleteFoodPreference: 성공")
                _deletedResult.value = true
            }else{
                _deletedResult.value= false
            }
        }

        mainActivityModel.deleteFoodPreference(_identifier.value!!,foodName,callbackResult!!)

    }

    fun searchFood(query : String){
        callbackSearchData = {result ->
            Log.d(TAG, "searchFood: test")
            if(result.result == "success"){
                val foodPreferenceLists = ArrayList<String>()
                _foodPreferenceList.value?.foodList?.forEach { it ->
                    foodPreferenceLists.add(it.foodName)
                }
                result.foodList.removeAll(foodPreferenceLists)
            } else if(result.result == "notFound"){
                Log.d(TAG, "searchFood: 찾을 수 없음")
            }
            _searchFood.value = result.foodList
            Log.d(TAG, "searchFood: $result")
        }
        mainActivityModel.searchFood(query,callbackSearchData!!)

    }

    private val _identifier = MutableLiveData<Int>()
    val identifier : LiveData<Int>
        get() = _identifier




    fun getUserInfo(sharedPreferences: SharedPreferences){
        val userInfo = mainActivityModel.getUserInfo(sharedPreferences)
        _identifier.value = userInfo["identifier"]

        _dailyTranslation.value = userInfo["dailyTranslation"]
        _dailyReward.value = userInfo["dailyReward"]
        _haveRewarded.value = userInfo["rewarded"]
        _todayRewarded.value = userInfo["todayRewarded"]

        Log.d(TAG, "first get \n identifier: ${identifier.value}\n dailyReward: ${_dailyReward.value}\n rewarded: ${_haveRewarded.value}")

    }


    fun requestUserInformation(identifier : Int){

        callbackUserInfo = {response ->
            _userInformation.value = response
            Log.d(TAG, "requestUserInformation: ${response.id}, ${response.email} ${_userInformation.value!!.freeFoodTicket} ${response}")
            _isLoading.value = response.id != null && response.email!=null
        }
        mainActivityModel.requestUserInformation(identifier, callbackUserInfo!!)
    }

    fun loadAd(key: String) {
        callbackAd = { ad ->
            Log.d(TAG, "loadAd: ${ad}")
            _rewardedAd.value = ad
        }
        mainActivityModel.requestAd(key, callbackAd!!)
    }
    fun setRewarded(sharedPreferences: SharedPreferences){
       mainActivityModel.setRewarded(sharedPreferences, _haveRewarded.value!!)
    }

    private val _isChangedProfile = MutableLiveData<Boolean>()
    val isChangedProfile : LiveData<Boolean>
        get() = _isChangedProfile

    fun saveSelectedProfile(imageName: String, sharedPreferences: SharedPreferences, resources: Resources){

        callbackResult = {
            if(it == "success"){
                Log.d(TAG, "saveSelectedProfile: $it")
                val image = resources.getIdentifier(imageName, "drawable", application.packageName)
                _profileImage.value = resources.getDrawable(image)
                _isChangedProfile.value = true
            }
        }
        mainActivityModel.saveSelectedProfile(imageName, sharedPreferences, callbackResult!!)

    }


    fun getProfileImage(sharedPreferences: SharedPreferences, resources: Resources){
        val getResult = mainActivityModel.getProfileImage(sharedPreferences)
        Log.d(TAG, "getProfileImage: $getResult")

        if(getResult != null){
            val image = resources.getIdentifier(getResult, "drawable", application.packageName)
            _profileImage.value = resources.getDrawable(image)
        }else{
            _profileImage.value = null
        }

    }
    fun rewardedSuccess(sharedPreferences: SharedPreferences){
        val rewarded = mainActivityModel.rewardedPlus(sharedPreferences)
        Log.d(TAG, "rewardedSuccess: $rewarded")
        _haveRewarded.value = rewarded.first
        _todayRewarded.value = rewarded.first

        _dailyReward.value = rewarded.second
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun withdrawal(sharedPreferences: SharedPreferences){
        callbackResult = {
            Log.d(TAG, "withdrawal: $it")
            _accountWithdrawal.value = it
            if(it == "success"){
                logout(sharedPreferences)
            }
        }
        val email = userInformation.value?.email
        val id = userInformation.value?.id
        val localDate: LocalDate = LocalDate.now()
        mainActivityModel.withdrawal(_identifier.value!!,email!!,id!!,localDate.toString(),callbackResult!!)
    }

    fun getFoodPreference(){
        callbackFoodPreference = { foodPreferenceDataClass ->
            Log.d(TAG, "getFoodPreference: ${foodPreferenceDataClass}")
            _foodPreferenceList.value = foodPreferenceDataClass
        }
        mainActivityModel.getFoodPreference(_identifier.value!!,callbackFoodPreference!!)
    }


    



    /**
     * 티켓 구매 + 다이얼로그
     */

    private val _paymentType = MutableLiveData<String>()

    private val _regularTranslationAmount = MutableLiveData<Int>()
    val regularTranslationAmount : LiveData<Int>
        get() = _regularTranslationAmount

    private val _rewardTranslationAmount = MutableLiveData<Int>()
    val rewardTranslationAmount : LiveData<Int>
        get() = _rewardTranslationAmount

    private val _regularTranslationPrice = MutableLiveData<String>()
    val regularTranslationPrice : LiveData<String>
        get() = _regularTranslationPrice

    private val _regularFoodAmount = MutableLiveData<Int>()
    val regularFoodAmount : LiveData<Int>
        get() = _regularFoodAmount

    private val _rewardFoodAmount = MutableLiveData<Int>()
    val rewardFoodAmount : LiveData<Int>
        get() = _rewardFoodAmount

    private val _regularFoodPrice = MutableLiveData<String>()
    val regularFoodPrice : LiveData<String>
        get() = _regularFoodPrice

    private val _regularTotalPrice = MutableLiveData<String>()

    val regularTotalPrice : LiveData<String>
        get() = _regularTotalPrice

    private val _totalPriceForPay = MutableLiveData<String>()

    fun updatePaymentType (type : String){
        Log.d(TAG, "updatePaymentType: $type")
        _paymentType.value = type
    }

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


    private fun incrementRewardAmount(amount: MutableLiveData<Int>) {
        amount.value = amount.value!! + 1
    }

    private fun decrementRewardAmount(amount: MutableLiveData<Int>) {
        if (amount.value!! > 0) {
            amount.value = amount.value!! - 1
        }
    }

    fun addTranslationTicketReward() {
        incrementRewardAmount(_rewardTranslationAmount)
    }

    fun addFoodTicketReward() {
        incrementRewardAmount(_rewardFoodAmount)
    }

    fun removeTranslationTicketReward() {
        decrementRewardAmount(_rewardTranslationAmount)
    }

    fun removeFoodTicketReward() {
        decrementRewardAmount(_rewardFoodAmount)
    }

    private val _isRewardExceeded = MutableLiveData<Boolean>()
    val isRewardExceeded : LiveData<Boolean>
        get() = _isRewardExceeded
    @RequiresApi(Build.VERSION_CODES.O)
    fun rewardPayment(identifier: Int){

        if(_haveRewarded.value!! < (_rewardFoodAmount.value!! + _rewardTranslationAmount.value!!)){
            _isRewardExceeded.value = true

        }else{
            _isRewardExceeded.value = false
            var item = itemName(_rewardTranslationAmount, _rewardFoodAmount)
            var time = LocalDateTime.now().toString()
            savePaymentHistory(identifier
                ,hashCode().toString()
                ,_paymentType.value!!
                ,item
                , 0
                ,time)
        }
    }





    private fun changeTicketAmount(ticketAmount: MutableLiveData<Int>,
                                   ticketPrice: MutableLiveData<String>,
                                   otherTicketAmount: MutableLiveData<Int>,
                                   change: Int = 1) {
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



    fun countTicket(ticketAmount: MutableLiveData<Int>,
                    otherTicketAmount: MutableLiveData<Int>): Int{
        if(ticketAmount.value!! > 0 && otherTicketAmount.value!! >0){

            return ticketAmount.value!! + otherTicketAmount.value!!
        }else{
            return 1
        }
    }

    fun itemName(ticketAmount: MutableLiveData<Int>,
                 otherTicketAmount: MutableLiveData<Int>) : String{
        if(ticketAmount.value!! > 0 && otherTicketAmount.value!! >0){
            val total = ticketAmount.value!! + otherTicketAmount.value!! - 1
            return "번역 티켓 외 $total"

        }else if(ticketAmount.value!! > 0 && otherTicketAmount.value!! == 0){
            return "번역 티켓"
        }else{
            return "음식 티켓"
        }
    }


    private val _paymentResponse = MutableLiveData<KakaoPayReadyResponseDTO>()
    val paymentResponse : LiveData<KakaoPayReadyResponseDTO>
        get() = _paymentResponse

    private val _pgToken = MutableLiveData<String>()

    private val _userId = MutableLiveData<String>()


    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePgToken(token : String){

        Log.d(TAG, "updatePgToken: $token")
        _pgToken.value = token

        if(_pgToken.value !=null){
            Log.d(TAG, "updatePgToken: not null")
            completePayment(_paymentResponse.value!!.tid, _userId.value!!, _pgToken.value!!)
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun createPaymentRequest(userId : String){
        Log.d(TAG, "createPaymentRequest: !!!")
        callbackKakaoReady = {response ->

            //여기서 웹뷰로 보내 줘야 함
            if(response!=null){
                _paymentResponse.value = response
            }

        }
        val item = itemName(_regularTranslationAmount, _regularFoodAmount)
        val quantity = countTicket(_regularTranslationAmount, _regularFoodAmount)

        _userId.value = userId

        mainActivityModel.createPaymentRequest(userId, item, quantity.toString(),
            _totalPriceForPay.value!!, callbackKakaoReady!!)

    }

    fun setPaymentResponse(){
        _paymentResponse.value = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun completePayment(tid : String, userId: String, pgToken : String){
        callbackApprove = {response ->
            Log.d(TAG, "completePayment: $response")
            _paymentResponse.value = null

            // db에 티켓 개수도 수정
            // 구매 이력 저장

            if(response.approved_at != null){
                savePaymentHistory(response.partner_user_id.toInt(),
                    response.tid, response.payment_method_type,
                    response.item_name, response.amount.total.toInt(), response.approved_at)
            }

        }
        mainActivityModel.requestApprovePayment(tid, userId, pgToken, callbackApprove!!)
    }

    private val _changeTicket = MutableLiveData<Boolean>()

    val changeTicket : LiveData<Boolean>
        get() =_changeTicket

    fun savePaymentHistory(identifier: Int, tid : String, paymentType : String, item : String,
                           price : Int, approveAt : String){
        var ticketSaveModel : TicketSaveDTO?= null

        callback = {response ->
            //여기서 클라이언트 티켓 개수 수정
            if(response.result == "success"){
                _changeTicket.value = true
                Log.d(TAG, "savePaymentHistory: ${response.result}")

                if(_paymentType.value == "regular"){
                    _userInformation.value!!.foodTicket = _userInformation.value!!.foodTicket + _regularFoodAmount.value!!
                    _userInformation.value!!.translationTicket = _userInformation.value!!.translationTicket + _regularTranslationAmount.value!!


                }else if(_paymentType.value == "reward"){
                    _haveRewarded.value = _haveRewarded.value!! - 1
                    _userInformation.value!!.foodTicket = _userInformation.value!!.foodTicket + _rewardFoodAmount.value!!
                    _userInformation.value!!.translationTicket = _userInformation.value!!.translationTicket + _rewardTranslationAmount.value!!

                }


            }else{
                Log.d(TAG, "savePaymentHistory: failed")
            }

        } //callback

        if(_paymentType.value == "regular"){
            ticketSaveModel = TicketSaveDTO(identifier,
                tid, paymentType, item, price,approveAt,
                _regularTranslationAmount.value!!, _regularFoodAmount.value!!)
            Log.d(TAG, "ticketSaveModel Regular: $ticketSaveModel")


        }else{
            ticketSaveModel = TicketSaveDTO(identifier, tid, paymentType, item, price, approveAt,
                _rewardTranslationAmount.value!!, _rewardFoodAmount.value!!)
            Log.d(TAG, "ticketSaveModel Reward: $ticketSaveModel")
        }


        mainActivityModel.savePaymentHistory(ticketSaveModel!!, callback!!)


    }

    fun logout(sharedPreferences: SharedPreferences){
        mainActivityModel.logout(sharedPreferences)
    }
    fun registerVariableReset(){
        _registerResult.value = false
    }

    private val _dailyTranslation = MutableLiveData<Int>()
    val dailyTranslation : LiveData<Int>
        get() = _dailyTranslation

    //reward 받을 수 있는 개수
    private val _dailyReward = MutableLiveData<Int>()
    val dailyReward : LiveData<Int>
        get() = _dailyReward

    fun scheduleMidnightWork(application: Application){
        val callback : ((Boolean) -> Unit) ={
            if(it){
                Log.d(TAG, "scheduleMidnightWork: true")
                _dailyTranslation.value = 3
                _dailyReward.value = 3
                _haveRewarded.value = 0
                _todayRewarded.value = 0
            }else{
                Log.d(TAG, "scheduleMidnightWork: false")
            }

        }
        mainActivityModel.scheduleMidnightWork(application, callback)
        Log.d(TAG, "scheduleMidnightWork: ?")


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

    }




}