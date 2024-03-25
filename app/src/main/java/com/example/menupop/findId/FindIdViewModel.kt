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

    var callback : ((FindIdResponseDTO) -> Unit)? = null

    private val _checkEmailForm = MutableLiveData<Boolean>()
    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    private val _userIdExistence = MutableLiveData<FindIdResponseDTO>()
    val userIdExistence : LiveData<FindIdResponseDTO>
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

        _checkEmailForm.value = pattern.matcher(email).matches()

        when(_checkEmailForm.value){
            true -> _emailWarning.value = null
            else -> _emailWarning.value = "올바른 이메일 형식이 아닙니다."
        }
    }

    fun checkUserId(email : String, domain: String){
        val email = "${email}@${domain}"
        callback = {status ->
            _userIdExistence.value = status

            when(_userIdExistence.value!!.result){
                "exist" -> _idResult.value = _userIdExistence.value!!.id

                else -> _idResult.value = "존재하지 않습니다."
            }

        }
        findIdModel.checkUserId(email, callback!!)
    }


}