package com.example.menupop.resetPassword

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordCheckIdBinding
import kotlinx.coroutines.launch

class ResetPasswordCheckIdFragment : Fragment() {
    private var TAG = "ResetPasswordCheckIdFragment"
    lateinit var binding : FragmentResetPasswordCheckIdBinding
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private var event: ResetPasswordFragmentEvent? = null
    private var context : Context ?=null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if (context is ResetPasswordFragmentEvent) {
            event = context
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
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_check_id, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()

    }
    fun init() {
        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "아이디 확인"
        resetPasswordViewModel = ViewModelProvider(requireActivity()).get(ResetPasswordViewModel::class.java)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this
        
        resetPasswordViewModel.checkIdResult.observe(viewLifecycleOwner) { result ->
            when(result){
                true ->  event?.existId()
                else -> binding.passwordResetCheckIdWarning.visibility = View.VISIBLE
            }
        }
    }

    private fun setListener(){
        binding.passwordResetIdCheckButton.setOnClickListener {
            var id = binding.passwordResetIdCheckEdittext.text.toString().trim()

            when(id.isEmpty()){
                true -> Toast.makeText(context,"아이디를 입력해주세요.",Toast.LENGTH_SHORT).show()
                else ->  {
                    lifecycleScope.launch{
                        resetPasswordViewModel.checkId(id)
                    }
                }
            }
        }

        binding.passwordResetBottomBackButton.setOnClickListener {
            activity?.finish()
        }

        binding.passwordResetIdCheckEdittext.addTextChangedListener{
            binding.passwordResetCheckIdWarning.visibility = View.GONE
        }

        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            event?.backBtnClick()
        }
    }
}