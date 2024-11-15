package com.example.menupop.resetPassword

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordEmailBinding
import kotlinx.coroutines.launch


class ResetPasswordEmailFragment : Fragment() {
    companion object{
        const val TAG = "ResetPasswordEmailFragment"
    }
    private var _binding : FragmentResetPasswordEmailBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resetPasswordViewModel = ViewModelProvider(requireActivity())[ResetPasswordViewModel::class.java]
        _binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_email, container, false)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "비밀번호 재설정"

        setObserver()

        setListener()

    }

    private fun setObserver() {

        resetPasswordViewModel.checkEmailForm.observe(viewLifecycleOwner) { result ->
            when(result){
                true -> binding.emailWarningText.visibility = View.GONE
                else -> binding.emailWarningText.visibility = View.VISIBLE
            }
        }

        resetPasswordViewModel.remainingTime.observe(viewLifecycleOwner) {time ->
            binding.passwordResetEmailCertificationWarningText.text = "시간 제한 : $time"

            if(time == "00:00"){
                binding.certificationButton.text = "재인증"
                binding.passwordResetEmailCertificationWarningText.text = "인증번호가 만료되었습니다."
            }

        }

        resetPasswordViewModel.verifycationCompleted.observe(viewLifecycleOwner){ result ->
            when(result){
                true -> {
                    resetPasswordViewModel.stopTimer()
                    Toast.makeText(context,"인증 완료.",Toast.LENGTH_SHORT).show()
                    binding.passwordResetEmailCertificationWarningText.visibility = View.GONE
                    binding.lastButton.setOnClickListener{
                        event?.successVerifyEmail()
                    }
                }
                else -> Toast.makeText(context,"인증번호를 확인해주세요.",Toast.LENGTH_SHORT).show()
            }
        }

        resetPasswordViewModel.verifiedEmail.observe(viewLifecycleOwner) { result ->
            val email = "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem}"

            when(result){
                false -> {
                    binding.emailWarningText.text = "사용자가 입력한 아아디와 일치하지 않은 이메일입니다."
                    binding.emailWarningText.visibility = View.VISIBLE
                }
                else -> {
                    binding.emailWarningText.visibility = View.GONE

                    lifecycleScope.launch {
                        resetPasswordViewModel.sendVerifyCode(email)
                    }
                    resetPasswordViewModel.startTimer()
                    binding.certificationButton.text = "확인"
                    binding.passwordResetEmailCertificationWarningText.visibility = View.VISIBLE
                }
            }
        }

    }
    private fun setListener(){
        binding.passwordResetEmailId.addTextChangedListener{

            val selectedItem = binding.passwordResetEmailSelection.selectedItem.toString()
            val emailFirst = binding.passwordResetEmailId.text.toString()

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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }

        }
        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            event?.backBtnClick()
        }

        binding.certificationButton.setOnClickListener {
            when(resetPasswordViewModel.checkEmailForm.value){
                false -> Toast.makeText(context,"이메일을 제대로 입력해주세요!",Toast.LENGTH_SHORT).show()

                else -> {
                    if (binding.certificationButton.text == "인증번호" || binding.certificationButton.text == "재인증") {
                        val email =
                            "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem}"
                        resetPasswordViewModel.checkEmail(email)
                    } else {
                        val verifyCode = binding.certificationNumber.text.toString()
                        resetPasswordViewModel.checkVerifyCode(verifyCode)
                    }
                }
            }

        }
    }
}