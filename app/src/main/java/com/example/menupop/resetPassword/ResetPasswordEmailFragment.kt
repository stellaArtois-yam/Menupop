package com.example.menupop.resetPassword

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordEmailBinding
import kotlinx.coroutines.launch


class ResetPasswordEmailFragment : Fragment() {

    private var _binding: FragmentResetPasswordEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resetPasswordViewModel =
            ViewModelProvider(requireActivity())[ResetPasswordViewModel::class.java]
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reset_password_email,
            container,
            false
        )
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()
        setListener()
    }

    private fun setEmailEditTextStatus(boolean: Boolean) {
        binding.passwordResetEmailId.isEnabled = boolean
        binding.passwordResetEmailSelection.isEnabled = boolean
    }

    private fun setObserver() {

        resetPasswordViewModel.certificateStatus.observe(viewLifecycleOwner) {
            if (it == "확인") {
                setEmailEditTextStatus(false)
            } else {
                setEmailEditTextStatus(true)
            }
        }

        //해당 아이디에 등록된 이메일인지 확인
        resetPasswordViewModel.isEmailMatchId.observe(viewLifecycleOwner) { result ->
            val email =
                "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem}"

            when (result) {
                true -> sendVerifyCode(email)
                false -> binding.emailWarningText.text = resources.getString(R.string.check_id_email_match)
                else -> binding.emailWarningText.text = resources.getString(R.string.email_rule_warning)
            }
        }

        //인증 완료
        resetPasswordViewModel.verifyCompleted.observe(viewLifecycleOwner) { result ->
            when (result) {
                true -> {
                    //타이머 멈춤
                    resetPasswordViewModel.stopTimer()
                    binding.passwordResetEmailCertificationWarningText.setTextColor(Color.BLUE)
                    binding.certificationNumber.isEnabled = false
                }
                //실패 시 인증 번호 확인 메세지
                else -> Toast.makeText(requireContext(), R.string.check_verify_code, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerifyCode(email: String) {
        // 인증 번호 보내기
        lifecycleScope.launch {
            resetPasswordViewModel.sendVerifyCode(email)
        }
        // 타이머 시작
        resetPasswordViewModel.startTimer()
    }

    private fun setListener() {

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        (activity as ResetPasswordActivity).toolbar!!.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.passwordResetEmailId.addTextChangedListener {

            if(resetPasswordViewModel.isEmailMatchId.value == false){
                resetPasswordViewModel.setIsEmailMatchIdInit()
            }

            val selectedItem = binding.passwordResetEmailSelection.selectedItem.toString()
            val emailId = binding.passwordResetEmailId.text.toString()

            resetPasswordViewModel.checkEmailForm(emailId, selectedItem)
        }

        binding.passwordResetEmailSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    val emailFirst = binding.passwordResetEmailId.text.toString()

                    if (selectedItem != "선택") {
                        resetPasswordViewModel.checkEmailForm(emailFirst, selectedItem)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Nothing to do
                }

            }

        //인증 번호 확인 버튼
        binding.certificationButton.setOnClickListener {
            when (resetPasswordViewModel.checkEmailForm.value) {
                false -> Toast.makeText(requireContext(), R.string.email_rule_warning, Toast.LENGTH_SHORT)
                    .show()

                else -> {
                    val email =
                        "${binding.passwordResetEmailId.text}@${binding.passwordResetEmailSelection.selectedItem}"

                    when (resetPasswordViewModel.certificateStatus.value) {
                        "인증번호" -> resetPasswordViewModel.checkEmail(email)
                        "재인증" -> sendVerifyCode(email)
                        "확인" -> {
                            val verifyCode = binding.certificationNumber.text.toString()
                            resetPasswordViewModel.checkVerifyCode(verifyCode)
                        }
                    }
                }
            }
        }

        //재설정 프래그먼트 이동
        binding.nextButton.setOnClickListener {
            if (resetPasswordViewModel.verifyCompleted.value == true) {
                findNavController().navigate(R.id.resetPasswordConfirmFragment)
            } else {
                Toast.makeText(requireContext(), R.string.check_verify, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}