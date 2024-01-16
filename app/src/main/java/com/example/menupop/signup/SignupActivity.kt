package com.example.menupop.signup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.databinding.ActivitySignupBinding
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.databinding.DialogWarningBinding
import com.example.menupop.login.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    val TAG = "SignupActivity"

    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup)

        signupViewModel = ViewModelProvider(this).get(SignupViewModel::class.java)
        binding.viewModel = signupViewModel
        binding.lifecycleOwner = this

        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text="회원가입"

        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            finish()
        }



        /**
         * 1. edittext의 아이디 입력 감지
         */
        binding.signupIdEdittext.addTextChangedListener {
            Log.d(TAG, "it: ${it.toString()}")
            signupViewModel.onIdTextChanged(it.toString())
        }

        /**
         * 2. 아이디 유효성 검사 관찰
         */
        signupViewModel.isValidId.observe(this, Observer { idValidate ->
            if (idValidate == false) {
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)
            } else {
                binding.signupIdWarning.visibility = View.GONE
            }
        })

        binding.signupPersonalCheckbox.setOnCheckedChangeListener { compoundButton, isCheck ->
            signupViewModel.personalCheckBoxChecked(isCheck)
        }
        binding.signupMarketingCheckbox.setOnCheckedChangeListener { compoundButton, isCheck ->
            signupViewModel.marketingCheckBoxChecked(isCheck)
        }

        /**
         * 3. 아이디 중복검사 버튼을 누르면
         */
        binding.signupIdDuplicationButton.setOnClickListener {
            //중복확인을 안한 상태
            if (binding.signupIdDuplicationButton.text.equals("중복확인")) {

                var id = binding.signupIdEdittext.text.toString().trim()

                if (id.isEmpty()) {
                    binding.signupIdWarning.visibility = View.VISIBLE
                    binding.signupIdWarning.text = "아이디를 입력해주세요."
                    binding.signupIdWarning.setTextColor(Color.RED)

                } else {
                    lifecycleScope.launch {
                        //유효성 검사가 완료되었다면 중복검사 시행
                        if (signupViewModel.isValidId.value == true) {
                            signupViewModel.checkUserIdDuplication(id)
                        }
                    }
                }
            } else {
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

            if (isDuplicate) {
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)

            } else {
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
            val confirmPassword = it.toString()
            signupViewModel.onConfirmPasswordTextChanged(password, confirmPassword)
        }

        /**
         * 2. 비밀번호 유효성 관찰
         */
        signupViewModel.isValidPassword.observe(this) { valid ->
            if (valid) {
                binding.signupPasswordWarning.visibility = View.GONE
            } else {
                binding.signupPasswordWarning.visibility = View.VISIBLE

            }
        }
        /**
         * 3. 비밀번호, 비밀번호 확인 일치 관찰
         */
        signupViewModel.isValidPasswordConfirm.observe(this) { valid ->
            if (valid) {
                binding.signupPasswordConfirmWarning.visibility = View.GONE
            } else {
                binding.signupPasswordConfirmWarning.visibility = View.VISIBLE
            }
        }


        /**
         * 1. 이메일 입력 감지
         */
        binding.signupEmailWarning.visibility = View.GONE

        binding.signupEmailIdEdittext.addTextChangedListener {
            val domainSelection = binding.signupEmailSelection.selectedItem.toString()
            val emailId = binding.signupEmailIdEdittext.text.toString()
            signupViewModel.checkEmailForm(emailId, domainSelection)

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
                    val domainSelection = parent?.getItemAtPosition(position).toString()
                    val emailId = binding.signupEmailIdEdittext.text.toString()

                    if(domainSelection != "선택"){
                        signupViewModel.checkEmailForm(emailId, domainSelection)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        /**
         * 3. 이메일 유효성 관찰
         */
        signupViewModel.checkEmailForm.observe(this, Observer {valid->

            if (valid) {
                binding.signupEmailWarning.visibility = View.GONE

            } else {
                binding.signupEmailWarning.setTextColor(Color.RED)
                binding.signupEmailWarning.visibility = View.VISIBLE
            }
        })

        binding.signupCheckProvideInformation.setOnClickListener {
            showInformationDialog("personal")
        }
        binding.signupCheckMarketingInformation.setOnClickListener {
            showInformationDialog("marketing")
        }


        /**
         * 이메일을 명확히 입력하면 인증번호 버튼이 활성화
         */
        binding.signupCertificationButton.setOnClickListener {
            if (binding.signupCertificationButton.text.equals("인증번호") ||
                binding.signupCertificationButton.text.equals("재인증")
            ) {

                val emailId = binding.signupEmailIdEdittext.text.toString().trim()
                val domainSelection = binding.signupEmailSelection.selectedItem.toString()
                val fullEmail = "${emailId}@${domainSelection}"

                lifecycleScope.launch {
                    signupViewModel.checkEmailExistence(fullEmail)

                }

            } else if (binding.signupCertificationButton.text.equals("확인")) {
                val authCode = binding.signupCertificationEdittext.text.toString().trim()
                Log.d(TAG, "authCode: $authCode")
                signupViewModel.checkVerifyCode(authCode)
            }

        }

        signupViewModel.isEmailExistence.observe(this, Observer {exist ->
            if(!exist){
                binding.signupEmailWarning.visibility = View.GONE

                val emailId = binding.signupEmailIdEdittext.text.toString().trim()
                val domainSelection = binding.signupEmailSelection.selectedItem.toString()
                val fullEmail = "${emailId}@${domainSelection}"

                signupViewModel.requestEmailAuth(fullEmail)

                binding.signupCertificationButton.text = "확인"
                signupViewModel.startTimer() //타이머 시작
                binding.signupCertificationWarning.visibility = View.VISIBLE

            }else{
                binding.signupEmailWarning.visibility = View.VISIBLE
                binding.signupEmailWarning.setTextColor(Color.RED)
            }
        })

        signupViewModel.isTimeExpired.observe(this, Observer {

            if (it) {
                Log.d(TAG, "timer 종료")
                binding.signupCertificationButton.text = "재인증"
                binding.signupSubmitButton.isEnabled = false
            }
        })


        signupViewModel.verifyCompleted.observe(this, Observer { result ->
            if (result) {
                signupViewModel.stopTimer()
                binding.signupCertificationEdittext.isEnabled = false
                binding.signupCertificationButton.isEnabled = false
                binding.signupCertificationWarning.setTextColor(Color.BLUE)

            }else{
                showCustomDialog("인증번호 불일치", "인증번호가 일치하지 않습니다. \n다시 입력해주세요.", false)
            }
        })

        // 가입 버튼 리스너 설정
        binding.signupSubmitButton.setOnClickListener {

            if(signupViewModel.checkUserInformation()){
                Log.d(TAG, "checkUserInfo true")

                val id = binding.signupIdEdittext.text.toString().trim()
                val password = binding.signupPasswordEdittext.text.toString().trim().hashCode().toString()
                Log.d(TAG, "password: $password")
                val email = "${binding.signupEmailIdEdittext.text.toString().trim()}@${binding.signupEmailSelection.selectedItem}"
                val identifier = id.hashCode()
                if(signupViewModel.checkBoxChecked()){
                    signupViewModel.sendUserInformation(id, password, email, identifier)
                }else{
                    Toast.makeText(this, "약관에 동의 해주세요.",Toast.LENGTH_LONG).show()
                }

            }else{
                Log.d(TAG, "checkUserInfo false")

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
            dialogDismiss(dialog, success)
        }

        dialog.show()
    }
    fun showInformationDialog(type : String){
        val bindingDialog : DialogWarningBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_warning, null, false);
        val dialog = Dialog(this)
        dialog.setContentView(bindingDialog.root)
        if(type == "personal"){
            bindingDialog.dialogTitle.text = "개인 정보 이용 약관"
            bindingDialog.dialogContent.text =  "개인 정보 이용약관이 들어갈 예정"
        }else{
            bindingDialog.dialogTitle.text = "마케팅 수신 이용 약관"
            bindingDialog.dialogContent.text = "마케팅 수신 이용 약관이 들어갈 예정"

        }
        dialog.show()

    }
    fun dialogDismiss(dialog: Dialog, close : Boolean){
        if(close){
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                dialog.dismiss()
                var intent = Intent(this, LoginActivity :: class.java)
                startActivity(intent)
            }, 3000) // 3초 후에 다이얼로그를 닫음


        }
    }
}









