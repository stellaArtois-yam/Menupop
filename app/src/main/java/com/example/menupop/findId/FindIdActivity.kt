package com.example.menupop.findId

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.menupop.R

class FindIdActivity : AppCompatActivity() {
    companion object{
        const val TAG = "FindIdActivity"
    }

    var toolbar : Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.find_id_container) as NavHostFragment
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