package com.example.menupop.findId

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class   FindIdViewModel :ViewModel() {
    companion object{
        const val TAG = "FindIdViewModel"
    }

    private val findIdModel = FindIdModel()

    private val _checkEmailForm = MutableLiveData(true)
    val checkEmailForm : LiveData<Boolean>
        get() = _checkEmailForm

    private val _userIdExistence = MutableLiveData<FindIdResponseDTO>()
    val userIdExistence : LiveData<FindIdResponseDTO>
        get() = _userIdExistence

    private val _emailWarning = MutableLiveData<String?>()

    val emailWarning : LiveData<String?>
        get() = _emailWarning

    private  val _idResult = MutableLiveData<String>()
    val idResult : LiveData<String>
        get() = _idResult

    fun checkEmailForm(emailId : String, domain : String){
        val pattern : Pattern = Patterns.EMAIL_ADDRESS
        val email = "${emailId}@${domain}"

        _checkEmailForm.value = pattern.matcher(email).matches()

        when(_checkEmailForm.value){
            else -> _emailWarning.value = "올바른 이메일 형식이 아닙니다."
        }
    }

    suspend fun checkUserId(emailId : String, domain: String){
        val email = "${emailId}@${domain}"
        viewModelScope.launch {
            val response =  findIdModel.checkUserId(email)
            _userIdExistence.value = response
            when(response.result){
                "exist" -> _idResult.value = _userIdExistence.value!!.id
                else -> _idResult.value = "존재하지 않습니다."
            }
        }
    }
}