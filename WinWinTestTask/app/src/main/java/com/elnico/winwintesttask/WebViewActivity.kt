package com.elnico.winwintesttask

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieSyncManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

open class WebViewActivity: AppCompatActivity() {

    protected lateinit var webView: WebView
    protected var splashView: View? = null

    protected lateinit var fallbackURL: String

    protected var shouldCacheTrackerLink = true

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    protected lateinit var preferences: SharedPreferences

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, data))
        }
    }

    companion object {
        private const val TAG = "WebViewScreen"

        const val TRACKER_VISITED = "is_tracker_visited"

        private const val WEBGL_OPTIMAL_WIDTH = 1920
        private const val WEBGL_OPTIMAL_HEIGHT = 1080
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()

        preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    protected fun forceScreenOrientation(orientation: Int) {
        requestedOrientation = orientation
    }

    protected fun setupGameWebView(webView: WebView, onProgressChanged: (Int) -> Unit) {

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
                    var url = request?.url.toString()
                    /*if (url.contains("jsontest")) {
                        url = "https://land2getluck.com/l/6660c1aa2ef39e264f0a91cc?click_id=31515315&sub_id=smirnov"
                    }*/

                    Log.v(TAG, "Now loading $url")

                    if (shouldCacheTrackerLink) {
                        preferences.edit().putString(TRACKER_VISITED, url).apply()
                        shouldCacheTrackerLink = false
                    }

                    if (url.contains("jsontest")) {
                        splashView?.visibility = View.VISIBLE

                        /**
                         * Заглушка
                         */
                        forceScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                        webView.loadUrl(fallbackURL)
                    } else {
                        /**
                         * Оффер
                         */
                        if (this@WebViewActivity is GameActivity) {
                            webView.loadUrl(url)
                        } else {
                            navigateToContents(
                                url,
                                false,
                                fallbackURL,
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            )
                        }
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
                    val newWebView = WebView(this@WebViewActivity)
                    setupGameWebView(newWebView, onProgressChanged)

                    newWebView.settings.pluginState = WebSettings.PluginState.ON
                    view!!.addView(newWebView)

                    val transport = resultMsg!!.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()

                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    runOnUiThread {
                        request?.grant(request.resources)
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    this@WebViewActivity.filePathCallback = filePathCallback

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
                    onProgressChanged(newProgress)
                }
            }
        }
    }

    protected fun navigateToContents(url: String, shouldCacheTrackerLink: Boolean, fallbackUrl: String, orientation: Int?) {
        val intent = Intent(this, GameActivity::class.java)

        intent.putExtra("url", url)
        intent.putExtra("shouldCacheTrackerLink", shouldCacheTrackerLink)
        intent.putExtra("fallbackUrl", fallbackUrl)

        if (orientation != null) {
            intent.putExtra("orientation", orientation)
        }

        startActivity(intent)
        finish()
    }

    private fun hideStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)



        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide nav bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // Hide status bar
                )
    }
}