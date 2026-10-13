package com.munzenberger.money.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.inject.appModule
import money.presentation.generated.resources.Res
import money.presentation.generated.resources.app_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.Text
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
            IntUiTheme(isDark = isSystemInDarkTheme()) {
                // Placeholder content until the app shell is built.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JewelTheme.globalColors.panelBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(Res.string.app_title))
                }
            }
        }
    }
}
