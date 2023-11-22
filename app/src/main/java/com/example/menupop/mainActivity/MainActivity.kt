package com.example.menupop.mainActivity

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.menupop.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    lateinit var frameLayout : FrameLayout
    lateinit var bottomNavigation : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        init()

        bottomNavigation.selectedItemId = R.id.tab_food

        settingListener()


    }


    private fun init(){
        frameLayout = findViewById(R.id.home_frame_layout)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun settingListener() {
        // 선택 리스너 등록
        bottomNavigation.setOnNavigationItemSelectedListener(TabSelectedListener())
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
