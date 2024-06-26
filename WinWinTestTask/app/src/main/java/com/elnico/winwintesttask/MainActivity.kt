package com.elnico.winwintesttask

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.elnico.winwintesttask.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    companion object {
        private const val TAG = "MainActivity"
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        Log.v(TAG, "Notifications permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.fragment_container)
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activityResultLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun navigateToContents(url: String, shouldCacheTrackerLink: Boolean, fallbackUrl: String) {
        val bundle = Bundle()
        bundle.putString("url", url)
        bundle.putBoolean("shouldCacheTrackerLink", shouldCacheTrackerLink)
        bundle.putString("fallbackUrl", fallbackUrl)

        navController.navigate(R.id.gameFragment, bundle)
    }
}