package com.elnico.winwintesttask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.elnico.winwintesttask.databinding.FragmentTrackerBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackerFragment: WebViewFragment() {

    private lateinit var binding: FragmentTrackerBinding

    private val trackerViewModel: TrackerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (preferences.contains(TRACKER_VISITED)) {
            val url = preferences.getString(TRACKER_VISITED, null)

            if (url != null) {

                lifecycleScope.launch {
                    trackerViewModel.configFlow.filterNotNull().collect {
                        val fallbackUrl = it["fallback_url"].orEmpty()

                        (requireActivity() as MainActivity).navigateToContents(
                            url = fallbackUrl,
                            shouldCacheTrackerLink = false,
                            fallbackUrl = fallbackUrl
                        )
                        //binding.webView.loadUrl("$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}")
                    }
                }

                trackerViewModel.fetchConfig()
            }
        } else {
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

                    setupGameWebView(binding.webView) { newProgress ->
                        binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE

                        if (newProgress == 100) {
                            binding.dummyView.visibility = View.GONE
                        }
                    }

                    val appUUID = (requireActivity().application as GameApplication).appUUID
                    (requireActivity() as MainActivity).navigateToContents(
                        url = "$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}",
                        shouldCacheTrackerLink = true,
                        fallbackUrl = fallbackUrl
                    )
                    //binding.webView.loadUrl("$trackerBaseUrl/click.php?key=$trackerKey&external_onesignal_user_id=$appUUID&appsflyer_id=${appsflyerId}&advertising_id=${advertisingId}&type=af_status&version=${version}&site_id=${siteId}")
                }
            }

            trackerViewModel.fetchConfig()
        }
    }
}