package com.munzenberger.money.desktop.inject

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.desktop.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MoneyRepositoryController() }
    viewModel { WelcomeViewModel() }
}
