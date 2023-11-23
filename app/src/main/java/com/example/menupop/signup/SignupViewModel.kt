package com.example.menupop.signup


import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.TimeUnit

import java.util.regex.Pattern


class SignupViewModel : ViewModel() {

    val TAG = "SignupViewModel"
    var signupModel  = SignupModel()
    private var callback: ((String) -> Unit) ?= null // 콜백

//    private val _isSignupEnabled = MutableLiveData<Boolean>().apply { value = false }


    /**
     * 아이디 경고 문구
     */

    private val _idWarning = MutableLiveData<String>()
    val idWarning : LiveData<String>
        get() = _idWarning

    private val _isValidId = MutableLiveData<Boolean>()
    val isValidId : LiveData<Boolean>
        get() = _isValidId

    fun validateId(inputId: String) : Boolean {

        val regex = Regex("^[a-zA-Z0-9]{6,12}\$")

        var matcher = regex.matches(inputId)

        return matcher
    }

    fun onIdTextChanged(id: String) {
        if (validateId(id)) {
            _idWarning.value = null
            _isValidId.value = true
        } else {
            _idWarning.value = "아이디는 6-12자 이내, 영문, 숫자 사용 가능"
            _isValidId.value = false
        }
    }





    /**
     * 아이디 중복 여부
     */
    val isIdDuplication = MutableLiveData<Boolean>()


    fun checkUserIdDuplication(id : String) {
        callback = {isDuplicate ->
            Log.d(TAG, "checkUserIdDuplication: ${isDuplicate}")
            if(isDuplicate == "exist"){
                isIdDuplication.value = true
                _idWarning.value = "이미 사용 중인 아이디 입니다."
            }else{
                isIdDuplication.value = false
                _idWarning.value = "사용 가능한 아이디 입니다."
            }

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

    private val _isValidPassword = MutableLiveData<Boolean>()
    val isValidPassword : LiveData<Boolean>
        get() = _isValidPassword

    private val _isValidPasswordConfirm = MutableLiveData<Boolean>()
    val isValidPasswordConfirm : LiveData<Boolean>
        get() = _isValidPasswordConfirm

    fun onPasswordTextChanged(password: String) {
        if (validatePassword(password)) {
            _passwordWarning.value = null
            _isValidPassword.value = true
        } else {
            _passwordWarning.value = "비밀번호는 최소 8자에 영문, 숫자, 특수문자 중 2가지 이상을 사용해야 합니다."
            _isValidPassword.value = false
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        if (checkPasswordsMatch(password, confirmPassword)) {
            _confirmPasswordWarning.value = null
            _isValidPasswordConfirm.value = true

        } else {
            _confirmPasswordWarning.value = "비밀번호가 일치하지 않습니다."
            _isValidPasswordConfirm.value = false
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

    private val _isEmailButton = MutableLiveData<Boolean>()
    val isEmailButton : LiveData<Boolean>
        get() = _isEmailButton

    val _checkEmailForm = MutableLiveData<Boolean>()
    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    val _emailWarning = MutableLiveData<String>()
    val emailWarning : LiveData<String>
        get() = _emailWarning

    fun checkEmailForm(email : String, domain : String){

        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        val email = "${email}@${domain}"

        _checkEmailForm.value = pattern.matcher(email).matches()

        if(_checkEmailForm.value == false){

            _isEmailButton.value = false
            _emailWarning.value = "올바른 이메일 형식이 아닙니다."
            Log.d(TAG, "emailButton: ${_isEmailButton.value}")

        }else{
            _isEmailButton.value = true
            Log.d(TAG, "emailButton: ${_isEmailButton.value}")
        }
    }

    private val _isEmailExistence = MutableLiveData<Boolean>()
    val isEmailExistence : LiveData<Boolean>
        get() = _isEmailExistence



    fun checkEmailExistence(email: String) {
        Log.d(TAG, "checkEmailExistence")
        callback = {result ->
            Log.d(TAG, "checkEmailExistence: $result")
            if(result == "exist"){
                _isEmailExistence.value = true
                _emailWarning.value = "해당 이메일로 가입된 계정이 존재합니다."

            }else{
                _isEmailExistence.value = false
            }
        }
        signupModel.checkEmailExistence(email, callback!!)
    }

    private var verifyCode : String = ""
    val verifyCompleted = MutableLiveData<Boolean>() //인증 완료

    fun requestEmailAuth(email : String) {
        callback = { result ->
            Log.d(TAG, "code: ${result}")
            verifyCode = result
        }
        signupModel.sendVerifyCode(email, callback!!)
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
        val initialTime = TimeUnit.MINUTES.toMillis(1)
        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 남은 시간을 LiveData에 업데이트
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                remainingTime.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // 타이머가 종료되면 인증번호 만료
                remainingTime.value = "인증번호가 만료되었습니다."

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

//    init {
//        // 기본적으로 가입 버튼 비활성화
//        _isSignupEnabled.value = false
//
//    }

    fun checkUserInformation() : Boolean{
        Log.d(TAG, "isValidId: ${isValidId.value}")
        Log.d(TAG, "checkEmailForm: ${checkEmailForm.value}")
        Log.d(TAG, "password: ${_isValidPassword.value}")
        Log.d(TAG, "passwordConfirm: ${_isValidPasswordConfirm.value}")
        Log.d(TAG, "verifyComplete: ${verifyCompleted.value}")

        if(isValidId.value == true && _checkEmailForm.value == true &&
            _passwordWarning.value == null && _confirmPasswordWarning.value == null &&
            verifyCompleted.value == true){
            
//            _isSignupEnabled.value = true
            return true
        }else{
            return false
        }

    }
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult : LiveData<Boolean>
        get() = _saveResult


    fun sendUserInformation(id : String, password : String, email :String, identifier : Int){
        callback = {result ->
            Log.d(TAG, "sendUserInformation: $result")

            if(result == "success"){
                _saveResult.value = true
            }else{
                _saveResult.value = false
            }

        }
        signupModel.sendUserInformation(id, password, email, identifier, callback!!)
    }





}