package com.example.menupop.findId

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentFindIdResultBinding

class FindIdResultFragment : Fragment() {
    companion object{
        const val TAG = "FindIdResultFragment"
    }

    private var _binding : FragmentFindIdResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var findIdViewModel: FindIdViewModel
    private lateinit var context : Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        findIdViewModel = ViewModelProvider(requireActivity())[FindIdViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_id_result, container, false)
        binding.findIdViewModel = findIdViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListener()
    }

    fun init(){

        findIdViewModel.userIdExistence.observe(viewLifecycleOwner){ result ->

            if(result.result == "exist"){
                binding.findIdResultInformation.text = "로그인 후 사용이 가능합니다."
            }else{
                binding.findIdResultTextBottom.visibility = View.GONE
                binding.findIdResultInformation.visibility = View.GONE
            }
        }
    }

    private fun setListener() {
        binding.findIdResultButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}