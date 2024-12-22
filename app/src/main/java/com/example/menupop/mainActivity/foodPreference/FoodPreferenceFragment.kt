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
import android.widget.TextView
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
    private lateinit var searchAdapter: FoodPreferenceSearchAdapter
    private lateinit var foodPreferenceAdapter: FoodPreferenceAdapter
    private lateinit var existBottomSheetDialog: BottomSheetDialog
    private lateinit var deleteDialog : Dialog
    private var index : Int? = null


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
            override fun itemClick(foodName: String, isFavorite: Boolean) {
                if (mainViewModel.checkFoodTicketEmpty()) {
                    emptyTicketDialog()
                } else {
                    val preference = if(isFavorite) "호" else "불호"
                    existTicketDialog(foodName, preference)
                }
            }
        })


        binding.foodPreferenceSearchRecyclerview.apply {
            binding.foodPreferenceSearchRecyclerview.adapter = searchAdapter
            binding.foodPreferenceSearchRecyclerview.layoutManager =
                LinearLayoutManager(requireContext())
        }


        foodPreferenceAdapter = FoodPreferenceAdapter(object : FoodPreferenceClickListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun deleteBtnClick(foodPreference: FoodPreference, idx: Int) {
                val result : Pair<Dialog, Int> = deleteFoodPreferenceItem(
                    foodPreference.foodName,
                    foodPreference.classification,
                    idx
                )

                deleteDialog = result.first
                deleteDialog.show()

                index = result.second
            }
        })


        binding.foodPreferenceRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodPreferenceAdapter
        }

        existBottomSheetDialog = BottomSheetDialog(requireContext())
    }


    fun emptyTicketDialog() {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        bottomSheetDialog.setContentView(bindingDialog.root)
        mainViewModel.setTicketStatus(false)

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
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


    fun existTicketDialog(foodName: String, classification: String) {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )
        bindingDialog.viewModel = mainViewModel
        existBottomSheetDialog.setContentView(bindingDialog.root)

        if(mainViewModel.userInformation.value!!.freeFoodTicket > 0){
            mainViewModel.setTicketStatus(true)
        } else{
            mainViewModel.setTicketStatus(false)
        }

        bindingDialog.dialogTicketBottomButton.text = "등록하기"

        bindingDialog.dialogTicketBottomText.text =
            setTextBold(
                "[$foodName]를 [$classification] 음식으로\n등록하시겠습니까?",
                foodName, classification
            )

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            existBottomSheetDialog.dismiss()
        }
        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.foodPreferenceRegister(foodName, classification)
            }
        }

        existBottomSheetDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("NotifyDataSetChanged")
    fun deleteFoodPreferenceItem(foodName: String, classification: String, idx: Int) : Pair<Dialog, Int> {
        val dialog = Dialog(requireContext())
        val binding: DialogDeletePreferenceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
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
            mainViewModel.deleteFoodPreference(foodName)
        }

        binding.dialogDeletePreferenceCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        return Pair(dialog, idx)
    }

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_warning)

        dialog.findViewById<TextView>(R.id.title).text = "검색 결과 없음"
        dialog.findViewById<TextView>(R.id.content).text = "검색 결과가 없습니다. \n추후 업데이트 예정 혹은 없는 음식입니다."
        dialog.show()

        binding.foodPreferenceSearchview
    }

    private fun setListener() {

        binding.foodPreferenceSearchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    mainViewModel.searchFood(query)
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
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
                    foodPreferenceAdapter.setFoodList(it.foodList!!)
                    binding.foodPreferenceEmptyListWarring.visibility = View.GONE
                }
                "notRegister" -> {
                    Log.d(TAG, "init not register")
                    binding.foodPreferenceEmptyListWarring.visibility = View.VISIBLE
                    binding.foodPreferenceRecyclerview.visibility = View.INVISIBLE
                }
                else -> Log.d(TAG, "foodPreferenceList is null")
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
            Log.d(TAG, "foodPreferenceRegister: $result")
            when(result){
                true -> {
                    existBottomSheetDialog.dismiss()
                    lifecycleScope.launch {
                        if (mainViewModel.userInformation.value?.freeFoodTicket!! > 0) {
                            mainViewModel.updateTicketQuantity("free_food_ticket", "-", 1)
                        } else {
                            mainViewModel.updateTicketQuantity("food_ticket", "-", 1)
                        }

                        val imm =
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

                        binding.foodPreferenceRecyclerview.visibility = View.VISIBLE
                        binding.foodPreferenceSearchRecyclerview.visibility = View.GONE
                        mainViewModel.getFoodPreference()
                    }
                    mainViewModel.initializeRegisterResult()
                }
                false -> {
                    Toast.makeText(requireContext(), resources.getString(R.string.network_error),Toast.LENGTH_SHORT).show()
                    mainViewModel.initializeRegisterResult()
                }
                else -> Log.d(TAG, "registerResult: $result")
            }
        }

        mainViewModel.deletedResult.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "deleteFoodPreferenceItem: $result")
            when(result){
                true -> {
                    deleteDialog.dismiss()
                    mainViewModel.foodPreferenceList.value?.foodList?.removeAt(index!!)
                    foodPreferenceAdapter.notifyDataSetChanged()
                    mainViewModel.initializeDeleteResult()
                }
                false -> {
                    deleteDialog.dismiss()
                    Toast.makeText(requireContext(), resources.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    mainViewModel.initializeDeleteResult()
                }
                else -> { Log.d(TAG, "deleteFoodPreferenceItem is null")}
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
