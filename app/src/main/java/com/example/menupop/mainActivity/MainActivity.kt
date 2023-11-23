package com.example.menupop.mainActivity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(){
    val TAG = "MainActivity"

    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var binding : ActivityMainBinding
    lateinit var foodPreferenceFragment: FoodPreferenceFragment
    lateinit var exchangeFragment: ExchangeFragment
    lateinit var profileFragment: ProfileFragment

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainActivityViewModel
        binding.lifecycleOwner = this



        var sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        val identifier = mainActivityViewModel.getUserInfo(sharedPreferences)

        mainActivityViewModel.requestUserInformation(identifier)

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
                    return true
                }
                R.id.tab_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, ProfileFragment())
                        .commit()
                    return true
                }
            }
            return false
        }
    }
}
