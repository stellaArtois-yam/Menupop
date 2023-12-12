package com.example.menupop.login

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

class LoginViewModel(private var application: Application) :  AndroidViewModel(application) {
    private var loginInfomation = MutableLiveData<LoginResponseModel>()
    private var loginModel = LoginModel(application)
    private  var callback:((LoginResponseModel) -> Unit) ?= null
    val mergeAccount = MutableLiveData<LoginResponseModel>()
    lateinit var kakaoCallback : (OAuthToken?, Throwable?) -> Unit
    lateinit var naverProfileCallback : NidProfileCallback<NidProfileResponse>
    lateinit var naverOauthLoginCallback : OAuthLoginCallback

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
    fun requestNaverSocialLogin(){
        naverProfileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val email = response.profile?.email
                socialLoginRequest(email!!)
                Log.d(TAG, "onSuccess: ${email}")
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: ${errorCode} ${errorDescription}")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        /** OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다. */
        naverOauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                NidOAuthLogin().callProfileApi(naverProfileCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d(TAG, "onFailure: ${errorCode} ${errorDescription}")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        loginModel.requestNaverSocialLogin(naverOauthLoginCallback)


    }
    fun requestKakaoSocialLogin(){
        kakaoCallback = { token, error ->
            if (error != null) {
                Log.d(TAG, "setKakaoCallback: ${error.toString()}")
            }
            else if (token != null) {
                Log.d("[카카오로그인]","로그인에 성공하였습니다.\n${token.accessToken}")
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    UserApiClient.instance.me { user, error ->
                        Log.d(TAG, "setKakaoCallback: 닉네임: ${user?.kakaoAccount?.profile?.nickname} 이메일 : ${user?.kakaoAccount?.email}")
                        socialLoginRequest(user?.kakaoAccount?.email.toString())
                    }
                }
            }
            else {
                Log.d(TAG, "setKakaoCallback: 토큰==null")
            }
        }
        loginModel.requestKakaoSocialLogin(kakaoCallback)
    }

}