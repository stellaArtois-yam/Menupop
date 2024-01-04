package com.example.menupop.mainActivity.exchange

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.FragmentExchangeBinding


class ExchangeFragment : Fragment() {
    private var TAG = "ExchangeFragmentTAG"
    lateinit var binding : FragmentExchangeBinding
    private lateinit var exchangeViewModel : ExchangeViewModel
    private lateinit var context : Context
    lateinit var currencyUnits : Array<String>

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

        Log.d(TAG, "array 정보: ${resources.getStringArray(R.array.currencies)}")

    }
    fun init() {
        exchangeViewModel = ViewModelProvider(this).get(ExchangeViewModel::class.java)
        binding.exchangeRateViewModel = exchangeViewModel
        binding.lifecycleOwner = this

        currencyUnits = resources.getStringArray(R.array.currencies)

        binding.exchangeRateApplicationStatus.isChecked = getStatus()

        exchangeViewModel.isPossible.observe(viewLifecycleOwner, Observer { result ->
            Log.d(TAG, "init: ${result}")
            binding.exchangeSourceEdittext.isFocusable = result
        })
        exchangeViewModel.init()

    }
    fun setListener(){
        /**
         * 보유 화폐 입력 시
         */
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
         * 메뉴판에 환율 적용 버튼
         */
        binding.exchangeRateApplicationStatus.setOnCheckedChangeListener {_, status ->
            if(exchangeViewModel.checkedSetCurrency()){
                Log.d(TAG, "환율 적용 버튼: ${status}")

                val sharedPreferences = context.getSharedPreferences("util", AppCompatActivity.MODE_PRIVATE)
                exchangeViewModel.exchangeRateApplicationStatus(sharedPreferences,status)

            }else{
                Toast.makeText(requireContext(), "기준 화폐와 환전 화폐를 선택해주세요.", Toast.LENGTH_LONG).show()
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
                    val unit = currencyUnits.get(position)
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
                    exchangeViewModel.requestExchangeRate(
                        apiKey,
                        baseSpinner
                    )
                    val unit = currencyUnits.get(position)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_exchange, container, false)

        return binding.root
    }


    /**
     * 이거 Model에서 가져와야 되는거 아닌가?
     */
    fun getStatus() : Boolean{
        val sharedPreferences = context.getSharedPreferences("util", AppCompatActivity.MODE_PRIVATE)
        val status = sharedPreferences.getBoolean("exchangeRateApplicationStatus",false)
        return status
    }


}