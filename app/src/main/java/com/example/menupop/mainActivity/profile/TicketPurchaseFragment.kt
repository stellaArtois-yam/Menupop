package com.example.menupop.mainActivity.profile

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
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
import androidx.appcompat.app.AppCompatActivity

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.menupop.R
import com.example.menupop.databinding.DialogPaymentRegularBinding
import com.example.menupop.databinding.DialogPaymentRewardBinding
import com.example.menupop.databinding.DialogSelectPaymentTypeBinding
import com.example.menupop.databinding.FragmentTicketPurchaseBinding
import com.example.menupop.mainActivity.MainActivityEvent
import com.example.menupop.mainActivity.MainActivityViewModel

class TicketPurchaseFragment : Fragment() {
    val TAG = "TicketPurchaseFragment"
    lateinit var binding: FragmentTicketPurchaseBinding
    private lateinit var ticketPurchaseViewModel: MainActivityViewModel
    var event: MainActivityEvent? = null
    private lateinit var context: Context
    var identifier : Int ?= null

    private lateinit var sharedPreferences : SharedPreferences

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
            //receivedReward가 0초과면 paymentTypeDialog
            Log.d(TAG, "haveReworded: ${ticketPurchaseViewModel.haveRewarded.value}")
            if(ticketPurchaseViewModel.haveRewarded.value != 0){
                paymentTypeDialog()
            }else{
                //0이면 바로 카카오페이(paymentRegularDialog) 호출
                paymentRegularDialog()
            }



        }

        binding.translationTicketPurchaseButton.setOnClickListener {

            if(ticketPurchaseViewModel.haveRewarded.value != 0){
                paymentTypeDialog()
            }else{
                paymentRegularDialog()
            }

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
            ticketPurchaseViewModel.updatePaymentType("reward")
            dialog.dismiss()
            paymentRewardDialog()
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
            Log.d(TAG, "paymentRegularDialog: ?!?!??")

        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentRewardDialog() {
        val dialogReward = Dialog(context)

        val dataBindingReward: DialogPaymentRewardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_payment_reward,
            null,
            false
        )

        dataBindingReward.ticketPurchaseViewModel = ticketPurchaseViewModel
        dataBindingReward.lifecycleOwner = this



        dialogReward.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogReward.setContentView(dataBindingReward.root)
        dialogReward.show()

        ticketPurchaseViewModel.isRewardExceeded.observe(viewLifecycleOwner, Observer {
            if(it){
                dataBindingReward.rewardTicketPurchaseWarning.visibility = View.VISIBLE
            }else{
                dataBindingReward.rewardTicketPurchaseWarning.visibility = View.GONE
            }

        })

        ticketPurchaseViewModel.changeTicket.observe(viewLifecycleOwner, Observer {
            if(it){
                sharedPreferences = context.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
                ticketPurchaseViewModel.setRewarded(sharedPreferences)

                dialogReward.dismiss()
                event?.completePayment()
            }
        })


        dataBindingReward.paymentRewardCancel.setOnClickListener {
            dialogReward.dismiss()
        }

        dataBindingReward.rewardTranslationTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.addTranslationTicketReward()
        }

        dataBindingReward.rewardTranslationTicketPurchaseMinusButton.setOnClickListener{
            Log.d(TAG, "paymentRewardDialog: click")
            ticketPurchaseViewModel.removeTranslationTicketReward()
        }

        dataBindingReward.rewardFoodTicketPurchasePlusButton.setOnClickListener{
            Log.d(TAG, "paymentRewardDialog: click")
            ticketPurchaseViewModel.addFoodTicketReward()
        }

        dataBindingReward.rewardFoodTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.removeFoodTicketReward()
        }

        dataBindingReward.rewardTicketPurchaseButton.setOnClickListener {
            ticketPurchaseViewModel.rewardPayment(identifier!!)

        }
    }
}