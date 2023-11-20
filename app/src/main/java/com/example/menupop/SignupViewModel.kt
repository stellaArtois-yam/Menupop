package com.example.menupop


import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.TimeUnit

import java.util.regex.Pattern


class SignupViewModel : ViewModel() {

    val TAG = "SignupViewModel"
    var signupModel  = SignupModel()
    private var callback: ((String) -> Unit) ?= null // 콜백

    private val _isSignupEnabled = MutableLiveData<Boolean>().apply { value = false }


    /**
     * 아이디 경고 문구
     */

    private val _idWarning = MutableLiveData<String>()
    val idWarning : LiveData<String>
        get() = _idWarning


    fun validateId(inputId: String) : Boolean {

        val regex = Regex("^[a-zA-Z0-9]{6,12}\$")

        var matcher = regex.matches(inputId)

        return matcher
    }

    fun onIdTextChanged(id: String) {
        if (validateId(id)) {
            _idWarning.value = null
        } else {
            _idWarning.value = "아이디는 6-12자 이내, 영문, 숫자 사용 가능"
        }
    }



    /**
     * 아이디 중복 확인
     */
    val isIdDuplication = MutableLiveData<String>()

    fun checkUserIdDuplication(id : String) {
        callback = {isDuplicate ->
            Log.d(TAG, "checkUserIdDuplication: ${isDuplicate}")
            isIdDuplication.value = isDuplicate
        }
        signupModel.requestIdDuplication(id, callback!!)
    }


    /**
     * 비밀번호 경고 문구
     */
    private val _passwordWarning = MutableLiveData<String>()
    val passwordWarning : LiveData<String>
        get() = _passwordWarning

    private val _confirmPasswordWarning = MutableLiveData<String>()
    val confirmPasswordWarning : LiveData<String>
        get() = _confirmPasswordWarning

    fun onPasswordTextChanged(password: String) {
        if (validatePassword(password)) {
            _passwordWarning.value = null
        } else {
            _passwordWarning.value = "비밀번호는 최소 8자에 영문, 숫자, 특수문자 중 2가지 이상을 사용해야 합니다."
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        if (checkPasswordsMatch(password, confirmPassword)) {
            _confirmPasswordWarning.value = null
        } else {
            _confirmPasswordWarning.value = "비밀번호가 일치하지 않습니다."
        }
    }

    fun validatePassword(password: String): Boolean {
        val digitPattern = ".*\\d.*"
        val letterPattern = ".*[a-zA-Z].*"
        val specialCharPattern = ".*[@#\$%^&+=].*"

        val hasMinimumLength = password.length >= 8
        val hasDigit = Pattern.compile(digitPattern).matcher(password).matches()
        val hasLetter = Pattern.compile(letterPattern).matcher(password).matches()
        val hasSpecialChar = Pattern.compile(specialCharPattern).matcher(password).matches()

        val count = listOf(hasDigit, hasLetter, hasSpecialChar).count { it }

        Log.d(TAG, "validatePassword: $hasMinimumLength && $count")

        return hasMinimumLength && count >= 2
    }

    fun checkPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }


    /**
     * 이메일
     */
    private val _emailId = MutableLiveData<String>()

    private val _domainSelection = MutableLiveData<String>()
    val domainSelection: LiveData<String>
        get() = _domainSelection

    private val _emailWarning = MutableLiveData<String>()
    val emailWarning : LiveData<String>
        get() = _emailWarning

    private val _isEmailButton = MutableLiveData<Boolean>()
    val isEnableButton : LiveData<Boolean>
        get() = _isEmailButton

    fun setEmailId(emailId: String) {
        Log.d(TAG, "setId: $emailId")
        _emailId.value = emailId
        validateEmail()
    }

    fun setEmailSelection(selection: String) {
        Log.d(TAG, "setEmailSelection:$selection ")
        _domainSelection.value = selection
        validateEmail()
    }

    fun validateEmail() {
        val emailId = _emailId.value
        val domainSelection = _domainSelection.value
        if (emailId.isNullOrBlank() || domainSelection == "선택") {

            _emailWarning.value = "이메일 아이디 또는 이메일을 선택하세요."
            _isEmailButton.value = false // 버튼 비활성화
        } else {
            _emailWarning.value = null
            _isEmailButton.value = true // 버튼 활성화
        }
    }

    private var verifyCode : String = ""
    val verifyCompleted = MutableLiveData<Boolean>() //인증 완료


    fun requestEmailAuth(email : String) {
        val fullEmailAdress = "$email@${domainSelection.value}"

        callback = { result ->
            Log.d(TAG, "code: ${result}")
            verifyCode = result
        }
        signupModel.sendVerifyCode(fullEmailAdress, callback!!)
    }

    fun checkVerifyCode(verifyCode : String) {
        Log.d(TAG, "checkVerifyCode: ${verifyCode} 인증코드 ${this.verifyCode}")
        val result = this.verifyCode == verifyCode
        Log.d(TAG, "result : $result")
        verifyCompleted.value = result

    }


    val remainingTime = MutableLiveData<String>() //타이머 시간

    private var timer: CountDownTimer? = null // 타이머 객체

    // 타이머를 시작하는 메서드
    fun startTimer() {
        // 3분(180초)으로 초기화
        val initialTime = TimeUnit.MINUTES.toMillis(3)

        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 남은 시간을 LiveData에 업데이트
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                remainingTime.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // 타이머가 종료되면 남은 시간을 00:00으로 설정
                remainingTime.value = "00:00"
            }
        }

        timer?.start()
    }

    // 타이머를 중지하는 메서드
    fun stopTimer() {
        timer?.cancel()
    }

    override fun onCleared() {
        // ViewModel이 소멸될 때 타이머도 중지
        stopTimer()
        super.onCleared()
    }

    init {
        // 기본적으로 가입 버튼 비활성화
        _isSignupEnabled.value = false

    }

    fun checkUserInformation(){
        if(_idWarning.value == null && _emailWarning.value == null &&
            _passwordWarning.value == null && _confirmPasswordWarning.value == null &&
            verifyCompleted.value == true){
            
            _isSignupEnabled.value = true
            Log.d(TAG, "checkUserInformation: true")
        }

    }
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult : LiveData<Boolean>
        get() = _saveResult


    fun sendUserInformation(id : String, password : String, email :String, identifier : Int){
        callback = {result ->
            Log.d(TAG, "sendUserInformation: $result")

            if(result == "성공"){
                _saveResult.value = true
            }else{
                _saveResult.value = false
            }

        }
        signupModel.sendUserInformation(id, password, email, identifier, callback!!)
    }





}