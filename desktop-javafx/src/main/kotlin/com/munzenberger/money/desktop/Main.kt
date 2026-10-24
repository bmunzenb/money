package com.munzenberger.money.desktop

import com.munzenberger.money.data.api.preferences.MoneyPreferences
import com.munzenberger.money.presentation.inject.presentationModule
import com.munzenberger.money.presentation.preferences.PreferenceKeys
import com.munzenberger.money.presentation.preferences.WindowBounds
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Screen
import javafx.stage.Stage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class MoneyApplication : Application(), KoinComponent {
    private val preferences: MoneyPreferences by inject()

    override fun start(stage: Stage) {
        stage.title = strings.getString("app_title")
        stage.scene = Scene(StackPane(), DEFAULT_WIDTH, DEFAULT_HEIGHT)
        restoreBounds(stage)
        stage.show()
    }

    // Skips bounds that no longer fall on any screen, e.g. from a disconnected monitor.
    private fun restoreBounds(stage: Stage) {
        val bounds = preferences[PreferenceKeys.windowBounds]?.takeIf { it.isOnScreen() } ?: return
        stage.x = bounds.x
        stage.y = bounds.y
        stage.width = bounds.width
        stage.height = bounds.height
    }

    private fun WindowBounds.isOnScreen() =
        Screen.getScreensForRectangle(x, y, width, height).isNotEmpty()

    private companion object {
        const val DEFAULT_WIDTH = 640.0
        const val DEFAULT_HEIGHT = 480.0
    }
}

fun main(args: Array<String>) {
    startKoin {
        modules(presentationModule)
    }

    Application.launch(MoneyApplication::class.java, *args)
}
