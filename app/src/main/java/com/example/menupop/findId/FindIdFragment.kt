package com.example.menupop.findId

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.FragmentFindIdBinding
import kotlinx.coroutines.launch

class FindIdFragment : Fragment() {

    private var _binding : FragmentFindIdBinding? = null
    private val binding get() = _binding!!
    private lateinit var findIdViewModel: FindIdViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        findIdViewModel = ViewModelProvider(requireActivity())[FindIdViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_id, container, false)
        binding.findIdViewModel = findIdViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setListener()
    }

    private fun setObservers() {

        findIdViewModel.userIdExistence.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.findIdResultFragment)
        }

    }

    private fun setListener() {

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        (activity as FindIdActivity).toolbar!!.setNavigationOnClickListener {
             requireActivity().finish()
        }

        binding.findIdEmail.addTextChangedListener {
            val domainSelection = binding.findIdEmailSelection.selectedItem.toString()
            val emailId = binding.findIdEmail.text.toString()

            findIdViewModel.checkEmailForm(emailId, domainSelection)
        }

        binding.findIdEmailSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val domainSelection = parent.getItemAtPosition(position).toString()
                    val emailId = binding.findIdEmail.text.toString()

                    if (domainSelection != "선택") {
                        findIdViewModel.checkEmailForm(emailId, domainSelection)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        binding.findIdConfirmButton.setOnClickListener {

            if (findIdViewModel.checkEmailForm.value == true) {
                val emailId = binding.findIdEmail.text.toString()
                val domainSelection = binding.findIdEmailSelection.selectedItem.toString()
                lifecycleScope.launch {
                    findIdViewModel.checkUserId(emailId, domainSelection)
                }
            }else{
                Toast.makeText(requireContext(), R.string.email_rule_warning, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}