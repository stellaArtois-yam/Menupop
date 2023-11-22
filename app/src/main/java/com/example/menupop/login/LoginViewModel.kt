package com.example.menupop.login

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private var loginInfomation = MutableLiveData<LoginResponseModel>()
    private var loginModel = LoginModel()
    private  var callback:((LoginResponseModel) -> Unit) ?= null
    val mergeAccount = MutableLiveData<LoginResponseModel>()
    val TAG = "LoginViewModel"
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
            Log.d(TAG, "socialLoginRequest: 들어옴")
            socialLoginResult.value = socialloginResponse
        }
        loginModel.socialLoginRequest(email,callback!!)
    }
    fun socialAccountMergeLocalAccount(identifier: Int){
        callback = {mergeAccountResponse ->
            Log.d(TAG, "socialLoginRequest: 들어옴")
            mergeAccount.value = mergeAccountResponse
        }
        loginModel.socialAccountMergeLocalAccount(identifier,callback!!)
    }

}