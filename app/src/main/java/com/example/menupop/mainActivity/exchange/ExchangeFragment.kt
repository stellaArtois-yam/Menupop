package com.example.menupop.mainActivity.exchange

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.FragmentExchangeBinding
import kotlinx.coroutines.launch


class ExchangeFragment : Fragment() {
    companion object{
        const val TAG = "ExchangeFragmentTAG"
    }
    private var _binding : FragmentExchangeBinding? = null
    private val binding get() = _binding!!
    private lateinit var exchangeViewModel : ExchangeViewModel
    private lateinit var context : Context
    lateinit var currencyUnits : Array<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exchangeViewModel = ViewModelProvider(this)[ExchangeViewModel::class.java]
        _binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_exchange, container, false)
        binding.exchangeRateViewModel = exchangeViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()

        Log.d(TAG, "array 정보: ${resources.getStringArray(R.array.currencies)}")

    }
    fun init() {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        currencyUnits = resources.getStringArray(R.array.currencies)

//        binding.exchangeRateApplicationStatus.isChecked = getStatus()

        exchangeViewModel.isPossible.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "init: $result")
            binding.exchangeSourceEdittext.isFocusable = result
        }
        exchangeViewModel.init()

    }
    private fun setListener(){
        // 보유 화폐 입력
        binding.exchangeSourceEdittext.addTextChangedListener {text ->

            exchangeViewModel.updateFormattedNumber(text.toString(), "")

            binding.exchangeSourceEdittext.clearFocus()
            binding.exchangeSourceEdittext.requestFocus()
            binding.exchangeSourceEdittext.setSelection( binding.exchangeSourceEdittext.text.length)

            Log.d(TAG, "exchangeSourceEditText: " +
                    "${binding.exchangeSourceSpinner.selectedItem.toString() != "선택" 
                            && binding.exchangeTargetSpinner.selectedItem.toString() != "선택"}")

            if(binding.exchangeSourceSpinner.selectedItem.toString() != "선택"
                && binding.exchangeTargetSpinner.selectedItem.toString() != "선택"){

                exchangeViewModel.exchange(text.toString(),
                    binding.exchangeTargetSpinner.selectedItem.toString())
            }
        }




        /**
         * 환전 화폐 선택
         */
        binding.exchangeTargetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val targetSpinner = parent?.getItemAtPosition(position).toString()
                Log.d(TAG, "환전 화폐 선택: $targetSpinner")
                Log.d(TAG, "position: $position")
                val amount = binding.exchangeSourceEdittext.text.toString()

                exchangeViewModel.exchange(amount, targetSpinner)


                if(position!=0){
                    val unit = currencyUnits[position]
                    Log.d(TAG, "환전 화폐 선택 unit: $unit")

                    exchangeViewModel.setTargetUnit(unit)
                }else{
                    return
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않았을 때의 처리 로직을 작성합니다.
            }
        }

        /**
         * 보유 화폐 선택
         */
        binding.exchangeSourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val baseSpinner = parent?.getItemAtPosition(position).toString()
                val apiKey = BuildConfig.BANK_API_KEY
                if(baseSpinner != "선택") {
                    Log.d(TAG, "onItemSelected: 요청 보냄")
                    lifecycleScope.launch {
                        exchangeViewModel.requestExchangeRate(
                            apiKey,
                            baseSpinner
                        )
                    }
                    val unit = currencyUnits[position]
                    Log.d(TAG, "보유 화폐 선택 unit: $unit")
                     exchangeViewModel.setSourceUnit(unit)

                }else{
                    return
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않았을 때의 처리 로직을 작성합니다.
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}