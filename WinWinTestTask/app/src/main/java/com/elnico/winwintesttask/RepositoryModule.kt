package com.elnico.winwintesttask

import org.koin.dsl.module

fun getRepositoriesModule() = module {
    single { GameRepository(get()) }
}