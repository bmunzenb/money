package com.munzenberger.money.desktop.navigation

import javafx.scene.Parent

/** How [NavigationHost] shows a back stack entry's node: in its own pane, or in a modal window of its own. */
sealed interface Destination {
    val node: Parent

    /** Shown in the host's pane, replacing the screen below it. */
    class Screen(override val node: Parent) : Destination

    /** Shown in a modal window titled [title], over the screen below it. */
    class Dialog(override val node: Parent, val title: String) : Destination
}
