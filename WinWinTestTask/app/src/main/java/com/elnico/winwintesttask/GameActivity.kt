package com.elnico.winwintesttask

import android.os.Bundle
import android.view.View
import com.elnico.winwintesttask.databinding.ActivityGameBinding

class GameActivity: WebViewActivity() {

    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webView

        val url = intent?.getStringExtra("url")
        val shouldCacheTrackerLink = intent?.getBooleanExtra("shouldCacheTrackerLink", false)
        val fallbackURL = intent?.getStringExtra("fallbackUrl")

        if (url != null && shouldCacheTrackerLink != null && fallbackURL != null) {
            this.fallbackURL = fallbackURL

            val orientation = intent?.getIntExtra("orientation", -1)
            if (orientation != null && orientation != -1) {
                forceScreenOrientation(orientation)
            }

            setupGameWebView(binding.webView) { newProgress ->
                //binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE

                if (newProgress == 100) {
                    binding.dummyView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }

            binding.webView.loadUrl(url)
        }
    }
}