package com.example.menupop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.databinding.SignupBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var singupViewModel :SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : SignupBinding = DataBindingUtil.setContentView(this, R.layout.signup)

        singupViewModel = ViewModelProvider(this).get(SignupViewModel::class.java)
        binding.viewModel = singupViewModel
        binding.lifecycleOwner = this

        binding.signupIdDuplicationButton.setOnClickListener(){
            lifecycleScope.launch {
                singupViewModel.checkUserIdDuplication()

            }
        }







    }

}