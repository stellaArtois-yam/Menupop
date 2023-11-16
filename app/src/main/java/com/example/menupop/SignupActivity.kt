package com.example.menupop

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
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


        // 아이디 입력 감지
        binding.signupIdEdittext.addTextChangedListener{
            signupViewModel.onIdTextChanged(it.toString())
        }

        signupViewModel.idWarning.observe(this, Observer { warning->
            if(warning !=null){
                binding.signupIdWarning.text = warning
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)
            }else{
                binding.signupIdWarning.visibility = View.GONE
            }
        })



        binding.signupIdDuplicationButton.setOnClickListener {

            if(binding.signupIdDuplicationButton.text.equals("중복확인")){

                var id = binding.signupIdEdittext.text.toString().trim()
                if (id.isEmpty()) {
                    Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
//                        signupViewModel.validateId(id)
//                        val sibal = binding.signupIdWarning.visibility
//                        Log.d(TAG, "^^ : $sibal")

                        if (binding.signupIdWarning.visibility == View.GONE) {
                            Log.d(TAG, "유효성 검사 완")
                            signupViewModel.checkUserIdDuplication(id)

                        } else if(binding.signupIdWarning.text.contains("이미")){
                            Log.d(TAG, "^^2")
                            signupViewModel.checkUserIdDuplication(id)
                        } else if(binding.signupIdWarning.visibility == View.VISIBLE){
                            Log.d(TAG, "하하하하하하하하ㅏㅎㅎ")
                        }
                    }
                }
            }else{
                Log.d(TAG, "^^3")
                binding.signupIdWarning.visibility = View.GONE

                binding.signupIdEdittext.isEnabled = true
                binding.signupIdEdittext.isClickable = true
                binding.signupIdDuplicationButton.text = "중복확인"
            }
        }






        signupViewModel.isIdDuplication.observe(this, Observer { isDuplicate ->
            // 여기서 UI 업데이트 로직을 수행
            if(isDuplicate.equals("exist")){

                binding.signupIdWarning.text = "이미 사용 중인 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)

            }else if(isDuplicate.equals("available")){
                binding.signupIdWarning.text = "사용 가능한 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.BLUE)

                binding.signupIdEdittext.isEnabled = false
                binding.signupIdEdittext.isClickable = false
                binding.signupIdDuplicationButton.text = "재입력"
            }
        })

        //비밀번호 입력 감지
        binding.signupPasswordEdittext.addTextChangedListener{
            signupViewModel.onPasswordTextChanged(it.toString())
        }


        //비밀번호 확인 입력감지
        binding.signupPasswordConfirmEdittext.addTextChangedListener{
            val password = binding.signupPasswordEdittext.text.toString()
            val confrimPassword = it.toString()
            signupViewModel.onConfirmPasswordTextChanged(password, confrimPassword)
        }

        signupViewModel.passwordError.observe(this){error ->
            if(error==null){
                binding.signupPasswordWarning.visibility = View.GONE
            }else{
                binding.signupPasswordWarning.visibility = View.VISIBLE
                binding.signupPasswordWarning.text = error
            }
        }

        signupViewModel.confirmPasswordError.observe(this){error->
            if(error ==null){
                binding.signupPasswordConfirmWarning.visibility = View.GONE
            }else{
                binding.signupPasswordConfirmWarning.visibility = View.VISIBLE
                binding.signupPasswordConfirmWarning.text = error
            }
        }


        //이메일 입력 감지
        binding.signupEmailIdEdittext.addTextChangedListener{
            signupViewModel.setEmailId(it.toString())
        }



        binding.signupEmailSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                signupViewModel.setEmailSelection(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }

        }



        signupViewModel.warningMessage.observe(this, Observer { warning->
            if(warning != null){
                binding.signupEmailWarning.text = warning
                binding.signupEmailWarning.visibility = View.VISIBLE
            }else{
                binding.signupEmailWarning.visibility = View.GONE
            }
        })

        // 이메일 아이디 EditText나 스피너 포커스 잃었을 때 유효성 검사 실행
        binding.signupEmailIdEdittext.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                signupViewModel.validateInputs()
            }
        }


        binding.signupEmailSelection.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                signupViewModel.validateInputs()

            }
        }

        // 인증번호 클릭 하면 인증하기로 바뀜
        binding.signupCertificationButton.setOnClickListener{
            if(binding.signupCertificationButton.text.equals("인증번호")){

                val id = binding.signupEmailIdEdittext.text.toString().trim()

                if(id.isEmpty()){
                    binding.signupEmailWarning.visibility = View.VISIBLE
                }else{
                    binding.signupCertificationButton.text = "확인"
                    lifecycleScope.launch {
                        signupViewModel.requestEmailAuth(id)
                    }
                }



            }else if(binding.signupCertificationButton.text.equals("확인")){
                val authCode = binding.signupCertificationEdittext.text.trim()
                Log.d(TAG, "authCode: $authCode")

                if(authCode.isEmpty()){
                    binding.signupCertificationWarning.visibility = View.VISIBLE
                    binding.signupCertificationWarning.text = "인증번호를 입력해주세요."
                }else{
                    //입력한 인증번호와 클라이언트가 가지고 있는 인증번호가 같으면 pass
                }
            }

        }

        // 가입 버튼 리스너 설정
//        binding.signupSubmitButton.setOnClickListener {
//            val id = binding.signupIdEdittext.text.toString().trim()
//            val password = binding.signupPasswordEdittext.text.toString()
//            val confirmPassword = binding.signupPasswordConfirmEdittext.text.toString()
//            val emailId = binding.signupEmailIdEdittext.text.toString().trim()
//            val emailSelection = binding.signupEmailSelection.selectedItem.toString()
//
//            // ViewModel에 가입 정보 전달
//            signupViewModel.signup(id, password, confirmPassword, emailId, emailSelection)
//        }
//
//        // ViewModel로부터 가입 가능 여부를 관찰하여 UI 업데이트
//        signupViewModel.isSignupEnabled.observe(this, { isSignupEnabled ->
//            binding.signupButton.isEnabled = isSignupEnabled
//        })



    }
}









