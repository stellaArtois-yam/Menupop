package com.example.menupop.mainActivity.profile

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileSelectionBinding
import com.example.menupop.mainActivity.MainActivityEvent
import com.example.menupop.mainActivity.MainActivityViewModel

class ProfileSelectionFragment : Fragment() {
    val TAG = "ProfileSelectionFragment"
    private lateinit var binding: FragmentProfileSelectionBinding
    private lateinit var mainViewModel : MainActivityViewModel
    private lateinit var context: Context
    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var sharedPreferences : SharedPreferences
    var event : MainActivityEvent? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if(context is MainActivityEvent){
            event = context
            Log.d(TAG, "onAttach: 호출")
        }else{
            throw RuntimeException(
                context.toString()
                        + "must implement FindIdFragmentEvent"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_selection, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    fun init(){
        mainViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        val imageList = mainViewModel.getProfileList(resources)
        Log.d(TAG, "init imageList: $imageList")

        profileAdapter = ProfileAdapter(object  : ProfileSelectionClickListener{
            override fun onItemClick(position: Int) {

                val imageName = resources.getStringArray(R.array.profile)[position]
                Log.d(TAG, "onItemClick: ${imageName}")

                sharedPreferences = context.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
                mainViewModel.saveSelectedProfile(imageName, sharedPreferences, resources)
            }
        })

        mainViewModel.isChangedProfile.observe(viewLifecycleOwner, Observer {
            if(it){
                Log.d(TAG, "move To Profile:")
                event?.moveToProfile()
            }
        })


        binding.profileSelectionRecyclerview.adapter = profileAdapter
        binding.profileSelectionRecyclerview.layoutManager = GridLayoutManager(context, 2)

        if(imageList != null){
            profileAdapter.setProfileSelection(imageList)
        }

    }

    fun clickListener(){

    }
}