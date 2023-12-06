package com.example.menupop.mainActivity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebViewFragment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        identifier = mainActivityViewModel.getUserInfo(sharedPreferences)

        mainActivityViewModel.requestUserInformation(identifier!!)

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
//                R.id.camera -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.home_ly, DogFragment())
//                        .commit()
//                    return true
//                }
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

    override fun moveToAdvertisement() {
        //광고보러 가기
    }

    override fun accountWithdrawal() {
      //회원 탈퇴
    }


}
