package com.elnico.winwintesttask

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GameApplication: Application() {

    lateinit var appUUID: String

    companion object {
        private const val TAG = "GameApp"
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        appUUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)



        startKoin {
            androidContext(applicationContext)
            modules(
                listOf(
                    getViewModelModule()
                )
            )
        }
    }

    fun setupAnalytics(appsflyerApiKey: String, onesignalApiKey: String) {
        AppsFlyerLib.getInstance().init(appsflyerApiKey, null, this)
        //AppsFlyerLib.getInstance().start(this)

        AppsFlyerLib.getInstance().start(this, appsflyerApiKey, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(TAG, "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.d(TAG, "Launch failed to be sent:\n" +
                        "Error code: " + errorCode + "\n"
                        + "Error description: " + errorDesc)
            }
        })

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, onesignalApiKey)
        OneSignal.login(appUUID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }
}