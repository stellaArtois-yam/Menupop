package com.example.menupop.mainActivity.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileBinding
import com.example.menupop.login.LoginActivity
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.mainActivity.MainActivityViewModel
import kotlinx.coroutines.launch

@SuppressLint("ResourceAsColor")
class ProfileFragment : Fragment() {
    companion object {
        const val TAG = "profileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.mainActivityViewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("userInfo", MODE_PRIVATE)

        viewModel.getProfileImage(sharedPreferences, resources)

        setObservers()
        setClick()
    }

    private fun setObservers() {

        viewModel.userInformation.observe(viewLifecycleOwner) {
            if (it.freeFoodTicket == 0) {
                binding.profileFoodCount.text = "무료티켓 소진"
                binding.profileFoodCount.setTextColor(Color.RED)
            }
        }

        viewModel.rewardedAd.observe(viewLifecycleOwner) {
            if (it != null) {
                loadingDialog.dismiss()
                it.show(requireActivity()) { rewardItem ->
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward: $rewardAmount, $rewardType")
                    lifecycleScope.launch {
                        viewModel.rewardedSuccess()
                    }
                }
            }

        }

        viewModel.isRewardSuccessful.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.profileAdCount.text =
                        "${viewModel.userInformation.value?.availableReward}/3"
                    viewModel.initializeIsRewardSuccessful()
                }

                false -> {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.initializeIsRewardSuccessful()
                }

                else -> Log.d(TAG, "isRewardSuccessful is null")
            }
        }


        viewModel.profileImage.observe(viewLifecycleOwner) {
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
            Log.d(TAG, "티켓 구매")
            findNavController().navigate(R.id.ticketPurchaseFragment)
        }

        // 광고보러 가기 누르면 광고 프래그먼트로 이동
        binding.profileAdButton.setOnClickListener {
            if (viewModel.userInformation.value!!.dailyReward == 0) {
                Toast.makeText(requireContext(), "하루에 받을 수 있는 리워드를 초과했습니다.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val key = BuildConfig.GOOGLE_AD_ID
                Log.d(MainActivity.TAG, "key: $key")
                loadingDialog = loadDialog()
                loadingDialog.show()
                lifecycleScope.launch {
                    viewModel.loadAd(key)
                }
            }
        }

        // 로그아웃 다이얼로그
        binding.profileLogoutButton.setOnClickListener {
            showLogoutDialog()
        }

        // 회원탈퇴 프래그먼트 이동
        binding.profileWithdrawalButton.setOnClickListener {
            findNavController().navigate(R.id.withdrawalFragment)
        }
    }


    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val size = viewModel.getDisplaySize(0.9f, 0.3f)
        dialog.window!!.setLayout(size.first, size.second)
        dialog.show()

        dialog.findViewById<Button>(R.id.dialog_two_button_agree).setOnClickListener {
            dialog.dismiss()

            val sharedPreferences =
                requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
            viewModel.logout(sharedPreferences!!)

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        dialog.findViewById<Button>(R.id.dialog_two_button_disagree).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun loadDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.progress_text).visibility = View.GONE
        dialog.findViewById<ImageView>(R.id.progress_image).visibility = View.GONE

        dialog.show()

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}