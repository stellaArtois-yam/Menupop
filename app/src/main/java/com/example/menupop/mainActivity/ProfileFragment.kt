package com.example.menupop.mainActivity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    val TAG = "ProfileFragment"
    lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: MainActivityViewModel
    private var context : Context? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init(){
        profileViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = profileViewModel
        binding.lifecycleOwner = this

        profileViewModel.userInformation.observe(viewLifecycleOwner, Observer{
            if(it.id != null){
                Log.d(TAG, "init: $id")
                binding.profileId.text = it.id
            }else{
                Log.d(TAG, "init: null")
            }
        })
    }


}