package com.example.menupop


import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import java.util.regex.Pattern
import kotlin.math.log


class SignupViewModel : ViewModel() {

    val TAG = "SignupViewModel"
    var signupModel  = SignupModel()

    // 아이디
    private val _isValidId = MutableLiveData<Int>().apply {
        value = View.GONE }

//    private val _isValidId = MutableLiveData<Int>()
    val isValidId: LiveData<Int>
        get() = _isValidId


    private val _idWarning = MutableLiveData<String>()
    val idWarning : LiveData<String>
        get() = _idWarning

    val isIdDuplication = MutableLiveData<String>()

    private val _isEnableButton = MutableLiveData<Boolean>()
    val isEnableButton : LiveData<Boolean>
        get() = _isEnableButton

    private val _selectedSpinnerItem = MutableLiveData<String>()
    val selectedSpinnerItem: LiveData<String>
        get() = _selectedSpinnerItem

    // 스피너 아이템이 선택되었을 때 처리
    fun onSpinnerItemSelected(item: String) {
        _selectedSpinnerItem.value = item
        // 스피너가 "선택"일 경우 버튼 비활성화와 경고 메시지 표시
        if (item == "선택") {
            _warningMessage.value = "이메일 도메인을 선택해주세요." // 경고 메시지
            _isEnableButton.value = false // 버튼 비활성화
        } else {
            _warningMessage.value = null // 경고 메시지 지우기
            _isEnableButton.value = true // 버튼 활성화
        }
    }





    // view를 감지해서 view가 변화하면 실행한다...!

    fun validateId(inputId: String)  {

        val regex = Regex("^[a-zA-Z0-9]{6,12}\$")

        var isValid = View.GONE
        var validIdWarning = ""

        if(!regex.matches(inputId)){
            isValid = View.VISIBLE
            Log.d(TAG, "validateId not match: $isValid")
            validIdWarning = "아이디는 6-12자 이내, 영문, 숫자 사용 가능"
        }else{
            Log.d(TAG, "validateId match : $isValid")
        }

        _isValidId.value = isValid
        _idWarning.value = validIdWarning
    }



//    fun onIdTextChanged(id: String) {
//        if (validateId(id)) {
//            _idWarning.value = null
//            Log.d(TAG, "onIdTextChanged: null")
//        } else {
//            _idWarning.value = "아이디는 6-12자 이내, 영문, 숫자 사용 가능"
//        }
//    }

    //비밀번호
    private val _passwordError = MutableLiveData<String>()
    val passwordError : LiveData<String>
        get() = _passwordError

    private val _confirmPasswordError = MutableLiveData<String>()
    val confirmPasswordError : LiveData<String>
        get() = _confirmPasswordError

    private val _emailId = MutableLiveData<String>()
    val emailId : LiveData<String>
        get() = _emailId


    private val _warningMessage = MutableLiveData<String>()
    val warningMessage: LiveData<String>
        get() = _warningMessage

    private val _emailSelection = MutableLiveData<String>()
    val emailSelection: LiveData<String>
        get() = _emailSelection


    fun setEmailId(input: String) {
        Log.d(TAG, "setId: $input")
        _emailId.value = input
    }


    fun setEmailSelection(selection: String) {
        Log.d(TAG, "setEmailSelection:$selection ")
        _emailSelection.value = selection
    }

    fun validateInputs() {
        val emailId = _emailId.value
        val emailSelectionValue = _emailSelection.value

        if (emailId.isNullOrBlank() || emailSelectionValue.equals("선택")) {
            _warningMessage.value = "이메일 아이디 또는 이메일을 선택하세요."
            Log.d(TAG, "validateInputs: 1")
        } else {
            _warningMessage.value = null
            Log.d(TAG, "validateInputs: 2")
        }
    }


    fun requestEmailAuth(email : String) {
        val fullEmailAdress = "$email@$emailSelection"
        val callback : (Int) -> Unit = {it ->
            Log.d(TAG, "requestEmailAuth: $it")
        }
        signupModel.requestEmailAuthCode(fullEmailAdress, callback)
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
        }
    }

    fun onConfirmPasswordTextChanged(password: String, confirmPassword: String) {
        if (checkPasswordsMatch(password, confirmPassword)) {
            _confirmPasswordError.value = null
        } else {
            _confirmPasswordError.value = "비밀번호가 일치하지 않습니다."
        }
    }


    private val _isSignupEnabled = MutableLiveData<Boolean>().apply { value = false }


    fun checkUserIdDuplication(id : String) {
        val callback : (String) -> Unit = {isDuplicate ->
            Log.d(TAG, "checkUserIdDuplication: $isIdDuplication")

            isIdDuplication.value = isDuplicate

//            if(isIdDuplication.value.equals("exist")){
//                _isSignupEnabled.value = false
//            }else{
//                _isSignupEnabled.value = true
//            }
        }
        signupModel.requestIdDuplication(id, callback!!)
    }


    init {
        // 기본적으로 가입 버튼 비활성화
        _isSignupEnabled.value = false
    }

//    fun signup(id: String, password: String, confirmPassword: String, emailId: String, emailSelection: String) {
//        // 비밀번호 확인 및 이메일 선택 로직 추가
//        val isPasswordValid = // 비밀번호 일치 여부 (true or false)
//        val isEmailValid = // 이메일 아이디 및 도메인 선택 여부 (true or false)
//
//            // 아이디 중복 확인 여부, 비밀번호 확인, 이메일 선택 여부가 모두 만족하면 가입 가능
//            _isSignupEnabled.value = isIdValid && isPasswordValid && isEmailValid
//    }









}