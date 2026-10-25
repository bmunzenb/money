package com.munzenberger.money.desktop.welcome

import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.VBox

// The buttons don't do anything yet.
class WelcomeScreen(
    private val viewModel: WelcomeViewModel,
) : VBox(SPACING) {
    init {
        alignment = Pos.CENTER
        children.setAll(
            Button(strings.getString("open_database_button_title")),
            Button(strings.getString("create_database_button_title")),
        )
    }

    private companion object {
        const val SPACING = 8.0
    }
}
