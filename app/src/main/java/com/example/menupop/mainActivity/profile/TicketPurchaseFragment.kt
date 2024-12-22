package com.example.menupop.mainActivity.profile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.DialogAvailableRewardBinding
import com.example.menupop.databinding.FragmentTicketPurchaseBinding
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.mainActivity.MainActivityViewModel

class TicketPurchaseFragment : Fragment() {
    companion object{
        const val TAG = "TicketPurchaseFragment"
    }

    private var _binding: FragmentTicketPurchaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_ticket_purchase, container, false)
        binding.ticketPurchaseViewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        clickListener()
    }

    fun init() {
        mainViewModel.paymentReady.observe(viewLifecycleOwner) {
            if (it!!.tid != "N/A") {
                findNavController().navigate(R.id.kakaoPayWebView)
            }
        }

        mainViewModel.isUseRewards.observe(viewLifecycleOwner) {
            if(it == false){
                Toast.makeText(requireContext(), "리워드 사용에 실패하였습니다. 다시 시도해주세요 :(", Toast.LENGTH_LONG)
                    .show()
                findNavController().navigate(R.id.profileFragment)
            }else if(it == true){
                Toast.makeText(requireContext(), "티켓 구매가 완료되었습니다 :)", Toast.LENGTH_LONG)
                    .show()
                findNavController().navigate(R.id.profileFragment)
                mainViewModel.initializeIsUseRewards()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun clickListener() {

        (activity as MainActivity).binding.toolbar.setNavigationOnClickListener {
            mainViewModel.initializeKakaoPayVariables()
            findNavController().navigate(R.id.profileFragment)  // 뒤로 가기
        }
        binding.translationTicketPlusButton.setOnClickListener{
            mainViewModel.adjustTicketQuantity("translation", "+")
        }

        binding.translationTicketMinusButton.setOnClickListener{
            mainViewModel.adjustTicketQuantity("translation", "-")
        }

        binding.foodTicketPlusButton.setOnClickListener{
            mainViewModel.adjustTicketQuantity("food", "+")
        }

        binding.foodTicketMinusButton.setOnClickListener{
            mainViewModel.adjustTicketQuantity("food", "-")
        }

        binding.ticketPurchaseButton.setOnClickListener {
            if(mainViewModel.userInformation.value?.availableReward!! > 0){
                Log.d(TAG, "clickListener: 리워드")
                existAvailableRewardDialog()
            }else{
                Log.d(TAG, "clickListener: 그냥")
                mainViewModel.updatePaymentType("kakao")
                mainViewModel.createPaymentRequest() //카카오페이 결제 요청
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun existAvailableRewardDialog() {
        val dialog = Dialog(requireContext())

        val dialogBinding: DialogAvailableRewardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_available_reward,
            null,
            false
        )

        dialogBinding.ticketPurchaseViewModel = mainViewModel
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        val size = mainViewModel.getDisplaySize(0.9f, 0.35f)
        dialog.window!!.setLayout(size.first, size.second)
        dialog.show()

        dialogBinding.paymentTypeCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.paymentRegular.setOnClickListener {
            dialog.dismiss()
            mainViewModel.updatePaymentType("kakao")
            mainViewModel.createPaymentRequest() // 그냥 결제 요청
        }

        dialogBinding.paymentReward.setOnClickListener {
            dialog.dismiss()
            mainViewModel.useRewards()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}