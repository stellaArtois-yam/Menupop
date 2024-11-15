package com.example.menupop.resetPassword

import android.os.CountDownTimer
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ResetPasswordViewModel : ViewModel() {
    companion object{
        const val TAG = "ResetPasswordViewModel"
    }

    private var _id = MutableLiveData<String>()
    private val resetPasswordModel = ResetPasswordModel()

    var verifiedEmail = MutableLiveData<Boolean>()

    private var _checkIdResult = MutableLiveData<Boolean?>() // 아이디 존재 여부
    val checkIdResult : LiveData<Boolean?> // 아이디 존재 여부
        get() = _checkIdResult

    private var _checkEmailForm = MutableLiveData<Boolean>() //이메일 형식

    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    private var verifyCode : String = ""

    private val _remainingTime = MutableLiveData<String>() //타이머 시간
    val remainingTime : LiveData<String>
        get() = _remainingTime

    private var timer: CountDownTimer? = null // 타이머 객체

    val verifycationCompleted = MutableLiveData<Boolean>() //인증 완료
    private val _passwordError = MutableLiveData<String?>()
    val passwordError : LiveData<String?>
        get() = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError : LiveData<String?>
        get() = _confirmPasswordError

    private var lastCheck = false

    private val _conformResetPassword = MutableLiveData<Boolean>()
    val conformResetPassword : LiveData<Boolean>
        get() =_conformResetPassword

    fun startTimer() {

        val initialTime = TimeUnit.MINUTES.toMillis(1)

        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _remainingTime.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _remainingTime.value = "00:00"
            }
        }

        timer?.start()
    }

    fun stopTimer() {
        timer?.cancel()
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }

    fun setCheckIdResult(){
        _checkIdResult.value = null
    }

    suspend fun checkId(id : String){
        viewModelScope.launch {
            val response = resetPasswordModel.checkId(id)

            when(response.result){
                "exist" -> {
                    _checkIdResult.value = true
                    _id.value = id
                }
                else ->  _checkIdResult.value = false
            }
        }

    }

    suspend fun sendVerifyCode(email :String){
        viewModelScope.launch{
            val response =  resetPasswordModel.sendVerifyCode(email)
            verifyCode = response
        }
    }

    fun checkVerifyCode(verifyCode : String) {
        val result = this.verifyCode == verifyCode
        verifycationCompleted.value = result
    }

    fun checkEmail(email:String){
        viewModelScope.launch {
            val response = resetPasswordModel.checkEmail(_id.value.toString(), email)
            verifiedEmail.value = response == "확인"
        }
    }

    fun checkEmailForm(email:String,emailType: String){
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        val email = "${email}@${emailType}"

        _checkEmailForm.value =pattern.matcher(email).matches()
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

    fun onPasswordTextChanged(password: String) {

        when(validatePassword(password)){
            true ->  _passwordError.value = null
            false -> {
                _passwordError.value = "비밀번호는 최소 8자에 영문, 숫자, 특수문자 중 2가지 이상을 사용해야 합니다."
                lastCheck = false
            }
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {

        when(checkPasswordsMatch(password, confirmPassword)){
            true -> {
                _confirmPasswordError.value = null
                lastCheck = true
            }
            false -> {
                _confirmPasswordError.value = "비밀번호가 일치하지 않습니다."
                lastCheck = false
            }
        }

    }

    fun resetPassword(password : String){
        viewModelScope.launch {
            when(resetPasswordModel.resetPassword(_id.value.toString(),password)){
                "success" -> _conformResetPassword.value = true
                else -> _conformResetPassword.value = false
            }
        }
    }

}