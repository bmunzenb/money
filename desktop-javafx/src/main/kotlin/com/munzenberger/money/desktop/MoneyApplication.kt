package com.munzenberger.money.desktop

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.preferences.WindowBounds
import com.munzenberger.money.presentation.preferences.windowBounds
import javafx.animation.Animation
import javafx.animation.PauseTransition
import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.util.Duration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MoneyApplication : Application(), KoinComponent {
    private val preferences: UserPreferences by inject()

    // Holds AppScreen's app-lifetime ViewModels.
    private val appScope = ScreenScope()

    private lateinit var stage: Stage
    private lateinit var appScreen: AppScreen

    // Saves the bounds once the window stops moving or resizing, rather than on every change.
    private val saveBoundsDelay = PauseTransition(Duration.millis(SAVE_BOUNDS_DELAY_MILLIS))

    override fun start(stage: Stage) {
        this.stage = stage
        stage.title = strings.getString("app_title")
        appScreen = AppScreen(appScope)
        stage.scene = Scene(appScreen, DEFAULT_WIDTH, DEFAULT_HEIGHT).apply {
            val styleSheet = MoneyApplication::class.java.getResource("styles.css")
            stylesheets += checkNotNull(styleSheet).toExternalForm()
        }
        restoreBounds(stage)
        stage.show()
        saveBoundsOnChange(stage)
    }

    override fun stop() {
        appScreen.close()
        appScope.close()

        if (saveBoundsDelay.status == Animation.Status.RUNNING) {
            saveBoundsDelay.stop()
            saveBounds(stage)
        }
    }

    // Skips bounds that no longer fall on any screen, e.g. from a disconnected monitor.
    private fun restoreBounds(stage: Stage) {
        val bounds = preferences.windowBounds?.takeIf { it.isOnScreen() } ?: return
        stage.x = bounds.x
        stage.y = bounds.y
        stage.width = bounds.width
        stage.height = bounds.height
    }

    private fun saveBoundsOnChange(stage: Stage) {
        saveBoundsDelay.setOnFinished { saveBounds(stage) }
        val listener = InvalidationListener { saveBoundsDelay.playFromStart() }
        stage.xProperty().addListener(listener)
        stage.yProperty().addListener(listener)
        stage.widthProperty().addListener(listener)
        stage.heightProperty().addListener(listener)
    }

    // Skips a maximized, full-screen or minimized window, so its normal bounds are what's restored.
    private fun saveBounds(stage: Stage) {
        if (stage.isMaximized || stage.isFullScreen || stage.isIconified) return
        preferences.windowBounds = WindowBounds(stage.x, stage.y, stage.width, stage.height)
    }

    private fun WindowBounds.isOnScreen() =
        Screen.getScreensForRectangle(x, y, width, height).isNotEmpty()

    private companion object {
        const val DEFAULT_WIDTH = 640.0
        const val DEFAULT_HEIGHT = 480.0
        const val SAVE_BOUNDS_DELAY_MILLIS = 500.0
    }
}
