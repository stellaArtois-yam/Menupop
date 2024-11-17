package com.example.menupop.resetPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.FragmentResetPasswordCheckIdBinding
import kotlinx.coroutines.launch

class ResetPasswordCheckIdFragment : Fragment() {
    private var _binding : FragmentResetPasswordCheckIdBinding? = null
    private val binding get() = _binding!!
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resetPasswordViewModel = ViewModelProvider(requireActivity())[ResetPasswordViewModel::class.java]
        _binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password_check_id, container, false)
        binding.resetPasswordViewModel = resetPasswordViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetPasswordViewModel.checkIdResult.observe(viewLifecycleOwner) { result ->
            if(result == true){
                findNavController().navigate(R.id.resetPasswordEmailFragment)
            }
        }

        setListener()
    }

    private fun setListener(){
        binding.passwordResetIdCheckButton.setOnClickListener {
            val id = binding.passwordResetIdCheckEdittext.text.toString().trim()

            when(id.isEmpty()){
                true -> Toast.makeText(requireContext(),R.string.id_empty,Toast.LENGTH_SHORT).show()
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
            binding.passwordResetCheckIdWarning.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetPasswordViewModel.setCheckIdResult()
        _binding = null
    }
}