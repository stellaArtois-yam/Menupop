package com.example.menupop.mainActivity.foodPreference

import android.app.Dialog
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
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menupop.R
import com.example.menupop.databinding.DialogDeletePreferenceBinding
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.databinding.FragmentFoodPreferenceBinding
import com.example.menupop.mainActivity.MainActivityEvent
import com.example.menupop.mainActivity.MainActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class FoodPreferenceFragment : Fragment() {
    private var TAG = "FoodPreferenceFragment"
    lateinit var binding : FragmentFoodPreferenceBinding
    private lateinit var mainViewModel : MainActivityViewModel
    private lateinit var context : Context
    private lateinit var searchAdapter: FoodPreferenceSearchAdapter
    private lateinit var foodPreferenceAdapter: FoodPreferenceAdapter
    var event: MainActivityEvent? = null
    lateinit var existBottomSheetDialog : BottomSheetDialog

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
        val bindingDialog : DialogTicketBottomBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_ticket_bottom, null, false);
        val bottomSheetDialog = BottomSheetDialog(context)
        val foodTicket = mainViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainViewModel.userInformation.value?.translationTicket.toString()
        bottomSheetDialog.setContentView(bindingDialog.root)
        bindingDialog.dialogTicketBottomFoodTicket.text = "음식 티켓 $foodTicket 개"
        bindingDialog.dialogTicketBottomTranslationTicket.text = "번역 티켓 $translationTicket 개"
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
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
        val bindingDialog : DialogTicketBottomBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_ticket_bottom, null, false);
        val foodTicket = mainViewModel.userInformation.value?.foodTicket.toString()
        val translationTicket = mainViewModel.userInformation.value?.translationTicket.toString()
        existBottomSheetDialog.setContentView(bindingDialog.root)
        bindingDialog.dialogTicketBottomFoodTicket.text = "음식 티켓 ${foodTicket} 개"
        bindingDialog.dialogTicketBottomTranslationTicket.text = "번역 티켓 ${translationTicket} 개"
        bindingDialog.dialogTicketBottomButton.text = "등록하기"
        bindingDialog.dialogTicketBottomText.text = setTextBold("[${foodName}]를 [${classfication}] 음식으로\n등록하시겠습니까?",foodName,classfication)
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            Log.d(TAG, "existTicketShowDialog: 클릭 됨")
            var sharedPreferences = context.getSharedPreferences("userInfo",
                AppCompatActivity.MODE_PRIVATE
            )
            mainViewModel.foodPreferenceRegister(sharedPreferences,foodName,classfication)

        }

        existBottomSheetDialog.show()
    }
    fun deleteFoodPreferenceItem(foodName: String,classification: String){
        val dialog = Dialog(context)
        val binding : DialogDeletePreferenceBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_delete_preference, null, false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        binding.dialogDeletePreferenceWarning.text = setTextBold("[${foodName}]를 [${classification}] 음식에서 \n삭제 하시겠습니까?",foodName,classification)
        binding.dialogDeletePreferenceDeleteButton.setOnClickListener {
            Log.d(TAG, "deleteFoodPreferenceItem: 호출")
            mainViewModel.deletedResult.observe(viewLifecycleOwner){ result ->
                if(result){
                    dialog.dismiss()
                    var sharedPreferences = context.getSharedPreferences("userInfo",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    mainViewModel.getFoodPreference(sharedPreferences)
                }else{
                    Toast.makeText(context,"잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
            }
            var sharedPreferences = context.getSharedPreferences("userInfo",
                AppCompatActivity.MODE_PRIVATE
            )
            mainViewModel.deleteFoodPreference(sharedPreferences,foodName)
        }
        binding.dialogDeletePreferenceCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun init(){
        mainViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainViewModel
        binding.lifecycleOwner = this
        searchAdapter = FoodPreferenceSearchAdapter(object : FoodPreferenceItemClickListener {
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
        existBottomSheetDialog = BottomSheetDialog(context)
        binding.foodPreferenceSearchRecyclerview.adapter = searchAdapter
        binding.foodPreferenceSearchRecyclerview.layoutManager = LinearLayoutManager(context)


        foodPreferenceAdapter = FoodPreferenceAdapter(object : FoodPreferenceClickListener {
            override fun deleteBtnClick(foodPreference: FoodPreference) {
                deleteFoodPreferenceItem(foodPreference.foodName,foodPreference.classification)
            }

        })
        binding.foodPreferenceRecyclerview.adapter = foodPreferenceAdapter
        binding.foodPreferenceRecyclerview.layoutManager = LinearLayoutManager(context)

        var sharedPreferences = context.getSharedPreferences("userInfo",
            AppCompatActivity.MODE_PRIVATE
        )
        mainViewModel.getFoodPreference(sharedPreferences)

        mainViewModel.foodPreferenceList.observe(viewLifecycleOwner){ it->
            if (it.result.trim() == "success") {
                foodPreferenceAdapter.setFoodList(it.foodList)
                binding.foodPreferenceEmptyListWarring.visibility = View.GONE
            }else if(it.result.trim()=="notRegister"){
                binding.foodPreferenceEmptyListWarring.visibility = View.VISIBLE
                binding.foodPreferenceRecyclerview.visibility = View.INVISIBLE
            }
        }

        mainViewModel.searchFood.observe(viewLifecycleOwner){ it ->

            Log.d(TAG, "init: ${it}")
            searchAdapter.setFoodList(it)

        }


        mainViewModel.registerResult.observe(viewLifecycleOwner){ result ->
            Log.d(TAG, "existTicketShowDialog: ${result}")
            if(result) {
                var sharedPreferences = context.getSharedPreferences("userInfo",
                    AppCompatActivity.MODE_PRIVATE
                )
                existBottomSheetDialog.dismiss()
                mainViewModel.foodTicketMinus(sharedPreferences)

                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

                binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                binding.foodPreferenceSearchRecyclerview.visibility = View.GONE

                mainViewModel.getFoodPreference(sharedPreferences)
                mainViewModel.registerVariableReset()

            } else {
                Log.d(TAG, "existTicketShowDialog: 실패")
            }
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
