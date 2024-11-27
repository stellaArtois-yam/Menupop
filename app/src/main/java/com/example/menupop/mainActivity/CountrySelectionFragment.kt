package com.example.menupop.mainActivity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.DialogTicketBottomBinding
import com.example.menupop.mainActivity.translation.CameraActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


class CountrySelectionFragment : Fragment() {

    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        return inflater.inflate(R.layout.fragment_country_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        observeData()
    }

    private fun setListener() {

        view?.findViewById<TextView>(R.id.country_selection_others)?.setOnClickListener {
            startTranslation("others")
        }
        view?.findViewById<TextView>(R.id.country_selection_japan)?.setOnClickListener {
            startTranslation("japan")
        }

        view?.findViewById<TextView>(R.id.country_selection_china)?.setOnClickListener {
            startTranslation("china")
        }

        view?.findViewById<TextView>(R.id.country_selection_taiwan)?.setOnClickListener {
            startTranslation("taiwan")
        }
    }

    private fun observeData() {
        if (!mainViewModel.checkingTranslationTicket()) {
            emptyTicketShowDialog()
        }
    }

    private fun startTranslation(country : String){
        val intent = Intent(requireContext(), CameraActivity::class.java)
        intent.putExtra("foodPreference", mainViewModel.foodPreferenceList.value?.foodList)
        intent.putExtra("country", country)
        startActivity(intent)
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

}
