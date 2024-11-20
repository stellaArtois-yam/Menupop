package com.example.menupop.resetPassword

import android.os.CountDownTimer
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.menupop.Encryption
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ResetPasswordViewModel : ViewModel() {
    companion object{
        const val TAG = "ResetPasswordViewModel"
    }

    private var _id = MutableLiveData<String>()
    private val resetPasswordModel = ResetPasswordModel()

    private var _isEmailMatchId = MutableLiveData<Boolean?>(null)
    val isEmailMatchId : LiveData<Boolean?>
        get() = _isEmailMatchId

    private var _checkIdResult = MutableLiveData<Boolean?>(null)// 아이디 존재 여부
    val checkIdResult : LiveData<Boolean?>
        get() = _checkIdResult

    private var _checkEmailForm = MutableLiveData<Boolean?>(null) //이메일 형식
    val checkEmailForm : LiveData<Boolean?>
        get() = _checkEmailForm

    private var verifyCode : String = ""

    private val _remainingTime = MutableLiveData<String>() //타이머 시간
    val remainingTime : LiveData<String>
        get() = _remainingTime

    private var timer: CountDownTimer? = null // 타이머 객체

    private var _verifyCompleted = MutableLiveData<Boolean>() //인증 완료
    val verifyCompleted : LiveData<Boolean>
        get() = _verifyCompleted

    private val _isPasswordFormMatched= MutableLiveData<Boolean?>(null)
    val isPasswordFormMatched : LiveData<Boolean?>
        get() = _isPasswordFormMatched

    private val _isConfirmPasswordMatched = MutableLiveData<Boolean?>(null)
    val isConfirmPasswordMatched : LiveData<Boolean?>
        get() = _isConfirmPasswordMatched

    var lastCheck = false

    private val _isResetPassword = MutableLiveData<Boolean>()
    val isResetPassword : LiveData<Boolean>
        get() =_isResetPassword

    private val _certificateStatus  = MutableLiveData("인증번호")
    val certificateStatus : LiveData<String>
        get() = _certificateStatus

    private lateinit var encryption : Encryption

    fun setIsEmailMatchIdInit(){
        _isEmailMatchId.value = null
    }

    fun startTimer() {

        val initialTime = TimeUnit.MINUTES.toMillis(3)
        _certificateStatus.value = "확인"

        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _remainingTime.value = String.format("시간 제한 : %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _remainingTime.value = "인증 번호가 만료되었습니다"
                _certificateStatus.value = "재인증"
            }
        }
        timer?.start()
    }

    fun stopTimer() {
        timer?.cancel()
        _remainingTime.value = "인증 완료"
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

    // 인증 번호 메일 보내기
    suspend fun sendVerifyCode(email :String){
        viewModelScope.launch{
            val response =  resetPasswordModel.sendVerifyCode(email)
            verifyCode = response
        }
    }

    fun checkVerifyCode(verifyCode : String) {
        val result = this.verifyCode == verifyCode
        _verifyCompleted.value = result
    }

    // 사용자가 입력한 아이디와 이메일이 일치하는지 확인
    fun checkEmail(email:String){
        viewModelScope.launch {
            val result = resetPasswordModel.checkEmail(_id.value.toString(), email)
            if(result == "success"){
                _isEmailMatchId.value = true
            }else{
                _isEmailMatchId.value = false
                _checkEmailForm.value = false
            }
        }
    }

    fun checkEmailForm(emailId:String, emailType: String){
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        val email = "${emailId}@${emailType}"

        _checkEmailForm.value = pattern.matcher(email).matches()
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

    fun onPasswordTextChanged(password: String) {
        when(validatePassword(password)){
            true ->  _isPasswordFormMatched.value = true
            false -> {
                _isPasswordFormMatched.value = false
                lastCheck = false
            }
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        when(checkPasswordsMatch(password, confirmPassword) && _isPasswordFormMatched.value == true){
            true -> {
                _isConfirmPasswordMatched.value = true
                lastCheck = true
            }
            false -> {
                _isConfirmPasswordMatched.value = false
                lastCheck = false
            }
        }
    }

    private fun checkPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword
    }

    suspend fun resetPassword(password : String){
        viewModelScope.launch {
            encryption = Encryption()
            val salt = encryption.generateSalt()
            val encryptedPassword = encryption.hashWithSalt(password, salt)
            val savedSalt = encryption.saltToString(salt)
            when(resetPasswordModel.resetPassword(_id.value.toString(),encryptedPassword, savedSalt)){
                "success" -> _isResetPassword.value = true
                else -> _isResetPassword.value = false
            }
        }
    }

}