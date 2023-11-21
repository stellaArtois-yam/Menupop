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

    val _checkEmailForm = MutableLiveData<Boolean>()
    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    val userIdExistence = MutableLiveData<FindIdResponseModel>()


    fun checkEmailForm(email : String, domain : String){
        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        val email = "${email}@${domain}"
        Log.d(TAG, "checkEmailForm: ${email}")
        _checkEmailForm.value = pattern.matcher(email).matches()
    }

    fun checkUserId(email : String, domain: String){
        val email = "${email}@${domain}"
        callback = {status ->
            Log.d(TAG, "checkUserId status: ${status}")
            userIdExistence.value = status

        }
        findIdModel.checkUserId(email, callback!!)
    }

}