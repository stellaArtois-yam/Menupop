package com.example.menupop.mainActivity.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.menupop.R
import com.example.menupop.mainActivity.MainActivityViewModel
import java.net.URISyntaxException

class KakaoPayWebView : Fragment() {
    companion object{
        const val TAG = "WebViewFragment"
    }
    private var webView: WebView? = null
    private lateinit var ticketPurchaseViewModel: MainActivityViewModel
    private lateinit var context: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.webview, container, false)

        webView = view.findViewById(R.id.webview)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ticketPurchaseViewModel =
            ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        val myWebViewClient = MyWebViewClient(ticketPurchaseViewModel)
        webView?.settings?.javaScriptEnabled = true
        webView?.webViewClient = myWebViewClient

        ticketPurchaseViewModel.paymentReady.observe(viewLifecycleOwner) {
            if (it != null) {
                val url = it.next_redirect_app_url
                Log.d(TAG, "mobile url: $url")
                webView?.loadUrl(url)
            }
        }


    }

    inner class MyWebViewClient(val viewModel: MainActivityViewModel) : WebViewClient() {

        val TAG = "MyWebViewClient"

        private var pgToken: String? = null

        @RequiresApi(Build.VERSION_CODES.O)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {


            val url = request!!.url.toString()
            Log.d(TAG, "urlLoading: $url")


            if (url.startsWith("intent://")) {

                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    intent?.let {
                        view?.context?.startActivity(it)
                        Log.d(TAG, "?")
                        return true
                    }
                } catch (e: URISyntaxException) {
                    Log.d(TAG, "exception: ${e.message}")
                }

            }

            pgToken = url.substringAfter("pg_token=")
            Log.d(TAG, "pgToken: $pgToken")

            if (pgToken != null) {
                viewModel.updatePgToken(pgToken!!)
            }


            if (url.contains("KakaoPayApprove")) {
                //여기서 결제 완료 observe해서 update 되면 이동
                viewModel.changeTicket.observe(viewLifecycleOwner) {
                    Log.d(TAG, "changeTicket: $it")
                    if (it == "success") {
                        completePayment()
                    } else if (it == "failed") {
                        Toast.makeText(requireContext(), "결제 실패로 결제가 취소되었습니다 :(", Toast.LENGTH_LONG)
                            .show()
                        completePayment()
                    }
                }

                Log.d(TAG, "webView Clear")

            } else if (url.contains("KakaoPayCancel")) {
                viewModel.setPaymentResponse()
                completePayment()

            } else if (url.contains("KakaoPayFail")) {
                viewModel.setPaymentResponse()
                completePayment()
            }

            view!!.loadUrl(url)
            return false
        }
    }

    private fun completePayment(){
        ticketPurchaseViewModel.setChangeTicket()
        findNavController().navigate(R.id.profileFragment)
    }


}