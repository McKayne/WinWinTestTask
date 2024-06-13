package com.elnico.winwintesttask

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun getViewModelModule() = module {
    viewModel { GameViewModel(get()) }
}