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
import androidx.annotation.RequiresApi

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.DialogPaymentRegularBinding
import com.example.menupop.databinding.DialogPaymentRewordBinding
import com.example.menupop.databinding.DialogSelectPaymentTypeBinding
import com.example.menupop.databinding.FragmentTicketPurchaseBinding

class TicketPurchaseFragment : Fragment() {
    val TAG = "TicketPurchaseFragment"
    lateinit var binding: FragmentTicketPurchaseBinding
    private lateinit var ticketPurchaseViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private lateinit var context: Context
    var identifier : Int ?= null

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

        identifier = arguments?.getInt("identifier")
        Log.d(TAG, "Identifier: $identifier")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_ticket_purchase, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        ticketPurchaseViewModel.paymentResponse.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                event?.moveToWebView()
            }
        })


    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentTypeDialog() {
        val dialog = Dialog(context)

        val dialogBinding: DialogSelectPaymentTypeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_select_payment_type,
            null,
            false
        )

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()


        dialogBinding.paymentTypeCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.paymentTypeRegular.setOnClickListener {
            Log.d(TAG, "regular click")
            dialog.dismiss()
            ticketPurchaseViewModel.updatePaymentType("regular")
            paymentRegularDialog()
        }

        dialogBinding.paymentTypeReword.setOnClickListener {
            Log.d(TAG, "reword click")
            ticketPurchaseViewModel.updatePaymentType("reword")
            dialog.dismiss()
            paymentRewordDialog()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentRegularDialog() {

        val dataBindingRegular: DialogPaymentRegularBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_payment_regular,
            null,
            false
        )

        dataBindingRegular.ticketPurchaseViewModel = ticketPurchaseViewModel
        dataBindingRegular.lifecycleOwner = this

        val dialogRegular = Dialog(context)
        dialogRegular.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogRegular.setContentView(dataBindingRegular.root)
        dialogRegular.show()

        dataBindingRegular.paymentRegularCancel.setOnClickListener{
            dialogRegular.dismiss()
        }

        dataBindingRegular.regularTranslationTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.addTranslationTicket()
        }

        dataBindingRegular.regularTranslationTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.removeTranslationTicket()
        }

        dataBindingRegular.regularFoodTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.addFoodTicket()
        }

        dataBindingRegular.regularFoodTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.removeFoodTicket()
        }

        dataBindingRegular.regularTicketPurchaseButton.setOnClickListener {
            //카카오페이 결제 요청
            dialogRegular.dismiss()
            ticketPurchaseViewModel.createPaymentRequest(identifier.toString())

        }



    }

    fun paymentRewordDialog() {
        val dialogReword = Dialog(context)

        val dataBindingReword: DialogPaymentRewordBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_payment_reword,
            null,
            false
        )

        dataBindingReword.ticketPurchaseViewModel = ticketPurchaseViewModel
        dataBindingReword.lifecycleOwner = this



        dialogReword.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogReword.setContentView(dataBindingReword.root)
        dialogReword.show()


        dataBindingReword.paymentRewordCancel.setOnClickListener {
            dialogReword.dismiss()
        }

        dataBindingReword.rewordTranslationTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.addTranslationTicket()
        }

        dataBindingReword.rewordTranslationTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.removeTranslationTicket()
        }

        dataBindingReword.rewordFoodTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.addFoodTicket()
        }

        dataBindingReword.rewordFoodTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.removeFoodTicket()
        }

        dataBindingReword.rewordTicketPurchaseButton.setOnClickListener {
            //가능한 리워드 내에서 결제하기
        }
    }
}