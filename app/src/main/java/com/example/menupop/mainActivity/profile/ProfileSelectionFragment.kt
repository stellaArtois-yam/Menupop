package com.example.menupop.mainActivity.profile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.menupop.R
import com.example.menupop.databinding.FragmentProfileSelectionBinding
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.mainActivity.MainActivityViewModel

class ProfileSelectionFragment : Fragment() {
    companion object{
        const val TAG = "ProfileSelectionFragment"
    }

    private var _binding: FragmentProfileSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel : MainActivityViewModel
    private lateinit var context: Context
    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var sharedPreferences : SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_selection, container, false)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setClickListener()
    }


    private fun setAdapter(){
        val imageList = mainViewModel.getProfileList(resources)
        Log.d(TAG, "init imageList: $imageList")

        profileAdapter = ProfileAdapter(object  : ProfileSelectionClickListener{
            override fun onItemClick(position: Int) {

                val imageName = resources.getStringArray(R.array.profile)[position]

                sharedPreferences = context.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
                mainViewModel.saveSelectedProfile(imageName, sharedPreferences, resources)
            }
        })

        mainViewModel.isChangedProfile.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.profileFragment)
            }
        }

        binding.profileSelectionRecyclerview.adapter = profileAdapter
        binding.profileSelectionRecyclerview.layoutManager = GridLayoutManager(context, 2)

        if(imageList.isNotEmpty()){
            profileAdapter.setProfileSelection(imageList)
        }
    }

    private fun setClickListener(){
        // 뒤로가기 버튼 활성화
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뒤로가기 버튼 클릭 시 동작 정의
        (activity as MainActivity).binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()  // 뒤로 가기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}