package com.example.menupop.mainActivity.profile

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
    companion object {
        const val TAG = "WebViewFragment"
    }

    private var webView: WebView? = null
    private lateinit var ticketPurchaseViewModel: MainActivityViewModel

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
            val url = it?.nextRedirectAppUrl
            if (url != null) {
                Log.d(TAG, "mobile url: $url")
                webView?.loadUrl(url)
            }
        }

        //티켓 구매 실패(결제 x)
        ticketPurchaseViewModel.isFailedToBuyTicket.observe(viewLifecycleOwner){
            if(it == true){
                completePayment()
                Toast.makeText(requireContext(), resources.getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }else if(it == false){
                completePayment()
            }
        }

        ticketPurchaseViewModel.cancelBuyTicket.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(requireContext(), "시스템 오류로 결제가 취소되었습니다 :(", Toast.LENGTH_LONG).show()
                completePayment()
            }else if(it == false){
                Toast.makeText(requireContext(), "결제가 완료되었으나 시스템 오류가 발생했습니다. 관리자에게 문의해 주세요.", Toast.LENGTH_LONG).show()
                completePayment()
            }
        }
    }

    inner class MyWebViewClient(val viewModel: MainActivityViewModel) : WebViewClient() {

        val TAG = "WebViewFragment"

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
                        return true
                    }
                } catch (e: URISyntaxException) {
                    Log.d(TAG, "exception: ${e.message}")
                    Toast.makeText(requireContext(), resources.getString(R.string.network_error), Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.profileFragment)
                }

            }

            pgToken = url.substringAfter("pg_token=")
            Log.d(TAG, "pgToken: $pgToken")

            if (pgToken != null) {
                viewModel.updatePgToken(pgToken!!) // 결제 요청
            }

            return false
        }
    }

    private fun completePayment() {
        Log.d(MainActivityViewModel.TAG, "completePayment: 호출")
        ticketPurchaseViewModel.initializeKakaoPayVariables()
        findNavController().navigate(R.id.profileFragment)

    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }


}