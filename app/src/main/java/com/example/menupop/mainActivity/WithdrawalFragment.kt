package com.example.menupop.mainActivity

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.FragmentWithdrawalBinding
@RequiresApi(Build.VERSION_CODES.O)

class WithdrawalFragment : Fragment() {
    val TAG = "WithdrawalFragment"
    lateinit var binding : FragmentWithdrawalBinding
    private lateinit var withdrawalViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private lateinit var context: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if (context is MainActivityEvent) {
            event = context
            Log.d(TAG, "onAttach: 호출")

        } else {
            throw RuntimeException(
                context.toString()
                        + "must implement MainActivityEvent"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdrawal, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        clickListener()
    }

    fun init(){
        withdrawalViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.withdrawalViewModel = withdrawalViewModel
        binding.lifecycleOwner = this

        withdrawalViewModel.accountWithdrawal.observe(viewLifecycleOwner){
            event?.logout()
        }

    }

    fun clickListener(){
        binding.withdrawalAgree.setOnClickListener {
            if(binding.withdrawalCheckBox.isChecked){
                showDialog()
            }else{
                Toast.makeText(context,"주의 사항에 동의해주세요", Toast.LENGTH_LONG).show()
            }
        }

        binding.withdrawalDisagree.setOnClickListener {
            event?.moveToProfile()
        }


    }

    fun showDialog(){
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_two_button)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()

        dialog.findViewById<TextView>(R.id.dialog_two_button_title).text = "회원 탈퇴"
        dialog.findViewById<TextView>(R.id.dialog_two_button_content).text = "정말로 탈퇴하시겠어요?"

        val agreeButton = dialog.findViewById<Button>(R.id.dialog_two_button_agree)
        agreeButton.text = "떠날래요"


        agreeButton.setOnClickListener{
            dialog.dismiss()
            //회원 탈퇴
            var sharedPreferences = context.getSharedPreferences("userInfo",
                AppCompatActivity.MODE_PRIVATE)
                withdrawalViewModel.withDrawal(sharedPreferences)

        }
        val disagreeButton = dialog.findViewById<Button>(R.id.dialog_two_button_disagree)
        disagreeButton.text = "더 써볼래요"

        disagreeButton.setOnClickListener{
            dialog.dismiss()
        }
    }

}