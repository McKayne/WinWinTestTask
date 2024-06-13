package com.elnico.winwintesttask

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameViewModel(application: Application): AndroidViewModel(application), KoinComponent {

    private val gameRepository: GameRepository by inject()

    val responseFlow = MutableStateFlow<Pair<String, Boolean>?>(null)

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    }

    fun loadInitialConfig() {
        viewModelScope.launch(exceptionHandler) {
            withContext(Dispatchers.IO) {
                val configPage = gameRepository.loadInitialConfig()
                responseFlow.update {
                    if (configPage != null)
                        Pair(configPage, true)
                    else
                        Pair(BuildConfig.GAME_URL, false)
                }
            }
        }
    }
}