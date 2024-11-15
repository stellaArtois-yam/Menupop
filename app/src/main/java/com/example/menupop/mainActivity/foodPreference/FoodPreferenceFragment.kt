package com.example.menupop.mainActivity.foodPreference

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menupop.R
import com.example.menupop.databinding.DialogDeletePreferenceBinding
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.databinding.DialogWarningBinding
import com.example.menupop.databinding.FragmentFoodPreferenceBinding
import com.example.menupop.mainActivity.MainActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class FoodPreferenceFragment : Fragment() {
    companion object {
        const val TAG = "FoodPreferenceFragment"
    }

    private var _binding: FragmentFoodPreferenceBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainActivityViewModel
    private lateinit var context: Context
    private lateinit var searchAdapter: FoodPreferenceSearchAdapter
    private lateinit var foodPreferenceAdapter: FoodPreferenceAdapter
    private lateinit var existBottomSheetDialog: BottomSheetDialog


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_food_preference, container, false)
        binding.mainActivityViewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLiveData()
        setAdapters()
        setListener()
    }

    private fun setAdapters() {

        searchAdapter = FoodPreferenceSearchAdapter(object : FoodPreferenceItemClickListener {
            override fun favoriteItemClick(foodName: String) {
                Log.d(TAG, "favoriteItemClick: 좋아하는 음식 $foodName 클릭됨 ")
                if (checkTicketEmpty()) {
                    emptyTicketShowDialog()
                } else {
                    if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
                        existFreeTicketShowDialog(foodName, "호")
                    } else {
                        existTicketShowDialog(foodName, "호")
                    }
                }
            }

            override fun unFavoriteItemClick(foodName: String) {
                Log.d(TAG, "unFavoriteItemClick: 싫어하는 음식 $foodName 클릭됨 ")
                if (checkTicketEmpty()) {
                    emptyTicketShowDialog()
                } else if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
                    existFreeTicketShowDialog(foodName, "호")
                } else {
                    if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
                        existFreeTicketShowDialog(foodName, "불호")
                    } else {

                        existTicketShowDialog(foodName, "불호")
                    }
                }
            }
        })


        binding.foodPreferenceSearchRecyclerview.apply {
            binding.foodPreferenceSearchRecyclerview.adapter = searchAdapter
            binding.foodPreferenceSearchRecyclerview.layoutManager =
                LinearLayoutManager(context)
        }


        foodPreferenceAdapter = FoodPreferenceAdapter(object : FoodPreferenceClickListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun deleteBtnClick(foodPreference: FoodPreference, idx: Int) {
                deleteFoodPreferenceItem(
                    foodPreference.foodName,
                    foodPreference.classification,
                    idx
                )
            }
        })


        binding.foodPreferenceRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = foodPreferenceAdapter
            Log.d(TAG, "setAdapters foodList: ${mainViewModel.foodPreferenceList.value}")
        }

        existBottomSheetDialog = BottomSheetDialog(context)
    }

    fun checkTicketEmpty(): Boolean {
        if (mainViewModel.userInformation.value?.foodTicket!! > 0) {
            return false
        } else if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
            return false
        }
        return true
    }

    fun emptyTicketShowDialog() {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )
        val bottomSheetDialog = BottomSheetDialog(context)
        bindingDialog.viewModel = mainViewModel

        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            Log.d(TAG, "favoriteItemClick: 결제 화면 띄우기")
            findNavController().navigate(R.id.ticketPurchaseFragment)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun setTextBold(
        text: String,
        foodName: String,
        classification: String
    ): SpannableString {
        val spannableString = SpannableString(text)

        // 변수 부분을 볼드체로 설정
        val startIndex = text.indexOf("[$foodName]")
        val endIndex = startIndex + foodName.length + 2 // 변수를 감싸는 [] 기호를 제외한 길이
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val startIndex2 = text.indexOf("[$classification]")
        val endIndex2 = startIndex2 + classification.length + 2
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex2,
            endIndex2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    fun existFreeTicketShowDialog(foodName: String, classfication: String) {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )

        val foodTicket = mainViewModel.userInformation.value?.freeFoodTicket.toString()
        Log.d(TAG, "existFreeTicketShowDialog: 호출")
        existBottomSheetDialog.setContentView(bindingDialog.root)
        bindingDialog.dialogTicketBottomFoodTicket.text = "무료 음식 티켓 $foodTicket 개"
        bindingDialog.dialogTicketBottomTranslationTicket.visibility = View.GONE
        bindingDialog.dialogTicketBottomButton.text = "등록하기"
        bindingDialog.dialogTicketBottomText.text =
            setTextBold("[$foodName]를 [$classfication] 음식으로\n등록하시겠습니까?", foodName, classfication)
        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            existBottomSheetDialog.dismiss()
        }
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.foodPreferenceRegister(foodName, classfication)
            }
        }

        existBottomSheetDialog.show()
    }

    fun existTicketShowDialog(foodName: String, classfication: String) {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )
        bindingDialog.viewModel = mainViewModel
        existBottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.dialogTicketBottomButton.text = "등록하기"

        bindingDialog.dialogTicketBottomText.text =
            setTextBold(
                "[$foodName]를 [$classfication] 음식으로\n등록하시겠습니까?",
                foodName, classfication
            )

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            existBottomSheetDialog.dismiss()
        }
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.foodPreferenceRegister(foodName, classfication)
            }
        }

        existBottomSheetDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("NotifyDataSetChanged")
    fun deleteFoodPreferenceItem(foodName: String, classification: String, idx: Int) {
        val dialog = Dialog(context)
        val binding: DialogDeletePreferenceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_delete_preference,
            null,
            false
        )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val size = mainViewModel.getDisplaySize(0.9f, 0.3f)
        dialog.window!!.setLayout(size.first, size.second)

        binding.dialogDeletePreferenceWarning.text = setTextBold(
            "[${foodName}]를 [${classification}] 음식에서 \n삭제 하시겠습니까?",
            foodName,
            classification
        )
        binding.dialogDeletePreferenceDeleteButton.setOnClickListener {
            Log.d(TAG, "deleteFoodPreferenceItem: 호출")
            mainViewModel.deletedResult.observe(viewLifecycleOwner) { result ->
                if (result) {
                    dialog.dismiss()
                    mainViewModel.foodPreferenceList.value?.foodList?.removeAt(idx)
                    foodPreferenceAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            mainViewModel.deleteFoodPreference(foodName)
        }
        binding.dialogDeletePreferenceCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showCustomDialog() {
        val dialog = Dialog(context)
        val bindingDialog: DialogWarningBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_warning,
            null,
            false
        )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.dialogTitle.text = "검색 결과 없음"
        bindingDialog.dialogContent.text = "검색 결과가 없습니다. \n추후 업데이트 예정 혹은 없는 음식입니다."
        dialog.show()
    }

    private fun setListener() {

        binding.foodPreferenceSearchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: $query")
                if (query != null) {
                    mainViewModel.searchFood(query)
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $newText")
                if (newText?.isEmpty() == true) {
                    binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
                    binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                    mainViewModel.searchFood.value?.clear()
                }
                return true
            }
        })

    }

    private fun observeLiveData() {

        mainViewModel.foodPreferenceList.observe(viewLifecycleOwner) {
            when (it.result) {
                "success" -> {
                    Log.d(TAG, "init foodPreference: ${it.foodList}")
                    foodPreferenceAdapter.setFoodList(it.foodList!!)
                    binding.foodPreferenceEmptyListWarring.visibility = View.GONE
                }

                "notRegister" -> {
                    Log.d(TAG, "init not register")
                    binding.foodPreferenceEmptyListWarring.visibility = View.VISIBLE
                    binding.foodPreferenceRecyclerview.visibility = View.INVISIBLE
                }

                else -> {
                    Log.d(TAG, "null 호출")
                }
            }
        }

        mainViewModel.searchFood.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.foodPreferenceSearchRecyclerview.visibility = View.VISIBLE
                binding.foodPreferenceRecyclerview.visibility = View.GONE
                searchAdapter.setFoodList(it)
            } else {
                Log.d(TAG, "init: 실패")
                showCustomDialog()
            }
        }

        mainViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "existTicketShowDialog: $result")
            if (result) {
                existBottomSheetDialog.dismiss()
                lifecycleScope.launch {
                    if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
                        mainViewModel.updateTicketQuantity("free_food_ticket", "-", 1)
                    } else {
                        mainViewModel.updateTicketQuantity("food_ticket", "-", 1)
                    }

                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

                    binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                    binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
                    mainViewModel.getFoodPreference()
                }

                mainViewModel.registerVariableReset()

            } else {
                Log.d(TAG, "existTicketShowDialog: 실패")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
        binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
