package com.elnico.winwintesttask

import android.app.Application
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

    companion object {
        private const val TAG = "GameApp"
    }

    override fun onCreate() {
        super.onCreate()
        AppsFlyerLib.getInstance().init(BuildConfig.APPSFLYER_API_KEY, null, this)
        //AppsFlyerLib.getInstance().start(this)

        AppsFlyerLib.getInstance().start(this, BuildConfig.APPSFLYER_API_KEY, object :
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
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_API_KEY)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }

        startKoin {
            androidContext(applicationContext)
            modules(
                listOf(
                    getApiModule(BuildConfig.INITIAL_CONFIG_BASE_URL),
                    getViewModelModule(),
                    getRepositoriesModule()
                )
            )
        }
    }
}