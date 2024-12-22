package com.example.menupop.mainActivity.exchange

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    companion object {
        const val TAG = "ExchangeFragmentTAG"
    }

    private var _binding: FragmentExchangeBinding? = null
    private val binding get() = _binding!!
    private lateinit var exchangeViewModel: ExchangeViewModel
    lateinit var currencyUnits: Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exchangeViewModel = ViewModelProvider(this)[ExchangeViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exchange, container, false)
        binding.exchangeRateViewModel = exchangeViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currencyUnits = resources.getStringArray(R.array.currencies)

        exchangeViewModel.init()

        exchangeViewModel.exchangeData.observe(viewLifecycleOwner) {
            if (binding.exchangeTargetSpinner.selectedItem.toString() != "선택") {
                Log.d(TAG, "exchangeData: ${it.baseCode}")
                exchangeViewModel.exchange(
                    binding.exchangeSourceEdittext.text.toString(),
                    binding.exchangeTargetSpinner.selectedItem.toString()
                )
            }
        }

        setListeners()
    }

    private fun setViewStatus(boolean: Boolean) {
        binding.exchangeSourceEdittext.isFocusable = boolean
        binding.exchangeSourceEdittext.isFocusableInTouchMode = boolean
        binding.exchangeTargetSpinner.isFocusableInTouchMode = boolean
    }

    private fun setListeners() {

        //기준 화폐 선택
        binding.exchangeSourceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val baseSpinner = parent?.getItemAtPosition(position).toString()
                    val apiKey = BuildConfig.BANK_API_KEY
                    if (position != 0) {
                        setViewStatus(true)

                        val unit = currencyUnits[position]
                        exchangeViewModel.setSourceUnit(unit)
                        lifecycleScope.launch {
                            exchangeViewModel.requestExchangeRate(apiKey, baseSpinner)
                        }
                    } else {
                        //기준 국가 미선택 시 입력 불가
                        setViewStatus(false)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무 항목도 선택되지 않았을 때
                }
            }

        binding.exchangeSourceEdittext.setOnClickListener {
            if (!binding.exchangeSourceEdittext.isFocusable && !binding.exchangeSourceEdittext.isFocusableInTouchMode) {
                binding.exchangeSourceSpinner.performClick()
            }
        }

        // 기준 화폐 입력
        binding.exchangeSourceEdittext.addTextChangedListener { text ->
            Log.d(TAG, "setListeners: $text")
            exchangeViewModel.updateFormattedNumber(text.toString())

            binding.exchangeSourceEdittext.clearFocus()
            binding.exchangeSourceEdittext.requestFocus()
            binding.exchangeSourceEdittext.setSelection(binding.exchangeSourceEdittext.text.length)

            if (binding.exchangeSourceSpinner.selectedItemPosition != 0
                && binding.exchangeTargetSpinner.selectedItemPosition != 0
            ) {
                exchangeViewModel.exchange(
                    text.toString(),
                    binding.exchangeTargetSpinner.selectedItem.toString()
                )
            }
        }

        binding.exchangeTargetSpinner.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if(!binding.exchangeTargetSpinner.isFocusableInTouchMode){
                    Toast.makeText(requireContext(), "기준 화페 국가를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                }else{
                    view.performClick()
                }
            }
           true
        }

        // 환전 대상 화폐 선택
        binding.exchangeTargetSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val targetSpinner = parent?.getItemAtPosition(position).toString()
                    val amount = binding.exchangeSourceEdittext.text.toString()

                    if (position != 0) {
                        val unit = currencyUnits[position]
                        exchangeViewModel.setTargetUnit(unit)
                        exchangeViewModel.exchange(amount, targetSpinner)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무 항목도 선택되지 않았을 때
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}