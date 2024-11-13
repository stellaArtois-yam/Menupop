package com.example.menupop.mainActivity.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileBinding
import com.example.menupop.login.LoginActivity
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.mainActivity.MainActivityViewModel

@SuppressLint("ResourceAsColor")
class ProfileFragment : Fragment() {
    companion object{
        const val TAG = "profileFragment"
    }
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: MainActivityViewModel
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.mainActivityViewModel = profileViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setClick()
    }

    fun init() {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        sharedPreferences = context.getSharedPreferences("userInfo", MODE_PRIVATE)

        //프로필 이미지를 먼저 얻음
        profileViewModel.getProfileImage(sharedPreferences, resources)


        profileViewModel.userInformation.observe(viewLifecycleOwner) {
            if (it.freeFoodTicket == 0) {
                binding.profileFoodCount.text = "무료티켓 소진"
                binding.profileFoodCount.setTextColor(Color.RED)
            }

        }
        profileViewModel.rewardedAd.observe(viewLifecycleOwner) {
            binding.profileAdButton.isClickable = true
        }


        profileViewModel.profileImage.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.profileImage.setImageResource(R.drawable.profile_unselected)

            } else {
                Log.d(TAG, "init profile is not null")
            }
        }

    }

    private fun setClick() {
        //프로필 누르면 프로필 이미지 바꿈
        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.profileSelectionFragment)
        }

        //티켓 구매를 누르면 티켓 프래그먼트로 이동
        binding.profileBuyTicketButton.setOnClickListener {
            findNavController().navigate(R.id.ticketPurchaseFragment)
        }

        // 광고보러 가기 누르면 광고 프래그먼트로 이동
        binding.profileAdButton.setOnClickListener {
            binding.profileAdButton.isClickable = false
            Log.d(TAG, "setClick: 광고 보러가기 클릭됨")
            if (profileViewModel.userInformation.value!!.dailyReward == 0) {
                Toast.makeText(context, "하루에 받을 수 있는 리워드를 초과했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val key = BuildConfig.GOOGLE_AD_ID
                Log.d(MainActivity.TAG, "key: $key")
                profileViewModel.loadAd(key)
            }
        }

        // 로그아웃하면 다이얼로그 뜨고
        binding.profileLogoutButton.setOnClickListener {
            showLogoutDialog()
        }


        // 회원탈퇴 누르면 프래그먼트 이동
        binding.profileWithdrawalButton.setOnClickListener {
            findNavController().navigate(R.id.withdrawalFragment)
        }
    }


    private fun showLogoutDialog() {
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        dialog.findViewById<Button>(R.id.dialog_two_button_agree).setOnClickListener {
            dialog.dismiss()

            val sharedPreferences =
                context.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
            profileViewModel.logout(sharedPreferences!!)

            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        dialog.findViewById<Button>(R.id.dialog_two_button_disagree).setOnClickListener {
            dialog.dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}