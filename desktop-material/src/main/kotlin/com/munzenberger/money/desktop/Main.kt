package com.munzenberger.money.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.inject.appModule
import money.desktop_material.generated.resources.Res
import money.desktop_material.generated.resources.app_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        val repositoryController: MoneyRepositoryController = koinInject()

        val shutdown: () -> Unit = {
            repositoryController.close()
            exitApplication()
        }

        Window(
            onCloseRequest = shutdown,
            title = stringResource(Res.string.app_title),
        ) {
            App()
        }
    }
}
