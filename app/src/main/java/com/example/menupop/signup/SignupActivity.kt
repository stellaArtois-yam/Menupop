package com.example.menupop.signup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.databinding.ActivitySignupBinding
import com.example.menupop.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    companion object{
        const val TAG = "SignupActivityTAG"
    }

    private lateinit var signupViewModel: SignupViewModel
    private var _binding : ActivitySignupBinding? = null
    private val binding get() = _binding!!
    private var progressbar : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signupViewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        binding.viewModel = signupViewModel
        binding.lifecycleOwner = this

        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text="회원가입"

        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            finish()
        }
        setObservers()
        setListeners()
    }

    private fun setIdViewsStatus(boolean: Boolean){
        binding.signupIdEdittext.isEnabled = boolean
        binding.signupIdEdittext.isClickable = boolean
        if(boolean){
            binding.signupIdDuplicationButton.text = "중복확인"
        }else{
            binding.signupIdDuplicationButton.text = "재입력"
        }
    }

    private fun setEmailViewsStatus(boolean: Boolean){
        binding.signupEmailIdEdittext.isEnabled = boolean
        binding.signupEmailSelection.isEnabled = boolean
    }

    private fun setObservers(){
        //아이디 유효성 검사
        signupViewModel.isValidId.observe(this){ idValidate ->

            if (idValidate == null || idValidate) {
                binding.signupIdWarning.visibility = View.GONE
            } else{
                binding.signupIdWarning.visibility = View.VISIBLE
                binding.signupIdWarning.setTextColor(Color.RED)
            }
        }

        //아이디 중복 여부
        signupViewModel.isIdDuplication.observe(this) { isDuplicate ->

            when (isDuplicate) {
                true -> {
                    binding.signupIdWarning.visibility = View.VISIBLE
                    binding.signupIdWarning.setTextColor(Color.RED)

                }
                false -> {
                    binding.signupIdWarning.visibility = View.VISIBLE
                    binding.signupIdWarning.setTextColor(Color.BLUE)
                    setIdViewsStatus(false)
                }
                else -> {
                    binding.signupIdWarning.visibility = View.GONE
                }
            }
        }

        // 이메일 중복 여부
        signupViewModel.isEmailExistence.observe(this) {exist ->
            if(!exist){
                binding.signupCertificationEdittext.isEnabled = true // 인증 번호 입력란 사용 가능
                binding.signupEmailWarning.visibility = View.GONE // 이메일 경고창
                setEmailViewsStatus(false)

                val emailId = binding.signupEmailIdEdittext.text.toString().trim()
                val domainSelection = binding.signupEmailSelection.selectedItem.toString()
                val fullEmail = "${emailId}@${domainSelection}"

                signupViewModel.requestEmailAuth(fullEmail) //이메일 인증

                binding.signupCertificationButton.text = "확인"
                signupViewModel.startTimer() //타이머 시작
                binding.signupCertificationTimer.visibility = View.VISIBLE //시간초

            }else{
                binding.signupEmailWarning.visibility = View.VISIBLE // 가입된 아이디 존재 문구창
                binding.signupEmailWarning.setTextColor(Color.RED)
            }
        }

        //인증 번호 만료
        signupViewModel.isTimeExpired.observe(this) {
            if (it) {
                binding.signupCertificationButton.text = "재인증"
                setEmailViewsStatus(true)
            }
        }

        signupViewModel.verifyCompleted.observe(this) { result ->
            if (result) {
                signupViewModel.stopTimer()
                binding.signupCertificationEdittext.isEnabled = false // 인증 번호 입력 불가
                binding.signupCertificationButton.isEnabled = false // 인증 번호 버튼 사용 불가
                binding.signupCertificationTimer.setTextColor(Color.BLUE)

            }else{
                customDialog("인증번호 불일치", "인증번호가 일치하지 않습니다. \n다시 입력해주세요.",
                    isProgress = false,
                    isCompletedSignup = false
                ).show()
            }
        }

        signupViewModel.saveResult.observe(this) {result->
            progressbar!!.dismiss()
            when(result){
                true -> customDialog("회원가입 완료", "반갑습니다 :) \n로그인 후 이용해주세요",
                    isProgress = false,
                    isCompletedSignup = true
                ).show()
                else -> customDialog("회원가입 실패", resources.getString(R.string.network_error),
                    isProgress = false,
                    isCompletedSignup = true
                ).show()
            } }
    }

    private fun setListeners(){
        binding.signupIdEdittext.addTextChangedListener {
            signupViewModel.onIdTextChanged(it.toString())
        }

        binding.signupPersonalCheckbox.setOnCheckedChangeListener { _, isCheck ->
            signupViewModel.personalCheckBoxChecked(isCheck)
        }
        binding.signupMarketingCheckbox.setOnCheckedChangeListener { _, isCheck ->
            signupViewModel.marketingCheckBoxChecked(isCheck)
        }

        //아이디 중복검사
        binding.signupIdDuplicationButton.setOnClickListener {
            //중복확인을 안한 상태
            if (binding.signupIdDuplicationButton.text.equals("중복확인")) {

                val id = binding.signupIdEdittext.text.toString().trim()

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
                setIdViewsStatus(true)
                signupViewModel.setIsIdDuplication(null)
            }
        }

        binding.signupPasswordEdittext.addTextChangedListener {
            signupViewModel.onPasswordTextChanged(it.toString())
        }


        binding.signupPasswordConfirmEdittext.addTextChangedListener {
            val password = binding.signupPasswordEdittext.text.toString()
            val confirmPassword = it.toString()

            if(password.isNotEmpty() && confirmPassword.isNotEmpty()){
                signupViewModel.onConfirmPasswordTextChanged(password, confirmPassword)
            }

        }


        binding.signupEmailIdEdittext.addTextChangedListener {
            val domainSelection = binding.signupEmailSelection.selectedItem.toString()
            val emailId = binding.signupEmailIdEdittext.text.toString()
            signupViewModel.checkEmailForm(emailId, domainSelection)
        }

        // 이메일 도메인
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

                    if(position != 0){
                        signupViewModel.checkEmailForm(emailId, domainSelection)

                    }else if(emailId.isNotEmpty()){
                        signupViewModel.setCheckEmailForm(false)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.signupCheckProvideInformation.setOnClickListener {
            informationDialog("personal")
        }
        binding.signupCheckMarketingInformation.setOnClickListener {
            informationDialog("marketing")
        }


        // 인증 번호 전송 버튼
        binding.signupCertificationButton.setOnClickListener {
            if (binding.signupCertificationButton.text == "인증번호" || binding.signupCertificationButton.text == "재인증") {

                val emailId = binding.signupEmailIdEdittext.text.toString().trim()
                val domainSelection = binding.signupEmailSelection.selectedItem.toString()
                val fullEmail = "${emailId}@${domainSelection}"

                lifecycleScope.launch {
                    signupViewModel.checkEmailExistence(fullEmail)
                }

            } else if (binding.signupCertificationButton.text == "확인") {
                val authCode = binding.signupCertificationEdittext.text.toString().trim()
                Log.d(TAG, "authCode: $authCode")
                if(authCode.isNotEmpty()){
                    signupViewModel.checkVerifyCode(authCode)
                }else{
                    Toast.makeText(this, "인증번호를 입력해주세요", Toast.LENGTH_LONG).show()
                }

            }else if(signupViewModel.checkEmailForm.value != true){
                Toast.makeText(this, "이메일 형식을 올바르게 입력해주세요", Toast.LENGTH_LONG).show()
            }
        }

        // 가입 버튼
        binding.signupSubmitButton.setOnClickListener {

            if(signupViewModel.checkUserInformation()){

                val id = binding.signupIdEdittext.text.toString().trim()
                val password = binding.signupPasswordEdittext.text.toString().trim()
                val email = "${binding.signupEmailIdEdittext.text.toString().trim()}@${binding.signupEmailSelection.selectedItem}"
                val identifier = id.hashCode()

                if(signupViewModel.checkBoxChecked()){
                    lifecycleScope.launch{
                        signupViewModel.sendUserInformation(id, password, email, identifier)
                        progressbar = customDialog(null, null,
                            isProgress = true,
                            isCompletedSignup = false
                        )
                        progressbar!!.show()
                    }
                }else{
                    Toast.makeText(this, "약관에 동의 해주세요.",Toast.LENGTH_LONG).show()
                }

            }else {
                Toast.makeText(this, resources.getString(R.string.signup_warning),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun customDialog(title : String?, content : String?,
                             isProgress : Boolean, isCompletedSignup : Boolean) : Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if(isProgress){
            dialog.setContentView(R.layout.progressbar)
            dialog.findViewById<TextView>(R.id.progress_text).visibility = View.GONE
            dialog.findViewById<ImageView>(R.id.progress_image).visibility = View.GONE
            dialog.findViewById<ProgressBar>(R.id.progress_bar).visibility = View.VISIBLE
            dialog.setCancelable(false)
        }else{
            dialog.setContentView(R.layout.dialog_warning)
            val titleTextView : TextView = dialog.findViewById(R.id.title)
            val contentTextView : TextView = dialog.findViewById(R.id.content)
            titleTextView.text = title
            contentTextView.text = content

            if(isCompletedSignup){
                lifecycleScope.launch {
                    delay(1000)
                    dialog.dismiss()
                    val intent = Intent(this@SignupActivity, LoginActivity :: class.java)
                    startActivity(intent)
                }
            }

        }
        return dialog
    }

    private fun informationDialog(type : String){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_terms_of_service)
        val height = (resources.displayMetrics.heightPixels * 0.9).toInt()

        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, height)

        val imageview = dialog.findViewById<ImageView>(R.id.dialog_service_image)
        val button = dialog.findViewById<Button>(R.id.dialog_service_button)

        when(type){
            "personal" -> imageview.setImageResource(R.drawable.personal)
            "marketing" ->  imageview.setImageResource(R.drawable.marketing)
        }

        dialog.show()

        button.setOnClickListener{
            dialog.dismiss()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        progressbar = null
    }
}