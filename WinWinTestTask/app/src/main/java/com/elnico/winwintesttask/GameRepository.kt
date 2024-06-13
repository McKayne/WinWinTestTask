package com.elnico.winwintesttask

import android.app.Application
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameRepository(application: Application): KoinComponent {

    private val apiService: ApiService by inject()

    suspend fun loadInitialConfig(): String? {
        val responseBody = apiService.performClick(BuildConfig.INITIAL_CONFIG_KEY)
        val type = responseBody.contentType()

        if (type?.toString() == "text/html") {
            val html = responseBody.string()
            return html
        }

        return null
    }
}