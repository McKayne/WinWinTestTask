package com.elnico.winwintesttask

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.elnico.winwintesttask.databinding.FragmentGameBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class GameFragment: WebViewFragment() {

    private lateinit var binding: FragmentGameBinding

    private val trackerViewModel: TrackerViewModel by viewModel()

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

        val url = arguments?.getString("url")
        val shouldCacheTrackerLink = arguments?.getBoolean("shouldCacheTrackerLink")
        val fallbackURL = arguments?.getString("fallbackUrl")

        if (url != null && shouldCacheTrackerLink != null && fallbackURL != null) {
            this.shouldCacheTrackerLink = shouldCacheTrackerLink
            this.fallbackURL = fallbackURL

            setupGameWebView(binding.webView) { newProgress ->
                if (newProgress == 100) {
                    binding.dummyView.visibility = View.GONE
                }
            }

            binding.webView.loadUrl(url)

            /*lifecycleScope.launch {
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

                    this@GameFragment.fallbackURL = fallbackUrl
                    (requireActivity().application as GameApplication).setupAnalytics(appsflyerApiKey, onesignalApiKey)

                    trackerViewModel.fetchTrackerVisitedStatus {
                        /*shouldCacheTrackerLink = it == null

                        setupGameWebView(binding.webView) {

                        }

                        if (it != null && it.contains("jsontest")) {
                            resizeWebViewToOptimalSize(binding.webView)
                            binding.webView.settings.userAgentString =
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5507.209 Safari/537.36"
                            binding.webView.loadUrl(fallbackUrl)
                        } else if (it != null) {
                            binding.webView.loadUrl(it)
                        } else {
                            val appUUID = (requireActivity().application as GameApplication).appUUID
                            binding.webView.loadUrl("$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}")
                        }*/
                    }
                }
            }

            trackerViewModel.fetchConfig()*/
        }
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).forceScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }
}
