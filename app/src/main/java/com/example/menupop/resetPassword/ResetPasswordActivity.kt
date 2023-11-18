package com.example.menupop.resetPassword

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity(),ResetPasswordFragmentEvent {
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private var TAG = "ResetPasswordActivity"
    private lateinit var binding : ActivityResetPasswordBinding
    private lateinit var checkIdFragment : ResetPasswordCheckIdFragment
    private lateinit var verifyEamilFragment: ResetPasswordEamilFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }
    fun init() {
        Log.d(TAG, "init: 호출")

        checkIdFragment = ResetPasswordCheckIdFragment()
        verifyEamilFragment = ResetPasswordEamilFragment()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        resetPasswordViewModel = ViewModelProvider(this).get(ResetPasswordViewModel::class.java)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.resetPasswordContainer,checkIdFragment)
            commit()
        }

    }

    // 아이디 있을때 이메일 인증으로 넘어감
    override fun existId() {
        Log.d(TAG, "existId: 호출됨")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.resetPasswordContainer,verifyEamilFragment)
            commit()
        }
    }

    override fun successVerifyEmail() {
        Log.d(TAG, "successVerifyEmail: 호출됨")
    }

    override fun successResetPassword() {
        Log.d(TAG, "successResetPassword: 호출됨")
    }

}