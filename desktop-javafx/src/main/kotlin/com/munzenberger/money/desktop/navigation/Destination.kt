package com.munzenberger.money.desktop.navigation

import javafx.scene.Node
import javafx.scene.Parent

/** How [NavigationHost] presents a back stack entry's [node]. */
sealed interface Destination {
    val node: Node

    /** Fills the host, replacing the screen below it. */
    class Screen(override val node: Node) : Destination

    /** Opens in a modal window, titled [title] if given, over the screen below it, which stays visible. */
    class Dialog(override val node: Parent, val title: String? = null) : Destination
}
