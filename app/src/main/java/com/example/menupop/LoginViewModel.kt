package com.example.menupop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope

class LoginViewModel : ViewModel() {
    private var loginInfomation = MutableLiveData<LoginResponseModel>()
    private var loginModel : LoginModel = LoginModel()
    val loginResult: LiveData<LoginResponseModel>
        get() = loginInfomation
    suspend fun requestLogin(id : String, password : String) {
            val result = loginModel.requestLogin(id, password)
            loginInfomation.value = result
    }

}