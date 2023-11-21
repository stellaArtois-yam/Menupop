package com.example.menupop

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.databinding.SignupBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    val TAG = "SignupActivity"

    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: SignupBinding = DataBindingUtil.setContentView(this, R.layout.signup)

        signupViewModel = ViewModelProvider(this).get(SignupViewModel::class.java)
        binding.viewModel = signupViewModel
        binding.lifecycleOwner = this


        /**
         * 1. edittext의 아이디 입력 감지
         */
        binding.signupIdEdittext.addTextChangedListener {
            Log.d(TAG, "it: ${it.toString()}")
            signupViewModel.onIdTextChanged(it.toString())
        }

        /**
         * 2. 아이디 유효성 검사 경고 문구 관찰
         */
        signupViewModel.idWarning.observe(this, Observer { warning ->
            if (warning != null) {
                binding.signupIdWarning.text = warning
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)
            } else {
                binding.signupIdWarning.visibility = View.GONE
            }
        })


        /**
         * 3. 아이디 중복검사 버튼을 누르면 유효성 검사가 완료되면 중복검사 시행
         */
        binding.signupIdDuplicationButton.setOnClickListener {

            if (binding.signupIdDuplicationButton.text.equals("중복확인")) {

                var id = binding.signupIdEdittext.text.toString().trim()

                if (id.isEmpty()) {
                    Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {

                        if (binding.signupIdWarning.visibility == View.GONE) {
                            Log.d(TAG, "유효성 검사 완")
                            signupViewModel.checkUserIdDuplication(id)
                        }
                    }
                }
            } else {
                /**
                 * 재입력인 경우
                 */
                binding.signupIdWarning.visibility = View.GONE

                binding.signupIdEdittext.isEnabled = true
                binding.signupIdEdittext.isClickable = true
                binding.signupIdDuplicationButton.text = "중복확인"
            }
        }


        /**
         * 중복검사 조회
         */
        signupViewModel.isIdDuplication.observe(this, Observer { isDuplicate ->
            Log.d(TAG, "중복검사")
            if (isDuplicate == "exist") {
                Log.d(TAG, "exist")

                binding.signupIdWarning.text = "이미 사용 중인 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)

            } else if (isDuplicate == "available") {
                Log.d(TAG, "available")
                binding.signupIdWarning.text = "사용 가능한 아이디 입니다."
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.BLUE)

                binding.signupIdEdittext.isEnabled = false
                binding.signupIdEdittext.isClickable = false
                binding.signupIdDuplicationButton.text = "재입력"
            }
        })

        /**
         * 1. 비밀번호 입력 감지
         */
        binding.signupPasswordEdittext.addTextChangedListener {
            signupViewModel.onPasswordTextChanged(it.toString())
        }


        binding.signupPasswordConfirmEdittext.addTextChangedListener {
            val password = binding.signupPasswordEdittext.text.toString()
            val confrimPassword = it.toString()
            signupViewModel.onConfirmPasswordTextChanged(password, confrimPassword)
        }

        /**
         * 2. 비밀번호 유효성 검사 경고문 관찰
         */
        signupViewModel.passwordWarning.observe(this) { error ->
            if (error == null) {
                binding.signupPasswordWarning.visibility = View.GONE
            } else {
                binding.signupPasswordWarning.visibility = View.VISIBLE
                binding.signupPasswordWarning.text = error
            }
        }
        /**
         * 3. 비밀번호, 비밀번호 확인 일치 관찰
         */
        signupViewModel.confirmPasswordWarning.observe(this) { error ->
            if (error == null) {
                binding.signupPasswordConfirmWarning.visibility = View.GONE
            } else {
                binding.signupPasswordConfirmWarning.visibility = View.VISIBLE
                binding.signupPasswordConfirmWarning.text = error
            }
        }


        /**
         * 1. 이메일 입력 감지
         */

        binding.signupEmailWarning.visibility = View.GONE

        binding.signupEmailIdEdittext.addTextChangedListener {
            signupViewModel.setEmailId(it.toString())

        }

        /**
         * 2. 도메인 선택 감지
         */
        binding.signupEmailSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    Log.d(TAG, "onItemSelected: $selectedItem")
                    signupViewModel.setEmailSelection(selectedItem)

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Nothing to do
                }

            }


        /**
         * 3. 이메일 경고 문구 관찰
         */
        signupViewModel.emailWarning.observe(this, Observer { warning ->

            if (!warning.isNullOrEmpty()) {
                binding.signupEmailWarning.text = warning
                binding.signupEmailWarning.visibility = View.VISIBLE
            } else {
                binding.signupEmailWarning.visibility = View.GONE
            }
        })


        /**
         * 이메일을 명확히 입력하면 인증번호 버튼이 활성화
         */
        binding.signupCertificationButton.setOnClickListener {
            if (binding.signupCertificationButton.text.equals("인증번호") ||
                binding.signupCertificationButton.text.equals("재인증")
            ) {

                val id = binding.signupEmailIdEdittext.text.toString().trim()

                binding.signupCertificationButton.text = "확인"
                signupViewModel.startTimer() //타이머 시작
                binding.signupCertificationWarning.visibility = View.VISIBLE

                lifecycleScope.launch {
                    signupViewModel.requestEmailAuth(id)

                }

            } else if (binding.signupCertificationButton.text.equals("확인")) {
                val authCode = binding.signupCertificationEdittext.text.toString().trim()
                Log.d(TAG, "authCode: $authCode")
                signupViewModel.checkVerifyCode(authCode)
            }

        }

        signupViewModel.remainingTime.observe(this, Observer { time ->
            binding.signupCertificationWarning.text = "시간 제한 : ${time}"

            if (time == "00:00") {
                Log.d(TAG, "timer 종료")
                binding.signupCertificationButton.text = "재인증"
                binding.signupCertificationWarning.text = "인증번호가 만료되었습니다."
                binding.signupSubmitButton.isEnabled = false
            }

        })

        signupViewModel.verifyCompleted.observe(this, Observer { result ->
            if (result) {
                signupViewModel.stopTimer()
                binding.signupCertificationWarning.text = "인증 완료"
                binding.signupCertificationWarning.setTextColor(Color.BLUE)

            }
        })

        // 가입 버튼 리스너 설정
        binding.signupSubmitButton.setOnClickListener {
            Log.d(TAG, "click?")
            signupViewModel.checkUserInformation()

            if(binding.signupSubmitButton.isEnabled){

                val id = binding.signupIdEdittext.text.toString().trim()
                val password = binding.signupPasswordEdittext.toString().trim().hashCode().toString()
                val email = "${binding.signupEmailIdEdittext.text.toString().trim()}@${binding.signupEmailSelection.selectedItem}"
                val identifier = id.hashCode()

                signupViewModel.sendUserInformation(id, password, email, identifier)
            }else{
                Toast.makeText(this, "입력하지 않은 항목이 있습니다.",Toast.LENGTH_LONG).show()
            }
        }

        signupViewModel.saveResult.observe(this, Observer { result ->
            if(result){
                showCustomDialog("회원가입 완료", "반갑습니다 :)", true)

            }else{
                showCustomDialog("회원가입 실패", "다시 시도해주세요 :(", false)
            }
        })
    }
    private fun showCustomDialog(title : String, content : String, success : Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_warning)

        val titleTextView : TextView = dialog.findViewById(R.id.dialog_title)
        val contentTextView : TextView = dialog.findViewById(R.id.dialog_content)

        titleTextView.text = title
        contentTextView.text = content

        if(success){
            dialog.setOnDismissListener{
                var intent = Intent(this,LoginActivity :: class.java)
                startActivity(intent)
            }
        }

        dialog.show()
    }
}









