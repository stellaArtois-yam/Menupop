package com.example.menupop.resetPassword

import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.menupop.signup.ResultModel
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ResetPasswordViewModel : ViewModel() {
    private val TAG = "ResetPasswordViewModel"
    private val id = MutableLiveData<String>()
    private val resetPasswordModel = ResetPasswordModel() //뷰모델
    private var callback: ((String) -> Unit) ?= null // 콜백
    private var callbackList : ((ResultModel) -> Unit) ?= null

    var verifiedEmail = MutableLiveData<Boolean>()

    private var _checkIdResult = MutableLiveData<Boolean>() // 아이디 존재 여부
    val checkIdResult : LiveData<Boolean> // 아이디 존재 여부
        get() = _checkIdResult

    private var _checkEmailForm = MutableLiveData<Boolean>() //이메일 형식

    val checkEamilForm : LiveData<Boolean>
        get() = _checkEmailForm

    private var verifyCode : String = ""

    val remainingTime = MutableLiveData<String>() //타이머 시간

    private var timer: CountDownTimer? = null // 타이머 객체

    val verifycationCompleted = MutableLiveData<Boolean>() //인증 완료
    private val _passwordError = MutableLiveData<String>()
    val passwordError : LiveData<String>
        get() = _passwordError

    private val _confirmPasswordError = MutableLiveData<String>()
    val confirmPasswordError : LiveData<String>
        get() = _confirmPasswordError

    var lastCheck = false

    val conformResetPassword = MutableLiveData<Boolean>()

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

    fun checkId(id : String){
        callbackList = { response ->
            Log.d(TAG, "checkId: ${response.result}")
            if (response.result == "exist"){
                _checkIdResult.value = true
                 this.id.value = id
            } else{
                _checkIdResult.value = false
            }
        }
        resetPasswordModel.checkId(id,callbackList!!)
    }

    fun sendVerifyCode(email :String){
        callback = { result ->
            Log.d(TAG, "checkId: ${result}")
            verifyCode = result
        }
        resetPasswordModel.sendVerifyCode(email,callback!!)
    }

    fun checkVerifyCode(verifyCode : String) {
        Log.d(TAG, "checkVerifyCode: ${verifyCode} 인증코드 ${this.verifyCode}")
        val result = this.verifyCode == verifyCode
        verifycationCompleted.value = result

    }
    fun checkEmail(email:String){
        callback = {response ->
            verifiedEmail.value = response.equals("확인")
            Log.d(TAG, "checkEmail: ${response}response ${verifiedEmail.value}")
        }
        resetPasswordModel.checkEamil(id.value.toString(),email,callback!!)
    }

    fun checkEmailForm(email:String,emailType: String){
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        val email = "${email}@${emailType}"
        Log.d(TAG, "emailSelection: 이메일 확인 ${email}")
        _checkEmailForm.value =pattern.matcher(email).matches()
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

    fun onPasswordTextChanged(password: String) {
        if (validatePassword(password)) {
            _passwordError.value = null
            Log.d(TAG, "onPasswordTextChanged: null")
        } else {
            _passwordError.value = "비밀번호는 최소 8자에 영문, 숫자, 특수문자 중 2가지 이상을 사용해야 합니다."
            lastCheck = false
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        if (checkPasswordsMatch(password, confirmPassword)) {
            _confirmPasswordError.value = null
            lastCheck = true
        } else {
            _confirmPasswordError.value = "비밀번호가 일치하지 않습니다."
            lastCheck = false
        }
    }

    fun resetPassword(password : String){
        callback = {result ->
            Log.d(TAG, "resetPassword: ${result}")

            conformResetPassword.value = result.equals("완료")
        }
        resetPasswordModel.resetPassword(id.value.toString(),password,callback!!)
    }

}