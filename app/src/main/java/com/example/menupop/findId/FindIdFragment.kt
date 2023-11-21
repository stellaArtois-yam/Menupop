package com.example.menupop.findId

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentFindIdBinding

class FindIdFragment : Fragment() {
    val TAG = "FindIdFragment"
    lateinit var binding: FragmentFindIdBinding
    private lateinit var findIdViewModel: FindIdViewModel
    var event : FindIdFragmentEvent? = null
    private var context : Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if(context is FindIdFragmentEvent){
            event = context
            Log.d(TAG, "onAttach: 호출")
        }else{
            throw RuntimeException(
                context.toString()
                    + "must implement FindIdFragmentEvent"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_id, container, false)
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

        findIdViewModel.checkEmailForm.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "이메일 유효성 검사: $it")

            if(it){
                binding.findIdEmailWarning.visibility = View.GONE
            }else{
                binding.findIdEmailWarning.visibility = View.VISIBLE
                binding.findIdEmailWarning.text = "올바른 이메일 형식이 아닙니다."
                binding.findIdEmailWarning.setTextColor(Color.RED)
            }
        })

    }

    fun setListener(){

        binding.findIdEmail.addTextChangedListener {
            val domainSelection = binding.findIdEmailSelection.selectedItem.toString()
            val emailId = binding.findIdEmail.text.toString()
            Log.d(TAG, "setListener email 형식 : ${emailId}@${domainSelection}")
            findIdViewModel.checkEmailForm(emailId, domainSelection)
        }

        binding.findIdEmailSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected
                        (parent : AdapterView<*>,
                         view : View?,
                         position: Int,
                         id : Long)
            {
                val domainSelection = parent?.getItemAtPosition(position).toString()
                val emailId = binding.findIdEmail.text.toString()

                if(domainSelection != "선택"){
                    findIdViewModel.checkEmailForm(emailId, domainSelection)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }



        binding.findIdComfirmButton.setOnClickListener{
            if(findIdViewModel.checkEmailForm.value == true) {
                val emailId = binding.findIdEmail.text.toString()
                val domainSelection = binding.findIdEmailSelection.selectedItem.toString()
                findIdViewModel.checkUserId(emailId, domainSelection)

                // 통신 후에 넘겨?
                event?.successFindId()

            }
        }


    }
}