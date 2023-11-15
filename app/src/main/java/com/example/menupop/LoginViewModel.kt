package com.example.menupop

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope

class LoginViewModel : ViewModel() {
    private var loginInfomation = MutableLiveData<LoginResponseModel>()
    private var loginModel = LoginModel()
    private  var callback:((LoginResponseModel) -> Unit) ?= null
    val loginResult: LiveData<LoginResponseModel>
        get() = loginInfomation
    fun requestLogin(id : String, password : String) {
        callback = { loginResponse ->
            loginInfomation.value = loginResponse
        }
        loginModel.requestLogin(id, password, callback!!)

    }
    fun saveIdentifier(sharedPreferences: SharedPreferences,identifier : Int){
        loginModel.saveUserIdentifier(sharedPreferences,identifier)
    }

}