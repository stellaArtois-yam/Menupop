package com.example.menupop.Login

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.SignupActivity
import com.example.menupop.resetPassword.ResetPasswordActivity
import com.example.menupop.databinding.LoginBinding
import com.example.menupop.findId.FindIdActivity
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
        loginViewModel.socialLoginResult.observe(this){ result ->
            Log.d(TAG, "onCreate: ${result.isNewUser} ${result.identifier} ${result.result}")
        }
    }
    fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this
    }
    private fun isNewUserCheck(isNewUser:Int){
        if (isNewUser == 1){
            var intent = Intent(this, SignupActivity :: class.java)
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
                    loginViewModel.requestLogin(id, password.hashCode().toString())
                }
            }
        }
        binding.loginSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        binding.loginFindId.setOnClickListener {
            val intent = Intent(this,FindIdActivity::class.java)
            startActivity(intent)
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

    private fun socialLoginRequest(email : String){
        loginViewModel.socialLoginRequest(email)
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
    }


}

