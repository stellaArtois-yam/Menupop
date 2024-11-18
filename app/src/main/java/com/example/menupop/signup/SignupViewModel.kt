package com.example.menupop.signup

import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

import java.util.regex.Pattern


class SignupViewModel : ViewModel() {
    companion object{
        const val TAG = "SignupViewModel"
    }

    private val signupModel  = SignupModel()

    private val _idWarning = MutableLiveData<String>()
    val idWarning : LiveData<String>
        get() = _idWarning

    private val _isValidId = MutableLiveData<Boolean?>(null)
    val isValidId : LiveData<Boolean?>
        get() = _isValidId


    private val _isIdDuplication = MutableLiveData<Boolean?>(null)
    val isIdDuplication : LiveData<Boolean?>
        get() = _isIdDuplication

    private val _isValidPassword = MutableLiveData<Boolean?>(null) // 비밀 번호 유효성
    val isValidPassword : LiveData<Boolean?>
        get() = _isValidPassword

    private val _isValidPasswordConfirm = MutableLiveData<Boolean?>(null) // 비밀 번호 확인 일치
    val isValidPasswordConfirm : LiveData<Boolean?>
        get() = _isValidPasswordConfirm

    private val _checkEmailForm = MutableLiveData<Boolean?>(null) // 이메일 형식 체크
    val checkEmailForm : LiveData<Boolean?>
        get() = _checkEmailForm

    private val _emailWarning = MutableLiveData<String>() // 이메일 경고 메세지(중복, 형식 불일치)
    val emailWarning : LiveData<String>
        get() = _emailWarning

    private var provideChecking : Boolean = false
    private var marketingChecking : Boolean = false

    private fun checkValidateId(inputId: String) : Boolean{
        val regex = Regex("^[a-zA-Z0-9]{6,12}\$")
        return regex.matches(inputId) && inputId.length > 5 && inputId.length < 13
    }

    fun onIdTextChanged(id : String){
        if(checkValidateId(id)){
            _isValidId.value = true
        }else{
            _idWarning.value = "아이디는 6-12자 이내, 영문, 숫자 사용 가능"
            _isValidId.value = false
        }
    }

    suspend fun checkUserIdDuplication(id : String) {
        viewModelScope.launch {
            val response = signupModel.requestIdDuplication(id)
            if(response.result == "exist"){
                _isIdDuplication.value = true
                _idWarning.value = "이미 사용 중인 아이디 입니다."
            }else{
                _isIdDuplication.value = false
                _idWarning.value = "사용 가능한 아이디 입니다."
            }
        }
    }

    fun setIsIdDuplication(boolean: Boolean?){
        _isIdDuplication.value = boolean
    }

    fun onPasswordTextChanged(password: String) {
        _isValidPassword.value = validatePassword(password)
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        _isValidPasswordConfirm.value = checkPasswordsMatch(password, confirmPassword)
    }

    private fun validatePassword(password: String): Boolean {
        val digitPattern = ".*\\d.*"
        val letterPattern = ".*[a-zA-Z].*"
        val specialCharPattern = ".*[@#\$%^&+=].*"

        val hasMinimumLength = password.length >= 8
        val hasDigit = Pattern.compile(digitPattern).matcher(password).matches()
        val hasLetter = Pattern.compile(letterPattern).matcher(password).matches()
        val hasSpecialChar = Pattern.compile(specialCharPattern).matcher(password).matches()

        val count = listOf(hasDigit, hasLetter, hasSpecialChar).count { it }

        return hasMinimumLength && count >= 2
    }

    private fun checkPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun checkEmailForm(emailId : String, domain : String){

        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        val email = "${emailId}@${domain}"

        _checkEmailForm.value = pattern.matcher(email).matches()

        if(_checkEmailForm.value == false){
            _emailWarning.value = "올바른 이메일 형식이 아닙니다."
        }
    }

    fun setCheckEmailForm(boolean: Boolean){
        _checkEmailForm.value = boolean
    }

    private val _isEmailExistence = MutableLiveData<Boolean>()
    val isEmailExistence : LiveData<Boolean>
        get() = _isEmailExistence


    suspend fun checkEmailExistence(email: String) {
        viewModelScope.launch {
            val response =   signupModel.checkEmailExistence(email)
            if(response.result == "exist"){
                _isEmailExistence.value = true
                _emailWarning.value = "해당 이메일로 가입된 계정이 존재합니다."
            }else{
                _isEmailExistence.value = false
            }
        }
    }

    private var verifyCode : String = ""
    private val _verifyCompleted = MutableLiveData<Boolean>()
    val verifyCompleted : LiveData<Boolean>
        get() = _verifyCompleted


    fun requestEmailAuth(email : String) {
        viewModelScope.launch{
            val response = signupModel.sendVerifyCode(email)
            verifyCode = response
        }
    }

    fun checkVerifyCode(verifyCode : String) {
        val result = this.verifyCode == verifyCode
        _verifyCompleted.value = result

        if(_verifyCompleted.value == true){
            _certificationTimer.value = "인증 완료"
        }
    }


    private val _certificationTimer = MutableLiveData<String>()
    val certificationTimer : LiveData<String>
        get() = _certificationTimer

    private val _isTimeExpired = MutableLiveData(false)
    val isTimeExpired : LiveData<Boolean>
        get() = _isTimeExpired

    private var timer: CountDownTimer? = null // 타이머 객체

    // 타이머 시작
    fun startTimer() {
        // 3분(180초)으로 초기화
        val initialTime = TimeUnit.MINUTES.toMillis(3)
        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 남은 시간을 LiveData에 업데이트
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _certificationTimer.value = "시간 제한 : " + String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // 타이머가 종료되면 인증번호 만료
                _isTimeExpired.value = true
                _certificationTimer.value = "인증번호가 만료되었습니다."
            }
        }
        timer?.start()
    }

    // 타이머 중지
    fun stopTimer() {
        timer?.cancel()
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }


    fun checkUserInformation() : Boolean{
        return _isIdDuplication.value == false && _checkEmailForm.value == true &&
                _isValidPassword.value == true && _isValidPasswordConfirm.value == true &&
                _verifyCompleted.value == true
    }

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult : LiveData<Boolean>
        get() = _saveResult

    suspend fun sendUserInformation(id : String, password : String, email :String, identifier : Int){
        viewModelScope.launch{
            val response =  signupModel.sendUserInformation(id, password, email, identifier)
            _saveResult.value = response.result == "success"
        }
    }

    fun personalCheckBoxChecked(isCheck : Boolean){
        provideChecking = isCheck
    }
    fun marketingCheckBoxChecked(isCheck : Boolean){
        marketingChecking = isCheck
    }

    fun checkBoxChecked():Boolean{
        Log.d(TAG, "checkBoxChecked: ${provideChecking && marketingChecking}")
        return provideChecking && marketingChecking
    }

}