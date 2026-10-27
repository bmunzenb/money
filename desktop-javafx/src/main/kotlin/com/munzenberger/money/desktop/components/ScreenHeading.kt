package com.munzenberger.money.desktop.components

import javafx.scene.control.Label

/** The title at the top of a screen. */
class ScreenHeading(text: String) : Label(text) {
    init {
        style = "-fx-font-size: 1.5em; -fx-font-weight: bold;"
    }
}
