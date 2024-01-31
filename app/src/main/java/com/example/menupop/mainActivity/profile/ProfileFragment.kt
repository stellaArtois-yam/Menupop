package com.example.menupop.mainActivity.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileBinding
import com.example.menupop.mainActivity.MainActivityEvent
import com.example.menupop.mainActivity.MainActivityViewModel
import com.google.android.gms.ads.OnUserEarnedRewardListener

@SuppressLint("ResourceAsColor")
class ProfileFragment : Fragment() {
    val TAG = "ProfileFragment"
    lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private lateinit var context : Context
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if(context is MainActivityEvent){
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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setClick()
    }

    fun init(){
        profileViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = profileViewModel
        binding.lifecycleOwner = this

        sharedPreferences = context.getSharedPreferences("userInfo", MODE_PRIVATE)

        //프로필 이미지를 먼저 얻음
        profileViewModel.getProfileImage(sharedPreferences, resources)


        profileViewModel.userInformation.observe(viewLifecycleOwner){
            if(it.freeFoodTicket == 0){
                binding.profileFoodCount.text = "무료티켓 소진"
                binding.profileFoodCount.setTextColor(Color.RED)
            }

        }
        profileViewModel.rewardedAd.observe(viewLifecycleOwner){
            binding.profileAdButton.isClickable = true
        }


        profileViewModel.profileImage.observe(viewLifecycleOwner, Observer {
            if(it == null){
                binding.profileImage.setImageResource(R.drawable.profile_unselected)

            }else{
                Log.d(TAG, "init profile is not null")
            }
        })

    }

    fun setClick(){

        binding.profileImage.setOnClickListener{
            event?.moveToProfileSelection()
        }

        //티켓 구매를 누르면 티켓 프래그먼트로 이동
        binding.profileBuyTicketButton.setOnClickListener{
            event?.moveToTicketPurchase()
            Log.d(TAG, "setClick move to Ticket: ")
        }

        // 광고보러 가기 누르면 광고 프래그먼트로 이동
        binding.profileAdButton.setOnClickListener{
            event?.moveToAdvertisement()
            binding.profileAdButton.isClickable = false
            Log.d(TAG, "setClick: 광고 보러가기 클릭됨")
        }

        // 로그아웃하면 다이얼로그 뜨고
        binding.profileLogoutButton.setOnClickListener{
            showLogoutDialog()
        }


        // 회원탈퇴 누르면 프래그먼트 이동
        binding.profileWithdrawalButton.setOnClickListener{
            event?.accountWithdrawal()
        }
    }


    fun showLogoutDialog(){
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()

        dialog.findViewById<Button>(R.id.dialog_two_button_agree).setOnClickListener{
            dialog.dismiss()

            var sharedPreferences = context?.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
            profileViewModel.logout(sharedPreferences!!)

            event?.logout()
        }

        dialog.findViewById<Button>(R.id.dialog_two_button_disagree).setOnClickListener{
            dialog.dismiss()
        }




    }


}