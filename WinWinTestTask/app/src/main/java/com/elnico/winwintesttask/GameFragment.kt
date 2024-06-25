package com.elnico.winwintesttask

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieSyncManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.PluginState
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.elnico.winwintesttask.databinding.FragmentGameBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class GameFragment: Fragment() {

    private var shouldCacheTrackerLink = true

    private lateinit var binding: FragmentGameBinding

    private lateinit var fallbackURL: String

    private val gameViewModel: GameViewModel by viewModel()

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, data))
        }
    }

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

        lifecycleScope.launch {
            gameViewModel.configFlow.filterNotNull().collect {
                val appsflyerApiKey = it["appsflyer_api_key"].orEmpty()
                val onesignalApiKey = it["onesignal_api_key"].orEmpty()
                val trackerBaseUrl = it["tracker_base_url"].orEmpty()
                val trackerKey = it["tracker_key"].orEmpty()
                val fallbackUrl = it["fallback_url"].orEmpty()
                val advertisingId = it["advertisingId"].orEmpty()
                val appsflyerId = it["appsflyerId"].orEmpty()
                val siteId = it["siteId"].orEmpty()
                val version = it["version"].orEmpty()

                this@GameFragment.fallbackURL = fallbackUrl
                (requireActivity().application as GameApplication).setupAnalytics(appsflyerApiKey, onesignalApiKey)

                gameViewModel.fetchTrackerVisitedStatus {
                    shouldCacheTrackerLink = it == null

                    setupGameWebView(binding.webView)

                    if (it != null && it.contains("jsontest")) {
                        resizeWebViewToOptimalSize()
                        binding.webView.settings.userAgentString =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5507.209 Safari/537.36"
                        binding.webView.loadUrl(fallbackUrl)
                    } else if (it != null) {
                        binding.webView.loadUrl(it)
                    } else {
                        val appUUID = (requireActivity().application as GameApplication).appUUID
                        binding.webView.loadUrl("$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}")
                    }
                }
            }
        }

        gameViewModel.fetchConfig()
    }

    private fun resizeWebViewToOptimalSize() {
        val params = binding.webView.layoutParams as ConstraintLayout.LayoutParams
        params.width = WEBGL_OPTIMAL_WIDTH
        params.height = WEBGL_OPTIMAL_HEIGHT
        binding.webView.layoutParams = params

        val metrics = resources.displayMetrics

        val scalingFactor = (metrics.heightPixels.toFloat() / WEBGL_OPTIMAL_HEIGHT.toFloat())

        binding.webView.scaleX = scalingFactor
        binding.webView.scaleY = scalingFactor
    }

    private fun setupGameWebView(webView: WebView) {

        webView.apply {
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true

            //settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; sdk_gphone64_x86_64 Build/TE1A.220922.021) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.71 Mobile Safari/537.36 Client/Android"
            //settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5507.209 Safari/537.36"

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

                    if (shouldCacheTrackerLink) {
                        gameViewModel.updateTrackerVisitedStatus(url) {
                            shouldCacheTrackerLink = false
                        }
                    }

                    if (url.contains("jsontest")) {

                        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5507.209 Safari/537.36"
                        binding.webView.loadUrl(fallbackURL)
                        //view?.loadUrl(url)
                    } else {
                        view?.loadUrl(url)
                    }

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
                }
            }

            webChromeClient = object : WebChromeClient() {

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    println(consoleMessage)
                    return super.onConsoleMessage(consoleMessage)
                }

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
                    this@GameFragment.filePathCallback = filePathCallback

                    fileChooserParams?.let {
                        val intent: Intent = fileChooserParams.createIntent()
                        try {
                            resultLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            return false
                        }
                    }

                    return true
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE

                    if (newProgress == 100) {
                        binding.dummyView.visibility = View.GONE
                    }
                }
            }
        }
    }
}
