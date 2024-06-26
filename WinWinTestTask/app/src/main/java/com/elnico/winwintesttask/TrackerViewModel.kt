package com.elnico.winwintesttask

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent

class TrackerViewModel(private val application: Application): AndroidViewModel(application), KoinComponent {

    private val db = FirebaseFirestore.getInstance()

    val configFlow = MutableStateFlow<HashMap<String, String>?>(null)

    fun fetchConfig() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        //////////

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            val advertisingId = FirebaseRemoteConfig.getInstance().getString("advertisingId")
            val appsflyerApiKey = FirebaseRemoteConfig.getInstance().getString("appsflyer_api_key")
            val appsflyerId = FirebaseRemoteConfig.getInstance().getString("appsflyerId")
            val fallbackUrl = FirebaseRemoteConfig.getInstance().getString("fallback_url")
            val onesignalApiKey = FirebaseRemoteConfig.getInstance().getString("onesignal_api_key")
            val siteId = FirebaseRemoteConfig.getInstance().getString("siteId")
            val trackerBaseUrl = FirebaseRemoteConfig.getInstance().getString("tracker_base_url")
            val trackerKey = FirebaseRemoteConfig.getInstance().getString("tracker_key")
            val version = FirebaseRemoteConfig.getInstance().getString("version")

            val config = HashMap<String, String>()
            config["advertisingId"] = advertisingId
            config["appsflyer_api_key"] = appsflyerApiKey
            config["appsflyerId"] = appsflyerId
            config["fallback_url"] = fallbackUrl
            config["onesignal_api_key"] = onesignalApiKey
            config["siteId"] = siteId
            config["tracker_base_url"] = trackerBaseUrl
            config["tracker_key"] = trackerKey
            config["version"] = version

            configFlow.update { config }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun updateTrackerVisitedStatus(url: String, onCompletion: (Boolean) -> Unit) {
        val status = hashMapOf(
            (application as GameApplication).appUUID to url
        )

        db.collection("tracker-visited-status").add(status).addOnSuccessListener {
            onCompletion(true)
        }.addOnFailureListener {
            it.printStackTrace()

            onCompletion(false)
        }
    }

    fun fetchTrackerVisitedStatus(onCompletion: (String?) -> Unit) {
        db.collection("tracker-visited-status").get().addOnSuccessListener { result ->
            var existingLink: String? = null

            for (document in result.documents) {
                existingLink = document.data?.get((application as GameApplication).appUUID) as? String

                if (existingLink != null) {
                    break
                }
            }

            onCompletion(existingLink)
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }
}