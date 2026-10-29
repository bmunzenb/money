package com.munzenberger.money.desktop.components

import javafx.scene.Node

/** Shows or hides this node. A hidden node also gives up its space in the layout. */
fun Node.setShown(shown: Boolean) {
    isVisible = shown
    isManaged = shown
}
