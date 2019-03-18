package com.munzenberger.money.app.navigation

import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleListProperty
import javafx.scene.Node
import java.util.concurrent.Callable

class Navigator(private val consumer: (Node) -> Unit) {

    private val backHistory = SimpleListProperty<Callable<Node>>()
    private val forwardHistory = SimpleListProperty<Callable<Node>>()

    private var lastNavigation: Callable<Node>? = null

    val backHistoryProperty: ReadOnlyListProperty<Callable<Node>> = backHistory
    val forwardHistoryProperty: ReadOnlyListProperty<Callable<Node>> = forwardHistory

    fun goTo(navigation: Callable<Node>) {

        if (navigate(navigation, backHistory)) {
            forwardHistory.clear()
        }
    }

    fun goBack() {

        goHistory(backHistory, forwardHistory)
    }

    fun goForward() {

        goHistory(forwardHistory, backHistory)
    }

    private fun navigate(to: Callable<Node>, history: MutableList<Callable<Node>>): Boolean {

        consumer.invoke(to.call())

        val changed =
                if (lastNavigation != null && lastNavigation != to) {
                    history.add(0, lastNavigation!!)
                    true
                } else {
                    false
                }

        lastNavigation = to

        return changed
    }

    private fun goHistory(from: MutableList<Callable<Node>>, to: MutableList<Callable<Node>>) {

        val callable = from.removeAt(0)
        navigate(callable, to)
    }
}
