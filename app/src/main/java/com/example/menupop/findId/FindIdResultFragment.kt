package com.example.menupop.findId

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentFindIdResultBinding

class FindIdResultFragment : Fragment() {
    val TAG = "FindIdResultFragment"
    lateinit var binding : FragmentFindIdResultBinding
    private lateinit var findIdViewModel: FindIdViewModel
    var event: FindIdFragmentEvent? = null
    private lateinit var context : Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if(context is FindIdFragmentEvent){
            event = context
            Log.d(TAG, "onAttach: 호출")
        }else{
            throw RuntimeException(
                context.toString()
                    +"must implement FindIdFragmentEvent"
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_id_result, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListener()
    }

    fun init(){
        findIdViewModel = ViewModelProvider(requireActivity()).get(FindIdViewModel::class.java)
        binding.findIdViewModel = findIdViewModel
        binding.lifecycleOwner = this


        findIdViewModel.userIdExistence.observe(viewLifecycleOwner, Observer{ result ->
            Log.d(TAG, "findId Result: ${result.result} ${result.id}")

            if(result.result == "exist"){
                var id = maskLastTwoChars(result.id)
                binding.findIdResultId.text = id
                binding.findIdResultInformation.text = "로그인 후 사용이 가능합니다."
            }else{
                binding.findIdResultId.text= "존재하지 않습니다."
                binding.findIdResultTextBottom.visibility = View.GONE
                binding.findIdResultInformation.visibility = View.GONE
            }

        })
    }

    fun maskLastTwoChars(inputString: String): String {
        if (inputString.length >= 2) {
            val maskedPart = inputString.substring(0, inputString.length - 2) + "**"
            return maskedPart
        }
        return inputString
    }

    fun setListener(){
        binding.findIdResultButton.setOnClickListener{
            event?.finishFindId()
        }
    }


}