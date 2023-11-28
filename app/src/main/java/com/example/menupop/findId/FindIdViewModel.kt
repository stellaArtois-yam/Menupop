package com.example.menupop.findId

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern


class   FindIdViewModel :ViewModel() {
    val TAG = "FindIdViewModel"

    val findIdModel = FindIdModel()

    var callback : ((FindIdResponseModel) -> Unit)? = null

    private val _checkEmailForm = MutableLiveData<Boolean>()
    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    private val _userIdExistence = MutableLiveData<FindIdResponseModel>()
    val userIdExistence : LiveData<FindIdResponseModel>
        get() = _userIdExistence

    private val _emailWarning = MutableLiveData<String>()

    val emailWarning : LiveData<String>
        get() = _emailWarning

    private  val _idResult = MutableLiveData<String>()
    val idResult : LiveData<String>
        get() = _idResult



    fun checkEmailForm(email : String, domain : String){
        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        val email = "${email}@${domain}"
        Log.d(TAG, "checkEmailForm: ${email}")
        _checkEmailForm.value = pattern.matcher(email).matches()

        if(_checkEmailForm.value == true){
            _emailWarning.value = null
        }else{
            _emailWarning.value = "올바른 이메일 형식이 아닙니다."
        }
    }

    fun checkUserId(email : String, domain: String){
        val email = "${email}@${domain}"
        callback = {status ->
            Log.d(TAG, "checkUserId status: ${status}")
            _userIdExistence.value = status

            if(_userIdExistence.value!!.result == "exist"){
                _idResult.value = maskLastTwoChars(_userIdExistence.value!!.id)
            }else{
                _idResult.value = "존재하지 않습니다."
            }

        }
        findIdModel.checkUserId(email, callback!!)
    }

    fun maskLastTwoChars(inputString: String): String {
        if (inputString.length >= 2) {
            val maskedPart = inputString.substring(0, inputString.length - 2) + "**"
            return maskedPart
        }
        return inputString
    }

}