package com.ezt.video.instasaver.screen.home.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.ezt.video.instasaver.base.BaseFragment
import com.ezt.video.instasaver.databinding.FragmentWebViewBinding
import com.ezt.video.instasaver.viewmodel.WebViewViewModel

class WebViewFragment : BaseFragment<FragmentWebViewBinding>(FragmentWebViewBinding::inflate){
    private lateinit var viewModel: WebViewViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWebViewAndButton()
    }

    private fun setUpWebViewAndButton() {
        val myWebView: WebView = binding.webview
        myWebView.settings.javaScriptEnabled=true
        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(myWebView, url)
                Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show()
            }

        }
        myWebView.loadUrl("https://www.instagram.com/accounts/login/")
    }

    private fun getCookieMap(): Map<String,String> {

        val manager = CookieManager.getInstance()
        val map = mutableMapOf<String,String>()

        manager.getCookie("https://www.instagram.com/")?.let {cookies ->
            Log.d("tagg", "Cookies we got: $cookies")
            val typedArray = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (element in typedArray) {
                val split = element.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if(split.size >= 2) {
                    map[split[0]] = split[1]
                } else if(split.size == 1) {
                    map[split[0]] = ""
                }
            }
        }

        return map
    }
}