package com.elnico.winwintesttask

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

fun getApiModule(baseURL: String) = module {
    single { appApi(baseURL) }
}

private fun appApi(baseURL: String): ApiService {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient.Builder()
        .connectTimeout(600, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; ${androidVersion()}; sdk_gphone64_x86_64 Build/TE1A.220922.021) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.71 Mobile Safari/537.36 Client/Android")
                    .build()
            )
        }
        .addInterceptor(interceptor)
        .build()
    return Retrofit.Builder()
        .baseUrl(baseURL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ApiService::class.java)
}

private fun androidVersion(): String {
    var androidVersion = ""
    androidVersion = when (android.os.Build.VERSION.SDK_INT){
        16 -> "Android 4.1"
        17 -> "Android 4.2"
        18 -> "Android 4.3"
        19 -> "Android 4.4"
        21 -> "Android 5.0"
        22 -> "Android 5.1"
        23 -> "Android 6.0"
        24 -> "Android 7.0"
        25 -> "Android 7.1"
        26 -> "Android 8.0"
        27 -> "Android 8.1"
        28 -> "Android 9"
        29 -> "Android 10"
        30 -> "Android 11"
        31 -> "Android 12"
        32 -> "Android 12"
        33 -> "Android 13"
        else -> "Android 14"
    }

    return androidVersion
}