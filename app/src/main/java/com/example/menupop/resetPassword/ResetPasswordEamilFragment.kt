package com.example.menupop.resetPassword

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordCheckIdBinding
import com.example.menupop.databinding.FragmentResetPasswordEamilBinding


class ResetPasswordEamilFragment : Fragment() {
    private var TAG = "ResetPasswordEamilFragment"
    lateinit var binding : FragmentResetPasswordEamilBinding
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private var event: ResetPasswordFragmentEvent? = null
    private var context : Context?=null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if (context is ResetPasswordFragmentEvent) {
            event = context
            Log.d(TAG, "onAttach: 호출됨")
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement ResetPasswordFragmentEvent"
            )
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_eamil, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()

    }

    fun init() {
        resetPasswordViewModel = ViewModelProvider(requireActivity()).get(ResetPasswordViewModel::class.java)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        resetPasswordViewModel.checkEamilForm.observe(viewLifecycleOwner, Observer { result ->
            Log.d(TAG, "이메일 형식 확인 ${result}")
            if(result){
                binding.emailWarningText.visibility = View.GONE
            } else{
                binding.emailWarningText.visibility = View.VISIBLE
            }

        })

        resetPasswordViewModel.remainingTime.observe(viewLifecycleOwner, Observer {time ->
            Log.d(TAG, "init: ${time}")
            binding.passwordResetEmailCertificationWarningText.text = "시간 제한 : ${time}"
            if(time == "00:00"){
                Log.d(TAG, "init: 종료 됨")
                binding.certificationButton.text = "재인증"
                binding.passwordResetEmailCertificationWarningText.text = "인증번호가 만료되었습니다."
            }

        })
        resetPasswordViewModel.verifycationCompleted.observe(viewLifecycleOwner, Observer { result ->
            if(result){
                resetPasswordViewModel.stopTimer()
                Toast.makeText(context,"인증 완료.",Toast.LENGTH_SHORT).show()
                binding.passwordResetEmailCertificationWarningText.visibility = View.GONE
                binding.lastButton.setOnClickListener{
                    event?.successVerifyEmail()
                }
            } else{
                Toast.makeText(context,"인증번호를 확인해주세요.",Toast.LENGTH_SHORT).show()
            }
        })

        resetPasswordViewModel.verifiedEmail.observe(viewLifecycleOwner, Observer { result ->
            Log.d(TAG, "verifycationCompleted 변경 확인")
            val email = "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem.toString()}"
            if(result == false){
                binding.emailWarningText.text = "사용자가 입력한 아아디와 일치하지 않은 이메일입니다."
                binding.emailWarningText.visibility = View.VISIBLE
            } else {
                binding.emailWarningText.visibility = View.GONE
                resetPasswordViewModel.sendVerifyCode(email)
                resetPasswordViewModel.startTimer()
                binding.certificationButton.text = "확인"
                binding.passwordResetEmailCertificationWarningText.visibility = View.VISIBLE
            }

        })

    }
    fun setListener(){
        binding.passwordResetEmailId.addTextChangedListener{
            val selectedItem = binding.passwordResetEmailSelection.selectedItem.toString()
            val emailFirst = binding.passwordResetEmailId.text.toString()
            Log.d(TAG, "이메일 형식 ${selectedItem} @ ${emailFirst}")
            resetPasswordViewModel.checkEmailForm(emailFirst,selectedItem)

        }
        binding.passwordResetEmailSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                val emailFirst = binding.passwordResetEmailId.text.toString()
                if(selectedItem != "선택"){
                    resetPasswordViewModel.checkEmailForm(emailFirst,selectedItem)
                }
                Log.d(TAG, "onItemSelected 이메일 형식 ${selectedItem} @ ${emailFirst}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }

        }

        binding.certificationButton.setOnClickListener {
            if(resetPasswordViewModel.checkEamilForm.value == false){
                Toast.makeText(context,"이메일을 제대로 입력해주세요!",Toast.LENGTH_SHORT).show()
            }else {
                if (binding.certificationButton.text == "인증번호" || binding.certificationButton.text == "재인증") {
                    val email =
                        "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem.toString()}"
                    resetPasswordViewModel.checkEmail(email)
                } else {
                    val verifyCode = binding.certificationNumber.text.toString()
                    resetPasswordViewModel.checkVerifyCode(verifyCode)
                }
            }
        }
    }
}