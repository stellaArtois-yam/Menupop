package com.example.menupop.mainActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentCountrySelectionBinding
import com.example.menupop.mainActivity.translation.CameraActivity
import com.example.menupop.resetPassword.ResetPasswordFragmentEvent

class CountrySelectionFragment  : Fragment(){
    private val TAG = "CountrySelectionFragment"
    private lateinit var context : Context
    lateinit var binding : FragmentCountrySelectionBinding
    private lateinit var mainViewModel : MainActivityViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_country_selection, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainViewModel
        binding.lifecycleOwner = this
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.countrySelection.observe(viewLifecycleOwner){
            Log.d(TAG, "onViewCreated: ${it}")
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra("foodPreference",mainViewModel.foodPreferenceList.value?.foodList)
            intent.putExtra("country",it)
            startActivity(intent)
        }
        setListener()
    }
    fun setListener(){
        binding.countrySelectionAmerica.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionAmerica)
        }
        binding.countrySelectionChina.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionChina)
        }
        binding.countrySelectionHongkong.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionHongkong)
        }
        binding.countrySelectionTaiwan.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionTaiwan)
        }
        binding.countrySelectionVietnam.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionVietnam)
        }
        binding.countrySelectionJapan.setOnClickListener {
            mainViewModel.selectionCountry(binding.countrySelectionJapan)
        }
    }
}