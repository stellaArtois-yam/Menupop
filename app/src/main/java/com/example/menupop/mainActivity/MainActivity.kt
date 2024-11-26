package com.example.menupop.mainActivity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.menupop.R
import com.example.menupop.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "mainActivityTAG"
    }

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    private lateinit var navController: NavController
    private var checkingTranslation: Boolean = false

    private var identifierByIntent: Int = 0

    private lateinit var loadingDialog: Dialog

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

        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainActivityViewModel = mainActivityViewModel
        binding.lifecycleOwner = this

        loadingDialog = loadDialog()

        //초기 유저 정보
        identifierByIntent = intent.getIntExtra("identifier", 0)
        checkingTranslation = intent.getBooleanExtra("checkedTranslation", false)

        setNavigation()
        setUserData()
        init()
    }

    private fun setUserData() {

        if (identifierByIntent != 0) {
            lifecycleScope.launch {
                mainActivityViewModel.requestUserInformation(identifierByIntent)
                mainActivityViewModel.setIdentifier(identifierByIntent)
            }

        } else {
            Log.d(TAG, "identifier intent: ${mainActivityViewModel.identifier.value}")
        }
    }

    private fun setNavigation() {
        val navHostFragment = binding.navHostFragment.getFragment() as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.toolbarTitle.text = navController.currentDestination?.label.toString()
        }

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == navController.currentDestination?.id) {
                // 이미 선택된 탭인 경우 처리 방지
                return@setOnItemSelectedListener false
            }
            navController.navigate(menuItem.itemId)
            true
        }

        // NavController에서 목적지가 변경될 때 ActionBar 상태 업데이트
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isRootDestination = when (destination.id) {
                R.id.profileFragment, R.id.foodPreferenceFragment, R.id.exchangeFragment, R.id.countrySelectionFragment -> true
                else -> false
            }
            // ActionBar 버튼 상태 설정
            supportActionBar?.setDisplayHomeAsUpEnabled(!isRootDestination)
        }
    }


    private fun init() {
        mainActivityViewModel.scheduleMidnightWork(application)

        mainActivityViewModel.isLoaded.observe(this) {
            Log.d(TAG, "isLoaded: $it")
            when(it){
                "success" ->{
                    //번역에 성공해서 번역 티켓을 사용해야한다
                    if (checkingTranslation) {
                        //사용자에게 무료 번역 티켓이 존재
                        lifecycleScope.launch {
                            if (mainActivityViewModel.userInformation.value!!.freeTranslationTicket > 0) {
                                //"free_transaltion_ticket" 타입의 티켓을 업데이트 한다
                                mainActivityViewModel.updateTicketQuantity(
                                    ticketType = "free_translation_ticket",
                                    operator = "-",
                                    quantity = 1
                                )

                            } else {
                                //없으면 ticket을 업데이트 한다
                                mainActivityViewModel.updateTicketQuantity(
                                    ticketType = "translation_ticket",
                                    operator = "-",
                                    quantity = 1)
                            }
                        }
                    }
                    loadingDialog.dismiss()
                }
                "isNotSuccessful", "failed" -> {
                    loadingDialog.dismiss()
                    networkErrorDialog()
                }
            }
        }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun loadDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.progress_text).visibility = View.GONE
        dialog.findViewById<ImageView>(R.id.progress_image).visibility = View.GONE

        dialog.show()

        return dialog
    }

    private fun networkErrorDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_one_button)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.findViewById<Button>(R.id.confirmButton).setOnClickListener{
            Log.d(TAG, "networkErrorDialog: 클릭")
            dialog.dismiss()
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
