package com.elnico.winwintesttask

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.elnico.winwintesttask.databinding.ActivityTrackerBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackerActivity : WebViewActivity() {

    private lateinit var binding: ActivityTrackerBinding

    private val trackerViewModel: TrackerViewModel by viewModel()

    companion object {
        private const val TAG = "MainActivity"
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        Log.v(TAG, "Notifications permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            forceScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }

        binding = ActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webView
        splashView = binding.splashView

        /////////////

        if (preferences.contains(TRACKER_VISITED)) {
            val url = preferences.getString(TRACKER_VISITED, null)

            if (url != null) {
                //binding.logo.visibility = View.GONE
                binding.dummyView.setBackgroundColor(Color.BLACK)

                if (url.contains("jsontest")) {
                    splashView?.visibility = View.VISIBLE
                    forceScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                }

                lifecycleScope.launch {
                    trackerViewModel.configFlow.filterNotNull().collect {
                        val fallbackUrl = it["fallback_url"].orEmpty()

                        this@TrackerActivity.fallbackURL = fallbackUrl

                        if (url.contains("jsontest")) {
                            setupGameWebView(binding.webView) { newProgress ->
                                if (newProgress == 100) {
                                    binding.splashView.visibility = View.GONE
                                    binding.progressBar.visibility = View.GONE
                                    binding.dummyView.visibility = View.GONE
                                }
                            }

                            binding.webView.loadUrl(fallbackUrl)
                        } else {
                            navigateToContents(
                                url = if (url.contains("jsontest")) fallbackUrl else url,
                                shouldCacheTrackerLink = false,
                                fallbackUrl = fallbackUrl,
                                orientation = if (url.contains("jsontest")) {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            )
                        }
                    }
                }

                trackerViewModel.fetchConfig()
            }
        } else {
            shouldCacheTrackerLink = true

            lifecycleScope.launch {
                trackerViewModel.configFlow.filterNotNull().collect {
                    val appsflyerApiKey = it["appsflyer_api_key"].orEmpty()
                    val onesignalApiKey = it["onesignal_api_key"].orEmpty()
                    val trackerBaseUrl = it["tracker_base_url"].orEmpty()
                    val trackerKey = it["tracker_key"].orEmpty()
                    val fallbackUrl = it["fallback_url"].orEmpty()
                    val advertisingId = it["advertisingId"].orEmpty()
                    val appsflyerId = it["appsflyerId"].orEmpty()
                    val siteId = it["siteId"].orEmpty()
                    val version = it["version"].orEmpty()

                    this@TrackerActivity.fallbackURL = fallbackUrl

                    (application as GameApplication).setupAnalytics(appsflyerApiKey, onesignalApiKey)

                    setupGameWebView(binding.webView) { newProgress ->
                        binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE

                        if (newProgress == 100) {
                            binding.dummyView.visibility = View.GONE
                        }
                    }

                    setupGameWebView(binding.webView) { newProgress ->
                        binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE

                        if (newProgress == 100) {
                            binding.dummyView.visibility = View.GONE
                        }
                    }

                    val appUUID = (application as GameApplication).appUUID
                    binding.webView.loadUrl("$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}")
                }
            }

            trackerViewModel.fetchConfig()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activityResultLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}