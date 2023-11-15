package com.example.menupop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.databinding.LoginBinding
import com.example.menupop.databinding.SignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private var TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        val binding: LoginBinding = DataBindingUtil.setContentView(this, R.layout.login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.loginViewModel = loginViewModel
        binding.lifecycleOwner = this

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

            loginViewModel.loginResult.observe(this, Observer { it ->
                Log.d(TAG, "onCreate:  ${it.identifier} ${it.result}")
            })

        }
    }
}

