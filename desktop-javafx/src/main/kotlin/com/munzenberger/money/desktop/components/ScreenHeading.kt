package com.munzenberger.money.desktop.components

import javafx.scene.control.Label

/** The title at the top of a screen. */
class ScreenHeading(text: String) : Label(text) {
    init {
        styleClass += "screen-heading"
    }
}
