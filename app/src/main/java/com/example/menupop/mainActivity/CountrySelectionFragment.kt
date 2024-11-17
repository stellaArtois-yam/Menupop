package com.example.menupop.mainActivity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.databinding.FragmentCountrySelectionBinding
import com.example.menupop.mainActivity.translation.CameraActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


class CountrySelectionFragment : Fragment() {

    private var _binding: FragmentCountrySelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_country_selection,
            container,
            false
        )
        binding.mainActivityViewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.countrySelection.observe(viewLifecycleOwner) {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            intent.putExtra("foodPreference", mainViewModel.foodPreferenceList.value?.foodList)
            intent.putExtra("country", it)
            startActivity(intent)
        }
        setListener()
        observeData()
    }

    private fun setListener() {
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

    private fun observeData() {
        if (!mainViewModel.checkingTranslationTicket()) {
            emptyTicketShowDialog()
        }
    }

    private fun emptyTicketShowDialog() {
        val bindingDialog: DialogTicketBottomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_ticket_bottom,
            null,
            false
        )
        bindingDialog.viewModel = mainViewModel
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bindingDialog.root)

        bindingDialog.dialogTicketBottomDown.setOnClickListener {
            findNavController().navigateUp()
            bottomSheetDialog.dismiss()
        }

        bindingDialog.dialogTicketBottomButton.setOnClickListener {
            findNavController().navigate(R.id.ticketPurchaseFragment)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
