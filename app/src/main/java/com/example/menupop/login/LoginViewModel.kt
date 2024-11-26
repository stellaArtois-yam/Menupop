package com.example.menupop.login

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.menupop.Encryption
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) :  AndroidViewModel(application) {
    companion object{
        const val TAG = "LoginViewModel"
    }


    private var loginModel = LoginModel(application)

    private val _mergeAccount = MutableLiveData<LoginResponseModel>()
    val mergeAccount : LiveData<LoginResponseModel>
        get() = _mergeAccount

    lateinit var naverProfileCallback : NidProfileCallback<NidProfileResponse>
    private lateinit var naverOauthLoginCallback : OAuthLoginCallback

    private val _loginResult = MutableLiveData<LoginResponseModel>()
    val loginResult: LiveData<LoginResponseModel>
        get() = _loginResult

    private val _socialLoginResult = MutableLiveData<LoginResponseModel>()
    val socialLoginResult : LiveData<LoginResponseModel>
        get() = _socialLoginResult

    suspend fun getSalt(id : String, password: String){
        viewModelScope.launch {
            val response = loginModel.getSalt(id)
            when(response.result){
                "success" -> {
                    val encryption = Encryption()
                    val salt = encryption.stringToSalt(response.salt)
                    val encryptedPassword = encryption.hashWithSalt(password, salt)
                    requestLogin(id, encryptedPassword)
                }
                else -> {
                    _loginResult.value = LoginResponseModel(response.result,0)
                }
            }
        }
    }
    private suspend fun requestLogin(id : String, password : String) {
        viewModelScope.launch {
            val response = loginModel.requestLogin(id, password)
            Log.d(TAG, "requestLogin: ${response.result}")
            _loginResult.value = response
        }
    }

    fun saveIdentifier(sharedPreferences: SharedPreferences,identifier : Int){
        loginModel.saveUserIdentifier(sharedPreferences, identifier)
    }
    fun socialLoginRequest(email:String){
        viewModelScope.launch{
            val response = loginModel.socialLoginRequest(email)
            _socialLoginResult.value = response
        }
    }
    fun socialAccountMergeLocalAccount(identifier: Int){
        viewModelScope.launch{
            val response = loginModel.socialAccountMergeLocalAccount(identifier)
            _mergeAccount.value = response
        }
    }
    fun requestNaverSocialLogin(){
        naverProfileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val email = response.profile?.email
                socialLoginRequest(email!!)
                Log.d(TAG, "onSuccess: $email")
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: $errorCode, $errorDescription")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        naverOauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                NidOAuthLogin().callProfileApi(naverProfileCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: $errorCode, $errorDescription")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        loginModel.requestNaverSocialLogin(naverOauthLoginCallback)

    }
}