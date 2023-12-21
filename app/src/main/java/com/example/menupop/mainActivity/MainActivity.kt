package com.example.menupop.mainActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.ActivityMainBinding
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.login.LoginActivity
import com.example.menupop.mainActivity.exchange.ExchangeFragment
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceFragment
import com.example.menupop.mainActivity.profile.KakaoPayWebView
import com.example.menupop.mainActivity.profile.ProfileFragment
import com.example.menupop.mainActivity.profile.TicketPurchaseFragment
import com.example.menupop.mainActivity.profile.WithdrawalFragment
import com.example.menupop.mainActivity.translation.CameraActivity
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar


class MainActivity : AppCompatActivity(), MainActivityEvent{
    val TAG = "MainActivity"

    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var binding : ActivityMainBinding

    lateinit var foodPreferenceFragment: FoodPreferenceFragment
    lateinit var exchangeFragment: ExchangeFragment
    lateinit var profileFragment: ProfileFragment

    lateinit var ticketPurchaseFragment: TicketPurchaseFragment


    var identifier : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        settingListener()


    }


    private fun init(){
        Log.d(TAG, "main activity init start")
        foodPreferenceFragment = FoodPreferenceFragment()
        exchangeFragment = ExchangeFragment()
        profileFragment = ProfileFragment()
        ticketPurchaseFragment = TicketPurchaseFragment()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainActivityViewModel
        binding.lifecycleOwner = this

        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.GONE

        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "음식 등록"

        /**
         * 여기서 dailyTranslation, dailyReward 를 받아....!
         */
        var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        identifier = mainActivityViewModel.getUserInfo(sharedPreferences)

        mainActivityViewModel.requestUserInformation(identifier!!)



        mainActivityViewModel.rewardedAd.observe(this){
            it.show(this, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward. ${rewardAmount} ${rewardType}")
                val sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
                mainActivityViewModel.rewardedSuccess(sharedPreferences)
            })
        }

        mainActivityViewModel.checkingTranslationTicket.observe(this){ result ->
            Log.d(TAG, "푸드 티켓 확인 값: ${result}")
            if (result){ // 티켓이 있을때
                val intent = Intent(this,CameraActivity::class.java)
                startActivity(intent)
                mainActivityViewModel.translationTicketMinus(sharedPreferences)
            }else { // 티켓이 없을때
                emptyTicketShowDialog()
            }

        }

//        mainActivityViewModel.setRewarded(getSharedPreferences("userInfo", MODE_PRIVATE))

        mainActivityViewModel.scheduleMidnightWork(application)


        mainActivityViewModel.isLoading.observe(this, Observer {
            if(it){

                binding.appbarMenu.visibility = View.VISIBLE
                binding.homeFrameLayout.visibility = View.VISIBLE

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.home_frame_layout,foodPreferenceFragment)
                    commit()
                }

            }else{
                //
            }
        })




    }

    fun emptyTicketShowDialog(){
        val bindingDialog : DialogTicketBottomBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_ticket_bottom, null, false);
        val bottomSheetDialog = BottomSheetDialog(this)
        val foodTicket = mainActivityViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainActivityViewModel.userInformation.value?.translationTicket.toString()
        bottomSheetDialog.setContentView(bindingDialog.root)
        bindingDialog.dialogTicketBottomFoodTicket.text = "음식 티켓 $foodTicket 개"
        bindingDialog.dialogTicketBottomTranslationTicket.text = "번역 티켓 $translationTicket 개"
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            Log.d(TAG, "favoriteItemClick: 결제 화면 띄우기")
            moveToTicketPurchase()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun settingListener() {
        // 선택 리스너 등록
        binding.bottomNavigation.setOnItemSelectedListener(TabSelectedListener())
    }


    inner class TabSelectedListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.tab_food -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, FoodPreferenceFragment())
                        .commit()
                    binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "음식 등록"
                    return true
                }
                R.id.tab_camera -> {
                    Log.d(TAG, "onNavigationItemSelected: 카메라 탭 선택")
                    mainActivityViewModel.checkingTranslationTicket()
                    return false
                }
                R.id.tab_exchange -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, ExchangeFragment())
                        .commit()
                    binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "환율"
                    return true
                }
                R.id.tab_profile -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, profileFragment)
                        .commit()
                    binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "프로필 확인"
                    return true
                }
            }
            return false
        }
    }

    override fun moveToTicketPurchase() {
        Log.d(TAG, "moveToTicketPurchase: 호출")

        val bundle = Bundle()
        bundle.putInt("identifier", identifier!!)
        ticketPurchaseFragment.arguments = bundle
        binding.bottomNavigation.selectedItemId = R.id.tab_profile


        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, ticketPurchaseFragment)
            commit()
            binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "티켓 구매"
            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.VISIBLE

            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener{
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.home_frame_layout, ProfileFragment())
                    commit()
                    binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "프로필 확인"
                    binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.GONE

                }
            }

            Log.d(TAG, "moveToTicketPurchase Main: ")
        }
    }

    override fun moveToWebView() {
        val webViewFragment = KakaoPayWebView()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, webViewFragment)
            commit()
        }
    }

    override fun completePayment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, profileFragment)
            commit()
        }
    }

    override fun moveToAdvertisement() {
        val rewarded = getSharedPreferences("userInfo", MODE_PRIVATE).getInt("dailyReward",0)
        Log.d(TAG, "showDialog: ${rewarded}")
        if(rewarded >= 3){
            Toast.makeText(this,"하루에 받을 수 있는 리워드를 초과했습니다.", Toast.LENGTH_SHORT).show()
        }else{
            val key = BuildConfig.GOOGLE_AD_ID
            mainActivityViewModel.loadAd(key)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun accountWithdrawal() {
        //회원 탈퇴
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, WithdrawalFragment())
            commit()
            binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "회원탈퇴"
        }
    }

    override fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun moveToProfile() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, profileFragment)
                .commit()
        }
    }


}
