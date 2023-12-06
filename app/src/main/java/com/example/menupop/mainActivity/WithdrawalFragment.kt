package com.example.menupop.mainActivity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.menupop.R

class WithdrawalFragment : Fragment() {
    private lateinit var withdrawalViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private lateinit var context: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_withdrawal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        }
        val disagreeButton = dialog.findViewById<Button>(R.id.dialog_two_button_disagree)
        disagreeButton.setBackgroundColor(resources.getColor(R.color.pale_gray))
        disagreeButton.setTextColor(resources.getColor(R.color.pale_gray))


        disagreeButton.setOnClickListener{
            dialog.dismiss()
        }
    }

}