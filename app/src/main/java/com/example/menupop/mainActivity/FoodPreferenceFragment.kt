package com.example.menupop.mainActivity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menupop.R
import com.example.menupop.databinding.FragmentExchangeBinding
import com.example.menupop.databinding.FragmentFoodPreferenceBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class FoodPreferenceFragment : Fragment() {
    private var TAG = "FoodPreferenceFragment"
    lateinit var binding : FragmentFoodPreferenceBinding
    private lateinit var mainViewModel : MainActivityViewModel
    private lateinit var context : Context
    private lateinit var searchAdapter: FoodPreferenceSearchAdapter
    var event: MainActivityEvent? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        setListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_food_preference, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
    fun checkTicketEmpty() : Boolean{
        if(mainViewModel.userInformation.value?.foodTicket ==0){
            return true
        }
        return false
    }
    fun emptyTicketShowDialog(){
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_ticket_bottom, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        val foodTicket = mainViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainViewModel.userInformation.value?.translationTicket.toString()
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_food_ticket)?.text = "음식 티켓 ${foodTicket} 개"
        bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_translation_ticket)?.text = "번역 티켓 ${translationTicket} 개"
        bottomSheetDialog.findViewById<Button>(R.id.dialog_ticket_bottom_button)?.setOnClickListener {
            Log.d(TAG, "favoriteItemClick: 결제 화면 띄우기")
            event?.moveToTicketPurchase()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun init(){
        mainViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainViewModel
        binding.lifecycleOwner = this
        searchAdapter = FoodPreferenceSearchAdapter(object : FoodPreferenceItemClickListener{
            override fun favoriteItemClick(foodName: String) {
                Log.d(TAG, "favoriteItemClick: 좋아하는 음식 ${foodName} 클릭됨 ")
                if(checkTicketEmpty()){
                    emptyTicketShowDialog()
                } else{
//                    호 음식 등록하기
//
//                    val bottomSheetView = layoutInflater.inflate(R.layout.dialog_, null)
//                    val bottomSheetDialog = BottomSheetDialog(context)
//                    val foodTicket = mainViewModel.userInformation.value?.foodTicket.toString()
//                    val translationTicket = mainViewModel.userInformation.value?.translationTicket.toString()
//                    bottomSheetDialog.setContentView(bottomSheetView)
//                    bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_food_ticket)?.text = "음식 티켓 ${foodTicket} 개"
//                    bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_translation_ticket)?.text = "번역 티켓 ${translationTicket} 개"
                }

            }

            override fun unFavoriteItemClick(foodName: String) {
                Log.d(TAG, "unFavoriteItemClick: 싫어하는 음식 ${foodName} 클릭됨 ")
                if(checkTicketEmpty()){
                    emptyTicketShowDialog()
                } else{
//                  불호 음식 등록하기
                }
            }

        })
        binding.foodPreferenceRecyclerview.adapter = searchAdapter
        binding.foodPreferenceRecyclerview.layoutManager = LinearLayoutManager(context)

        mainViewModel.searchFood.observe(viewLifecycleOwner){ it ->

            Log.d(TAG, "init: ${it}")
            searchAdapter.setFoodList(it)

        }

    }

    fun setListener(){
        binding.foodPreferenceSearchview.setOnQueryTextListener(object  : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: ${query}")
                if(query!=null){
                    mainViewModel.searchFood(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: ${newText}")
                return true
            }

        })
    }


}