package com.example.menupop

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.resetPassword.ResetPasswordActivity
import com.example.menupop.databinding.LoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private  val TAG = "LoginActivity"
    private lateinit var binding : LoginBinding
    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        init()

        initOnClickListener()


        loginViewModel.loginResult.observe(this, Observer { it ->
            Log.d(TAG, "onCreate: ${it.isNewUser} ${it.identifier} ${it.result}")
            if (it.result == "failed"){
                showCustomDialog()
            } else {
                var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
                loginViewModel.saveIdentifier(sharedPreferences,it.identifier)
                isNewUserCheck(it.isNewUser)
            }
        })
    }
    fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this
    }
    private fun isNewUserCheck(isNewUser:Int){
        if (isNewUser == 1){
            var intent = Intent(this,SignupActivity :: class.java)
            startActivity(intent)
            return
        }
    }
    private fun initOnClickListener(){
        binding.loginButton.setOnClickListener {
            var id = binding.loginIdEditText.text.toString().replace(" ", "")
            var password = binding.loginPasswordEditText.text.toString().replace(" ", "")
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    loginViewModel.requestLogin(id, password)
                }
            }
        }
        binding.loginSignup.setOnClickListener {
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
        }
        binding.loginFindId.setOnClickListener {
//            val intent = Intent(this,::class.java)
//            startActivity(intent)
        }
        binding.loginFindPassword.setOnClickListener {
            val intent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.googleLoginButton.setOnClickListener {

        }
        binding.naverLoginButton.setOnClickListener {

        }
        binding.kakaoLoginButton.setOnClickListener {
            kakaoLoginRequest()
            getHash()
        }
    }
    private fun getHash(){
        val keyHash = Utility.getKeyHash(this)
        Log.d(TAG, "getHash: ${keyHash}")
    }
    private fun kakaoLoginRequest(){
        KakaoSdk.init(this,getString(R.string.KAKAO_NATIVE_APP_KEY))
        setKakaoCallback()
        btnKakaoLogin()
    }
    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_warning)
        dialog.show()
    }

    fun btnKakaoLogin() {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = kakaoCallback)
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
        }
    }

    fun setKakaoCallback() {
        kakaoCallback = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Log.d(TAG,"접근이 거부 됨(동의 취소)")
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Log.d(TAG,"유효하지 않은 앱")
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Log.d(TAG,"인증 수단이 유효하지 않아 인증할 수 없는 상태")
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Log.d(TAG,"요청 파라미터 오류")
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Log.d(TAG,"유효하지 않은 scope ID")
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Log.d(TAG,"설정이 올바르지 않음(android key hash)")
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Log.d(TAG,"서버 내부 에러")
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Log.d(TAG,"앱이 요청 권한이 없음")
                    }
                    else -> { // Unknown
                        Log.d(TAG,error.toString())
                    }
                }
            }
            else if (token != null) {
                Log.d("[카카오로그인]","로그인에 성공하였습니다.\n${token.accessToken}")
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    UserApiClient.instance.me { user, error ->
                        Log.d(TAG, "setKakaoCallback: 닉네임: ${user?.kakaoAccount?.profile?.nickname} 이메일 : ${user?.kakaoAccount?.email}")
                    }
                }
            }
            else {
                Log.d("카카오로그인", "토큰==null error==null")
            }
        }
    }
}

