package com.example.menupop.mainActivity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.menupop.BuildConfig
import com.example.menupop.R
import com.example.menupop.databinding.ActivityMainBinding
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.login.LoginActivity
import com.example.menupop.mainActivity.exchange.ExchangeFragment
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceFragment
import com.example.menupop.mainActivity.profile.KakaoPayWebView
import com.example.menupop.mainActivity.profile.ProfileFragment
import com.example.menupop.mainActivity.profile.ProfileSelectionFragment
import com.example.menupop.mainActivity.profile.TicketPurchaseFragment
import com.example.menupop.mainActivity.profile.WithdrawalFragment
import com.example.menupop.mainActivity.translation.CameraActivity
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.log


class MainActivity : AppCompatActivity(), MainActivityEvent{
    val TAG = "MainActivityTAG"

    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var binding : ActivityMainBinding

    lateinit var foodPreferenceFragment: FoodPreferenceFragment
    lateinit var exchangeFragment: ExchangeFragment
    lateinit var profileFragment: ProfileFragment
    lateinit var countrySelectionFragment : CountrySelectionFragment

    lateinit var ticketPurchaseFragment: TicketPurchaseFragment
    var checkingTranslation : Boolean = false

    var identifierByIntent : Int = 0

    lateinit var loadingDialog: Dialog

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(applicationContext, "한 번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                delay(2000) // 2초 대기
                doubleBackToExitPressedOnce = false
            }

        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingDialog = loadingDialog()

        init(loadingDialog)

