package com.example.menupop.resetPassword

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordConfirmBinding
import kotlinx.coroutines.launch

class ResetPasswordConfirmFragment : Fragment() {
    private var _binding : FragmentResetPasswordConfirmBinding? = null
    private val binding get() = _binding!!
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private var progressbar : Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resetPasswordViewModel =
            ViewModelProvider(requireActivity())[ResetPasswordViewModel::class.java]
        _binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_confirm, container, false)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setObservers()
        setListener()
    }

    private fun setObservers() {

        resetPasswordViewModel.isResetPassword.observe(viewLifecycleOwner){ result ->
            progressbar!!.dismiss()
            if(result){
                Toast.makeText(requireContext(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            } else{
                Toast.makeText(requireContext(),"잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setListener(){
        binding.passwordResetEdittext.addTextChangedListener{
            resetPasswordViewModel.onPasswordTextChanged(it.toString())
        }

        //비밀번호 확인 입력감지
        binding.passwordResetConfirmEdittext.addTextChangedListener{
            val password = binding.passwordResetEdittext.text.toString()
            val confirmPassword = it.toString()
            resetPasswordViewModel.onConfirmPasswordTextChanged(password, confirmPassword)
        }

        binding.passwordResetConfirmButton.setOnClickListener {
            if(resetPasswordViewModel.lastCheck){
                val password = binding.passwordResetEdittext.text.toString().trim()
                lifecycleScope.launch {
                    resetPasswordViewModel.resetPassword(password)
                    progressbar = loadDialog()
                    progressbar!!.show()
                }

            }else if(resetPasswordViewModel.isPasswordFormMatched.value != true)
                Toast.makeText(requireContext(), "비밀번호 형식이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()

            else if(resetPasswordViewModel.isConfirmPasswordMatched.value == false){
                Toast.makeText(requireContext(), R.string.password_confirm_warning, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.progress_text).visibility = View.GONE
        dialog.findViewById<ImageView>(R.id.progress_image).visibility = View.GONE

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        progressbar = null
    }
}