package com.example.menupop.mainActivity

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.example.menupop.R
import com.example.menupop.databinding.DialogPaymentRegularBinding
import com.example.menupop.databinding.DialogSelectPaymentTypeBinding
import com.example.menupop.databinding.FragmentProfileBinding
import com.example.menupop.databinding.FragmentTicketPurchaseBinding
import com.example.menupop.mainActivity.MainActivityEvent

class TicketPurchaseFragment : Fragment() {
    val TAG = "TicketPurchaseFragment"
    lateinit var binding: FragmentTicketPurchaseBinding
    private lateinit var ticketPurchaseViewModel: MainActivityViewModel
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_ticket_purchase, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        clickListener()

    }

    fun init() {
        ticketPurchaseViewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        binding.ticketPurchaseViewModel = ticketPurchaseViewModel
        binding.lifecycleOwner = this


    }

    fun clickListener() {

        binding.foodTicketPurchaseButton.setOnClickListener {
            //다이얼로그를 띄워준다
            paymentTypeDialog()

        }

        binding.translationTicketPurchaseButton.setOnClickListener {
            //다이얼로그를 띄워준다
            paymentTypeDialog()
        }


    }

    fun paymentTypeDialog() {
        val dialog = Dialog(requireActivity())

//        val dialogBinding: DialogSelectPaymentTypeBinding = DataBindingUtil.inflate(
//            LayoutInflater.from(requireActivity()),
//            R.layout.dialog_select_payment_type,
//            null,
//            false
//        )

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_select_payment_type)
        dialog.show()

        val cancel = dialog.findViewById<Button>(R.id.payment_type_cancel)
        cancel.setOnClickListener{
            dialog.dismiss()
        }

        val regular = dialog.findViewById<Button>(R.id.payment_type_regular)
        regular.setOnClickListener{
            dialog.dismiss()
            paymentRegularDialog()
        }

        val reword = dialog.findViewById<Button>(R.id.payment_type_reword)
        reword.setOnClickListener{
            dialog.dismiss()
            paymentRewordDialog()
        }

//        dialogBinding.paymentTypeCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialogBinding.paymentTypeRegular.setOnClickListener {
//            dialog.dismiss()
//            paymentRegularDialog()
//        }
//
//        dialogBinding.paymentTypeReword.setOnClickListener {
//            dialog.dismiss()
//            paymentRewordDialog()
//        }

    }

    fun paymentRegularDialog() {

        Log.d(TAG, "paymentRegularDialog: 호출")

//        val dataBinding: DialogPaymentRegularBinding = DataBindingUtil.inflate(
//            LayoutInflater.from(requireActivity()),
//            R.layout.dialog_payment_regular,
//            null,
//            false
//        )

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_regular)
        dialog.show()

        val cancel = dialog.findViewById<Button>(R.id.payment_regular_cancel)
        cancel.setOnClickListener{dialog.dismiss()}

        dialog.findViewById<ImageView>(R.id.regular_translation_ticket_purchase_plus_button).setOnClickListener{
            ticketPurchaseViewModel.addTranslationTicket()
        }

        dialog.findViewById<ImageView>(R.id.regular_translation_ticket_purchase_minus_button).setOnClickListener{
            ticketPurchaseViewModel.removeTranslationTicket()
        }


    }

    fun paymentRewordDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_reword)
        dialog.show()
    }
}