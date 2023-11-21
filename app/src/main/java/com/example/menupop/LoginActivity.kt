package com.example.menupop

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
import com.example.menupop.resetPassword.ResetPasswordActivity
import com.example.menupop.databinding.LoginBinding
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private var TAG = "LoginActivity"
    private lateinit var binding : LoginBinding
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

        }
    }
    private fun kakaoLoginRequest(){
    }
    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_warning)
        dialog.show()
    }
}