        settingListener()

    }


    private fun init(loadingDialog : Dialog){
        foodPreferenceFragment = FoodPreferenceFragment()
        exchangeFragment = ExchangeFragment()
        profileFragment = ProfileFragment()
        ticketPurchaseFragment = TicketPurchaseFragment()
        countrySelectionFragment = CountrySelectionFragment()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainActivityViewModel
        binding.lifecycleOwner = this

        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.GONE

        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "음식 등록"


        /**
         * 초기 유저 정보 세팅
         */

        identifierByIntent = intent.getIntExtra("identifier", 0)


        if(identifierByIntent != 0){
            mainActivityViewModel.setIdentifier(identifierByIntent)

        }else{
            Log.d(TAG, "identifier intent: ${mainActivityViewModel.identifier.value}")
        }

        mainActivityViewModel.identifier.observe(this, Observer{
            Log.d(TAG, "init identifier observe: $it")

            if(it != null){
                mainActivityViewModel.requestUserInformation(it)

            }else{
                Log.d(TAG, "identifier observe: ${mainActivityViewModel.identifier.value}")
            }
        })

        checkingTranslation = intent.getBooleanExtra("checkedTranslation", false)


        mainActivityViewModel.checkingTranslationTicket.observe(this){ result ->

            when(result){
                true -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, countrySelectionFragment)
                        .commit()
                    binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "번역"
                }
                else -> {
                    emptyTicketShowDialog()
                }
            }
        }


        /**
         * 초기화
         */
        mainActivityViewModel.scheduleMidnightWork(application)



        mainActivityViewModel.isLoading.observe(this, Observer {
            if(it.equals("success")){
                if(checkingTranslation){
                    if(mainActivityViewModel.userInformation.value!!.freeTranslationTicket > 0){
                        mainActivityViewModel.updateTicketQuantity("free_translation_ticket", "-", 1)
                        Log.d(TAG, "Free Translation Ticket Use")
                    }else{
                        mainActivityViewModel.updateTicketQuantity("translation_ticket", "-", 1)
                    }
                    Log.d(TAG, "init Translation Ticket: ${mainActivityViewModel.userInformation.value!!.translationTicket} ")

                }

                binding.appbarMenu.visibility = View.VISIBLE
                binding.homeFrameLayout.visibility = View.VISIBLE

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.home_frame_layout,foodPreferenceFragment)
                    commit()
                }

                loadingDialog.dismiss()

            }else if(it.equals("yet")){
                loadingDialog

            }else if(it.equals("isNotSuccessful") || it.equals("timeout")){
                Toast.makeText(this, "서버 오류로 로딩이 불가합니다 :(", Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()

            }

        })



        mainActivityViewModel.rewardedAd.observe(this){

            if(it != null){
                it.show(this, OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward. ${rewardAmount} ${rewardType}")
                    mainActivityViewModel.rewardedSuccess()
                })

                loadingDialog.dismiss()
            }

        }

        this.onBackPressedDispatcher.addCallback(this, callback)

    }

    fun loadingDialog() : Dialog{

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.progress_text).visibility = View.GONE
        dialog.findViewById<ImageView>(R.id.progress_image).visibility = View.GONE

        dialog.show()

        Log.d(TAG, "loadingDialog: 실행")




        return dialog



    }

    fun emptyTicketShowDialog(){
        val bindingDialog : DialogTicketBottomBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_ticket_bottom, null, false);
        val bottomSheetDialog = BottomSheetDialog(this)
        val foodTicket = mainActivityViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainActivityViewModel.userInformation.value?.translationTicket.toString()
        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


        bindingDialog.dialogTicketBottomFoodTicket.text = "음식 티켓 : $foodTicket 개"
        bindingDialog.dialogTicketBottomTranslationTicket.text = "번역 티켓 : $translationTicket 개"
        bindingDialog.dialogTicketBottomButton.setOnClickListener {

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
                    if(mainActivityViewModel.tabStatus.value != 1){
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.home_frame_layout, FoodPreferenceFragment())
                            .commit()
                        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "음식 등록"
                        backButtonGone()
                        mainActivityViewModel.setTabStatus(1)
                    }

                    return true
                }
                R.id.tab_translation -> {
                    if(mainActivityViewModel.tabStatus.value != 2){
                        Log.d(TAG, "onNavigationItemSelected: 카메라 탭 선택")
                        mainActivityViewModel.checkingTranslationTicket()
                        backButtonGone()
                        mainActivityViewModel.setTabStatus(2)
                    }
                    return true
                }
                R.id.tab_exchange -> {
                    if(mainActivityViewModel.tabStatus.value != 3){

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.home_frame_layout, ExchangeFragment())
                            .commit()
                        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "환율"
                        backButtonGone()
                        mainActivityViewModel.setTabStatus(3)
                    }
                    return true
                }
                R.id.tab_profile -> {
                    if(mainActivityViewModel.tabStatus.value != 4){

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.home_frame_layout, profileFragment)
                            .commit()
                        binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "프로필 확인"
                        backButtonGone()
                        mainActivityViewModel.setTabStatus(4)
                    }
                    return true
                }
            }
            return false
        }
    }

    override fun moveToProfileSelection() {
        Log.d(TAG, "moveToProfileSelection: 호출")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, ProfileSelectionFragment())
            commit()
            binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "프로필 선택"
            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.VISIBLE

            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener{
                moveToProfile()
            }

        }
    }

    override fun moveToTicketPurchase() {
        Log.d(TAG, "moveToTicketPurchase: 호출")

        val bundle = Bundle()
        bundle.putInt("identifier", mainActivityViewModel.identifier.value!!)
        ticketPurchaseFragment.arguments = bundle
        binding.bottomNavigation.selectedItemId = R.id.tab_profile


        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, ticketPurchaseFragment)
            commit()
            binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "티켓 구매"
            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.VISIBLE

            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).setOnClickListener{
                moveToProfile()
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
        binding.appbarMenu.visibility = View.GONE
        binding.bottomNavigation.visibility = View.GONE
    }

    override fun completePayment() {
        mainActivityViewModel.setChangeTicket()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_frame_layout, profileFragment)
            commit()
        }
        binding.appbarMenu.visibility = View.VISIBLE
        binding.bottomNavigation.visibility = View.VISIBLE
    }
    fun backButtonGone(){
        binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.GONE
    }


    override fun moveToAdvertisement() {
        if(mainActivityViewModel.userInformation.value!!.dailyReward == 0){
            Toast.makeText(this,"하루에 받을 수 있는 리워드를 초과했습니다.", Toast.LENGTH_SHORT).show()
        }else{
            loadingDialog.show()
            val key = BuildConfig.GOOGLE_AD_ID
            Log.d(TAG, "key: $key")
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
            binding.appbarMenu.findViewById<TextView>(R.id.appbar_status).text = "프로필 확인"
            binding.appbarMenu.findViewById<ImageView>(R.id.appbar_back).visibility = View.GONE
        }
    }


}
