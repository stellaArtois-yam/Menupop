package com.example.menupop.resetPassword

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ResetPasswordViewModel : ViewModel() {
    private val TAG = "ResetPasswordViewModel"
    private val resetPasswordModel = ResetPasswordModel()
    private var callback: ((String) -> Unit) ?= null

    private var _checkIdResult = MutableLiveData<Boolean>()
    val checkIdResult : LiveData<Boolean>
        get() = _checkIdResult

    private var _checkEmailForm = MutableLiveData<Boolean>() //이메일 형식

    fun checkId(id : String){
        callback = { result ->
            Log.d(TAG, "checkId: ${result}")
            if (result == "exist"){
                _checkIdResult.value = true
            } else{
                _checkIdResult.value = false
            }
        }
        resetPasswordModel.checkId(id,callback!!)
    }

    fun sendVerifyCode(email :String){
        callback = { result ->
            Log.d(TAG, "checkId: ${result}")

        }
        resetPasswordModel.sendVerifyCode(email,callback!!)
    }

    fun checkVerifyCode(verifyCode : String) {
    }

    fun emailSelection(emailType: String){

    }

}