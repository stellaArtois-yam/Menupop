package com.example.menupop.findId

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.ActivityFindIdBinding

class FindIdActivity : AppCompatActivity(),FindIdFragmentEvent {
    val TAG = "FindIdActivity"

    private lateinit var findIdViewModel : FindIdViewModel
    private lateinit var binding: ActivityFindIdBinding
    private lateinit var findIdFragment: FindIdFragment
    private lateinit var findIdResultFragment: FindIdResultFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

    }

    fun init(){

        findIdFragment = FindIdFragment()
        findIdResultFragment = FindIdResultFragment()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_id)
        findIdViewModel = ViewModelProvider(this).get(FindIdViewModel::class.java)
        binding.findIdViewModel = findIdViewModel
        binding.lifecycleOwner = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.find_id_frame_layout, findIdFragment)
            commit()

        }


    }

    override fun successFindId() {

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.find_id_frame_layout, findIdResultFragment)
            commit()
        }
    }

    override fun finishFindId() {
        finish()
    }

    override fun backButtonClick() {
        finish()
    }
}