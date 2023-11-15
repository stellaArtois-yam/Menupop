package com.example.menupop

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern
import kotlin.math.log


class SignupViewModel : ViewModel() {

    val TAG = "SignupViewModel"

    var isPasswordRegexp = MutableLiveData<Boolean>()
    var isPasswordMatch = MutableLiveData<Boolean>()

    var signupModel  = SignupModel()



    // 아이디 유효성 검사
    private val _isValidId = MutableLiveData<Int>()
    private val _validIdWarning = MutableLiveData<String>()
    val isValidId: LiveData<Int>
        get() = _isValidId

    val validIdWarning : LiveData<String>
        get() = _validIdWarning

    fun validateId(inputId: String)  {
        val regex = Regex("^[a-zA-Z0-9]{6,12}\$")

        var isValid = View.GONE

        if(!regex.matches(inputId)){
            isValid = View.VISIBLE
        }
        Log.d(TAG, "validateId: $isValid")
        _isValidId.value = isValid

    }

    // 아이디 중복 확인
    var isIdDuplication = MutableLiveData<Boolean>()
    fun checkUserIdDuplication(id : String) {
        val callback : (Boolean) -> Unit = {isDuplicate ->
            Log.d(TAG, "checkUserIdDuplication: $isIdDuplication")

            isIdDuplication.value = isDuplicate
        }

        val result = signupModel.requestIdDuplication(id, callback)

    }






}