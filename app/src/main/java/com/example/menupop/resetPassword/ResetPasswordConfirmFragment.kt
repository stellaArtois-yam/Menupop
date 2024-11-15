package com.example.menupop.resetPassword

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordConfirmBinding


class ResetPasswordConfirmFragment : Fragment() {
    private var TAG = "ResetPasswordConformFragment"
    lateinit var binding : FragmentResetPasswordConfirmBinding
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
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_confirm, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()


    }

    fun init() {
        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "비밀번호 재설정"
        resetPasswordViewModel =
            ViewModelProvider(requireActivity())[ResetPasswordViewModel::class.java]
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this


        resetPasswordViewModel.passwordError.observe(viewLifecycleOwner){error ->
            if(error==null){
                binding.passwordResetWarningText.visibility = View.GONE
            }else{
                binding.passwordResetWarningText.visibility = View.VISIBLE
                binding.passwordResetWarningText.text = error
            }
        }

        resetPasswordViewModel.confirmPasswordError.observe(viewLifecycleOwner){error->
            if(error ==null){
                binding.passwordResetConfirmWarningText.visibility = View.GONE
            }else{
                binding.passwordResetConfirmWarningText.visibility = View.VISIBLE
                binding.passwordResetConfirmWarningText.text = error
            }
        }
        resetPasswordViewModel.conformResetPassword.observe(viewLifecycleOwner){ result ->
            if(result){
                event?.successResetPassword()
            } else{
                Toast.makeText(context,"잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
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
            val password = binding.passwordResetEdittext.text.toString()
            resetPasswordViewModel.resetPassword(password.trim().hashCode().toString())
        }
        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            event?.backBtnClick()
        }
    }

}