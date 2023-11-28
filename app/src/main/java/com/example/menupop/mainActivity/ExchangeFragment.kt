package com.example.menupop.mainActivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentExchangeBinding
import com.example.menupop.databinding.FragmentResetPasswordEamilBinding
import com.example.menupop.login.LoginViewModel
import com.example.menupop.resetPassword.ResetPasswordFragmentEvent
import com.example.menupop.resetPassword.ResetPasswordViewModel


class ExchangeFragment : Fragment() {
    private var TAG = "ExchangeFragment"
    lateinit var binding : FragmentExchangeBinding
    private lateinit var exchangeViewModel : ExchangeViewModel
    private lateinit var context : Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()

    }
    fun init() {
        exchangeViewModel = ViewModelProvider(this).get(ExchangeViewModel::class.java)
        binding.exchangeRateViewModel = exchangeViewModel
        binding.lifecycleOwner = this

        binding.exchangeRateApplicationStatus.isChecked = getStatus()

        exchangeViewModel.isPossible.observe(viewLifecycleOwner, Observer { result ->
            Log.d(TAG, "init: ${result}")
            binding.exchangeSourceEdittext.isFocusable = result
        })
        exchangeViewModel.init()

    }
    fun setListener(){
        binding.exchangeSourceEdittext.addTextChangedListener {text ->
            exchangeViewModel.updateFormattedNumber(text.toString())
            binding.exchangeSourceEdittext.clearFocus()
            binding.exchangeSourceEdittext.requestFocus()
            binding.exchangeSourceEdittext.setSelection( binding.exchangeSourceEdittext.text.length)
            Log.d(TAG, "setListener: ${binding.exchangeSourceSpinner.selectedItem.toString() != "선택" && binding.exchangeTargetSpinner.selectedItem.toString() != "선택"}")
            if(binding.exchangeSourceSpinner.selectedItem.toString() != "선택" && binding.exchangeTargetSpinner.selectedItem.toString() != "선택"){
                exchangeViewModel.exchange(text.toString(),binding.exchangeTargetSpinner.selectedItem.toString(),binding.exchangeStandardEdittext.text.toString())
            }
        }
        binding.exchangeStandardEdittext.addTextChangedListener {text ->
            exchangeViewModel.exchange(binding.exchangeSourceEdittext.text.toString(),binding.exchangeTargetSpinner.selectedItem.toString(),binding.exchangeStandardEdittext.text.toString())
        }
        binding.exchangeRateApplicationStatus.setOnCheckedChangeListener {_, status ->
            Log.d(TAG, "setListener: ${status}")
            val sharedPreferences = context.getSharedPreferences("util", AppCompatActivity.MODE_PRIVATE)
            exchangeViewModel.exchangeRateApplicationStatus(sharedPreferences,status)
        }
        binding.exchangeTargetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val targetSpinner = parent?.getItemAtPosition(position).toString()
                val amount = binding.exchangeSourceEdittext.text.toString()
                exchangeViewModel.exchange(amount,targetSpinner,binding.exchangeStandardEdittext.text.toString())

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않았을 때의 처리 로직을 작성합니다.
            }
        }
        binding.exchangeSourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val baseSpinner = parent?.getItemAtPosition(position).toString()
                if(baseSpinner != "선택") {
                    Log.d(TAG, "onItemSelected: 요청 보냄")
                    exchangeViewModel.requestExchangeRate(
                        getString(R.string.BANK_API_KEY),
                        baseSpinner
                    )
                    binding.exchangeStandardTextview.text = baseSpinner
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않았을 때의 처리 로직을 작성합니다.
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_exchange, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
    fun getStatus() : Boolean{
        val sharedPreferences = context.getSharedPreferences("util", AppCompatActivity.MODE_PRIVATE)
        val status = sharedPreferences.getBoolean("exchangeRateApplicationStatus",false)
        return status
    }


}