package com.example.menupop.Login

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private var loginInfomation = MutableLiveData<LoginResponseModel>()
    private var loginModel = LoginModel()
    private  var callback:((LoginResponseModel) -> Unit) ?= null
    val loginResult: LiveData<LoginResponseModel>
        get() = loginInfomation
    val socialLoginResult = MutableLiveData<LoginResponseModel>()
    fun requestLogin(id : String, password : String) {
        callback = { loginResponse ->
            loginInfomation.value = loginResponse
        }
        loginModel.requestLogin(id, password, callback!!)

    }
    fun saveIdentifier(sharedPreferences: SharedPreferences,identifier : Int){
        loginModel.saveUserIdentifier(sharedPreferences,identifier)
    }
    fun socialLoginRequest(email:String){
        callback = {socialloginResponse ->
            socialLoginResult.value = socialloginResponse
        }
        loginModel.socialLoginRequest(email,callback!!)
    }

}