package com.example.menupop.mainActivity.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.databinding.FragmentWithdrawalBinding
import com.example.menupop.login.LoginActivity
import com.example.menupop.mainActivity.MainActivityViewModel

@RequiresApi(Build.VERSION_CODES.O)

class WithdrawalFragment : Fragment() {
    companion object{
        const val TAG ="WithdrawalFragment"
    }
    private var _binding : FragmentWithdrawalBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainActivityViewModel
    private lateinit var context: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdrawal, container, false)
        binding.withdrawalViewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.accountWithdrawal.observe(viewLifecycleOwner){
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
        clickListener()
    }


    private fun clickListener(){
        binding.withdrawalAgree.setOnClickListener {
            if(binding.withdrawalCheckBox.isChecked){
                showDialog()
            }else{
                Toast.makeText(context,"주의 사항에 동의해주세요", Toast.LENGTH_LONG).show()
            }
        }

        binding.withdrawalDisagree.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun showDialog(){
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val size = mainViewModel.getDisplaySize(0.9f, 0.3f)
        dialog.window!!.setLayout(size.first, size.second)
        dialog.show()

        dialog.findViewById<TextView>(R.id.dialog_two_button_title).text = "회원 탈퇴"
        dialog.findViewById<TextView>(R.id.dialog_two_button_content).text = "정말로 탈퇴하시겠어요?"

        val agreeButton = dialog.findViewById<Button>(R.id.dialog_two_button_agree)
        agreeButton.text = "떠날래요"


        agreeButton.setOnClickListener{
            dialog.dismiss()
            //회원 탈퇴
            val sharedPreferences = context.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
            mainViewModel.withdrawal(sharedPreferences)

        }
        val disagreeButton = dialog.findViewById<Button>(R.id.dialog_two_button_disagree)
        disagreeButton.text = "더 써볼래요"

        disagreeButton.setOnClickListener{
            dialog.dismiss()
        }
    }

}