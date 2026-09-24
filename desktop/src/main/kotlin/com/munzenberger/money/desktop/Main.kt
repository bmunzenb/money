package com.munzenberger.money.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.close
import com.munzenberger.money.desktop.inject.appModule
import com.munzenberger.money.desktop.menu.MoneyMenuBar
import money.shared.generated.resources.Res
import money.shared.generated.resources.app_title
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
            MoneyMenuBar(onExit = shutdown)

            App()
        }
    }
}
