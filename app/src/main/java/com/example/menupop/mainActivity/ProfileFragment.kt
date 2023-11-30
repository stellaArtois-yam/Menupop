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
import com.example.menupop.findId.FindIdFragmentEvent

class ProfileFragment : Fragment() {
    val TAG = "ProfileFragment"
    lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private var context : Context? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setClick()
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

    fun setClick(){
        //티켓 구매를 누르면 티켓 프래그먼트로 이동
        binding.profileBuyTicketButton.setOnClickListener{
            event?.moveToTicketPurchase()
            Log.d(TAG, "setClick move to Ticket: ")
        }

        // 광고보러 가기 누르면 광고 프래그먼트로 이동
        binding.profileAdButton.setOnClickListener{
            event?.moveToAdvertisement()
        }

        // 로그아웃하면 다이얼로그 뜨고
        binding.profileLogoutButton.setOnClickListener{

        }


        // 회원탈퇴 누르면 프래그먼트 이동
        binding.profileWithdrawalButton.setOnClickListener{
            event?.accountWithdrawal()
        }
    }


}