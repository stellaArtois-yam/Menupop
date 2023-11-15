package com.example.menupop

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.databinding.SignupBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.log

class SignupActivity : AppCompatActivity() {

    val TAG = "SignupActivity"

    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: SignupBinding = DataBindingUtil.setContentView(this, R.layout.signup)

        signupViewModel = ViewModelProvider(this).get(SignupViewModel::class.java)
        binding.viewModel = signupViewModel
        binding.lifecycleOwner = this

        binding.signupIdDuplicationButton.setOnClickListener {
            var id = binding.signupIdEdittext.text.toString().replace(" ", "")

            if (id.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    signupViewModel.validateId(id)

                    if (binding.signupIdWarning.visibility == View.GONE) {
                        // 아이디 유효성 검사 후 중복 확인
                        signupViewModel.checkUserIdDuplication(id)
                    } else {
                        binding.signupIdWarning.visibility = View.VISIBLE
                        binding.signupIdWarning.text = "올바른 형식의 아이디를 입력해주세요."
                    }
                }
            }
        }



        signupViewModel.isIdDuplication.observe(this, Observer { isDuplicate ->
            Log.d(TAG, "isDuplicate: $isDuplicate")
            // 여기서 UI 업데이트 로직을 수행
            if(isDuplicate){
                binding.signupIdWarning.text = "이미 사용 중인 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
            }else{
                binding.signupIdWarning.text = "사용 가능한 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.BLUE)

                binding.signupIdEdittext.isEnabled = false
                binding.signupIdEdittext.isClickable = false
                binding.signupIdDuplicationButton.text = "재입력"
            }
        })






//        binding.signupIdDuplicationButton.setOnClickListener {
//            var id = binding.signupIdEdittext.text.toString().replace(" ", "")
//
//            if (id.isEmpty()) {
//
//                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
//
//            } else {
//
//                lifecycleScope.launch {
//                    //아이디 검사
//                    signupViewModel.validateId(id)
//
//
//                }
//
//                signupViewModel.isValidId.observe(this, Observer { validId ->
//                    Log.d(TAG, "validId : $validId")
//
//                    if(validId){
//                        binding.signupIdWarning.visibility = View.GONE
//                        signupViewModel.checkUserIdDuplication(id)
//                    }else{
//                        binding.signupIdWarning.visibility = View.VISIBLE
//                    }
//                })
//
//
//
//                signupViewModel.isIdDuplication.observe(this, Observer { isDuplicate ->
//                    Log.d(TAG, "isDuplicate: $isDuplicate")
//
//                    if (isDuplicate) {
//                        binding.signupIdWarning.text = "이미 사용 중인 아이디 입니다."
//                        binding.signupIdWarning.visibility = View.VISIBLE
//                    } else {
//                        binding.signupIdWarning.text = "사용 가능한 아이디 입니다."
//                        binding.signupIdWarning.visibility = View.VISIBLE
//                        binding.signupIdWarning.setTextColor(Color.BLUE)
//
//                        binding.signupIdEdittext.isEnabled = false
//                        binding.signupIdEdittext.isClickable = false
//                        binding.signupIdDuplicationButton.text = "재입력"
//                    }
//                })

//            }

//        }

    }
}









