package com.elnico.winwintesttask

import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieSyncManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.PluginState
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.elnico.winwintesttask.databinding.FragmentGameBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class GameFragment: Fragment() {

    private lateinit var binding: FragmentGameBinding

    private val gameViewModel: GameViewModel by viewModel()

    companion object {
        private const val TAG = "GameScreen"

        private const val WEBGL_OPTIMAL_WIDTH = 1920
        private const val WEBGL_OPTIMAL_HEIGHT = 1080
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGameWebView(binding.webView)

        lifecycleScope.launch {
            gameViewModel.responseFlow.filterNotNull().collect {
                val isHtmlPageContent = it.second

                if (isHtmlPageContent) {
                    binding.webView.loadDataWithBaseURL(null, it.first, "text/html", "utf-8", null)
                    //binding.webView.loadData(it.first, "text/html; charset=utf-8", "UTF-8")
                } else {
                    resizeWebViewToOptimalSize()
                    binding.webView.loadUrl(it.first)
                }
            }
        }

        gameViewModel.loadInitialConfig()
    }

    private fun resizeWebViewToOptimalSize() {
        val params = binding.webView.layoutParams as ConstraintLayout.LayoutParams
        params.width = WEBGL_OPTIMAL_WIDTH
        params.height = WEBGL_OPTIMAL_HEIGHT
        binding.webView.layoutParams = params

        val metrics = resources.displayMetrics

        val scalingFactor = (metrics.heightPixels / WEBGL_OPTIMAL_HEIGHT).toFloat()

        binding.webView.scaleX = scalingFactor
        binding.webView.scaleY = scalingFactor
    }

    private fun setupGameWebView(webView: WebView) {
        webView.apply {
            //settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; sdk_gphone64_x86_64 Build/TE1A.220922.021) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.71 Mobile Safari/537.36 Client/Android"
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5507.209 Safari/537.36"

            settings.javaScriptEnabled = true
            settings.setSupportZoom(false)

            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            //settings.javaScriptCanOpenWindowsAutomatically = true
            //settings.setSupportMultipleWindows(true)

            //setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //setScrollbarFadingEnabled(true);
            //setInitialScale(100)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()

                    Log.v(TAG, "Now loading $url")

                    view?.loadUrl(url)

                    return false
                }

                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    CookieSyncManager.getInstance().sync()

                    //view!!.loadUrl("javascript:(myWindow = window.open(\"\", \"\", \"width=200, height=100\");myWindow.resizeTo(300, 300);)()")

                    //view!!.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");


                    //super.onPageFinished(view, url);
                }
            }

            webChromeClient = object : WebChromeClient() {

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val newWebView = WebView(requireContext())
                    setupGameWebView(newWebView)

                    newWebView.settings.pluginState = PluginState.ON
                    view!!.addView(newWebView)

                    val transport = resultMsg!!.obj as WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()

                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    requireActivity().runOnUiThread {
                        request?.grant(request.resources)
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    /*this@AppActivity.filePathCallback = filePathCallback

                    fileChooserParams?.let {
                        val intent: Intent = fileChooserParams.createIntent()
                        try {
                            startActivityForResult(intent, 5909)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            return false
                        }
                    }*/

                    return true
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
