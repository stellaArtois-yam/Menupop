package com.example.menupop.resetPassword

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

import com.example.menupop.R

class ResetPasswordActivity : AppCompatActivity(){
    var toolbar : Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.resetPasswordContainer) as NavHostFragment
        val navController = navHostFragment.navController

        toolbar  = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController.addOnDestinationChangedListener {
                _, _, _ ->
            findViewById<TextView>(R.id.toolbar_title).text = navController.currentDestination?.label.toString()
        }

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

}