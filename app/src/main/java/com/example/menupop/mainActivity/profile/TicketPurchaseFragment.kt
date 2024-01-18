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

        ticketPurchaseViewModel.paymentReady.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                event?.moveToWebView()
            }
        })


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun clickListener() {
        binding.foodTicketPurchaseButton.setOnClickListener {
            Log.d(TAG, "haveRewarded: ${ticketPurchaseViewModel.userInformation.value!!.haveRewarded}")
            if(ticketPurchaseViewModel.userInformation.value!!.haveRewarded > 0){
                paymentTypeDialog()
            }else{
                paymentRegularDialog()
            }



        }

        binding.translationTicketPurchaseButton.setOnClickListener {
            Log.d(TAG, "haveRewarded: ${ticketPurchaseViewModel.userInformation.value!!.haveRewarded}")
            if(ticketPurchaseViewModel.userInformation.value!!.haveRewarded > 0){
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

        dialogBinding.paymentTypeReward.setOnClickListener {
            Log.d(TAG, "reward click")
            ticketPurchaseViewModel.updatePaymentType("reward")
            dialog.dismiss()
            Log.d(TAG, "reward click?")
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

        ticketPurchaseViewModel.updatePaymentType("regular")

        dataBindingRegular.paymentRegularCancel.setOnClickListener{
            dialogRegular.dismiss()
        }

        dataBindingRegular.regularTranslationTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.adjustRegularTicketQuantity("translation", "+")
        }

        dataBindingRegular.regularTranslationTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.adjustRegularTicketQuantity("translation", "-")
        }

        dataBindingRegular.regularFoodTicketPurchasePlusButton.setOnClickListener{
            ticketPurchaseViewModel.adjustRegularTicketQuantity("food", "+")
        }

        dataBindingRegular.regularFoodTicketPurchaseMinusButton.setOnClickListener{
            ticketPurchaseViewModel.adjustRegularTicketQuantity("food", "-")
        }

        dataBindingRegular.regularTicketPurchaseButton.setOnClickListener {
            //카카오페이 결제 요청
            dialogRegular.dismiss()
            ticketPurchaseViewModel.createPaymentRequest()
            Log.d(TAG, "paymentRegularDialog: ?!?!??")

        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentRewardDialog() {
        Log.d(TAG, "paymentRewardDialog start");
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
                dialogReward.dismiss()
                event?.completePayment()
            }
        })


        dataBindingReward.paymentRewardCancel.setOnClickListener {
            dialogReward.dismiss()
        }

        dataBindingReward.rewardTranslationTicketPurchasePlusButton.setOnClickListener{
            Log.d(TAG, "reward translation plus: ${ticketPurchaseViewModel.rewardTranslationAmount.value}")
            ticketPurchaseViewModel.adjustRewardTicketQuantity("translation", "+")
        }

        dataBindingReward.rewardTranslationTicketPurchaseMinusButton.setOnClickListener{
            Log.d(TAG, "reward translation minus: ${ticketPurchaseViewModel.rewardTranslationAmount.value}")
            ticketPurchaseViewModel.adjustRewardTicketQuantity("translation", "-")
        }

        dataBindingReward.rewardFoodTicketPurchasePlusButton.setOnClickListener{
            Log.d(TAG, "reward food plus: ${ticketPurchaseViewModel.rewardFoodAmount.value}")
            ticketPurchaseViewModel.adjustRewardTicketQuantity("food", "+")
        }

        dataBindingReward.rewardFoodTicketPurchaseMinusButton.setOnClickListener{
            Log.d(TAG, "reward food minus: ${ticketPurchaseViewModel.rewardFoodAmount.value}")
            ticketPurchaseViewModel.adjustRewardTicketQuantity("food", "-")
        }

        dataBindingReward.rewardTicketPurchaseButton.setOnClickListener {
            ticketPurchaseViewModel.rewardPayment(identifier!!)

        }
    }
}