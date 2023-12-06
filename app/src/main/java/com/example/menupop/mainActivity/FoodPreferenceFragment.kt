package com.example.menupop.mainActivity

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
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
    private lateinit var foodPreferenceAdapter: FoodPreferenceAdapter
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
        if(mainViewModel.userInformation.value?.foodTicket!! <= 0){
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
    fun setTextBold(text:String,foodName: String,classification: String) : SpannableString {
        val spannableString = SpannableString(text)

// 변수 부분을 볼드체로 설정
        val startIndex = text.indexOf("[$foodName]")
        val endIndex = startIndex + foodName.length + 2 // 변수를 감싸는 [] 기호를 제외한 길이
        spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val startIndex2 = text.indexOf("[$classification]")
        val endIndex2 = startIndex2 + classification.length + 2
        spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD), startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
    fun existTicketShowDialog(foodName:String,classfication : String){
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_ticket_bottom, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        val foodTicket = mainViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainViewModel.userInformation.value?.translationTicket.toString()
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_food_ticket)?.text = "음식 티켓 ${foodTicket} 개"
        bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_translation_ticket)?.text = "번역 티켓 ${translationTicket} 개"
        bottomSheetDialog.findViewById<Button>(R.id.dialog_ticket_bottom_button)?.text = "등록하기"
        bottomSheetDialog.findViewById<TextView>(R.id.dialog_ticket_bottom_text)?.text = setTextBold("[${foodName}]를 [${classfication}] 음식으로\n등록하시겠습니까?",foodName,classfication)
        bottomSheetDialog.findViewById<Button>(R.id.dialog_ticket_bottom_button)?.setOnClickListener {
            Log.d(TAG, "existTicketShowDialog: 클릭 됨")
            var sharedPreferences = context.getSharedPreferences("userInfo",
                AppCompatActivity.MODE_PRIVATE
            )
            mainViewModel.foodPreferenceRegister(sharedPreferences,foodName,classfication)
            mainViewModel.registerResult.observe(viewLifecycleOwner){ result ->
                Log.d(TAG, "existTicketShowDialog: ${result}")
                if(result) {
                    bottomSheetDialog.dismiss()
                    mainViewModel.ticketMinus()
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                    binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                    binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
                } else {
                    Log.d(TAG, "existTicketShowDialog: 실패")
                }
            }

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
                    existTicketShowDialog(foodName,"호")
                }

            }

            override fun unFavoriteItemClick(foodName: String) {
                Log.d(TAG, "unFavoriteItemClick: 싫어하는 음식 ${foodName} 클릭됨 ")
                if(checkTicketEmpty()){
                    emptyTicketShowDialog()
                } else{
                    existTicketShowDialog(foodName,"불호")
                }
            }

        })
        binding.foodPreferenceSearchRecyclerview.adapter = searchAdapter
        binding.foodPreferenceSearchRecyclerview.layoutManager = LinearLayoutManager(context)


        foodPreferenceAdapter = FoodPreferenceAdapter()
        binding.foodPreferenceRecyclerview.adapter = foodPreferenceAdapter
        binding.foodPreferenceRecyclerview.layoutManager = LinearLayoutManager(context)

        var sharedPreferences = context.getSharedPreferences("userInfo",
            AppCompatActivity.MODE_PRIVATE
        )
        mainViewModel.getFoodPreference(sharedPreferences)

        mainViewModel.foodPreferenceList.observe(viewLifecycleOwner){ it->
            foodPreferenceAdapter.setFoodList(it)
        }

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
                    binding.foodPreferenceSearchRecyclerview.visibility = View.VISIBLE
                    binding.foodPreferenceRecyclerview.visibility = View.GONE
                    mainViewModel.searchFood(query)
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: ${newText}")
                if(newText?.isEmpty() == true){
                    binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
                    binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                }
                return true
            }

        })
    }


}