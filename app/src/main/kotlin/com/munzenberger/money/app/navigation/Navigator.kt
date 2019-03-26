package com.munzenberger.money.app.navigation

import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.scene.Node
import java.util.concurrent.Callable

class Navigator(private val consumer: (Node) -> Unit) : AutoCloseable {

    private val backHistory = SimpleListProperty<Navigation>(FXCollections.observableArrayList())
    private val forwardHistory = SimpleListProperty<Navigation>(FXCollections.observableArrayList())

    private var lastNavigation: Navigation? = null

    val backHistoryProperty: ReadOnlyListProperty<Navigation> = backHistory
    val forwardHistoryProperty: ReadOnlyListProperty<Navigation> = forwardHistory

    fun goTo(navigation: Navigation) {

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

    private fun navigate(to: Navigation, history: SimpleListProperty<Navigation>): Boolean {

        if (lastNavigation == to) {
            return false
        }

        lastNavigation?.close()
        consumer.invoke(to.call())
        history.add(0, lastNavigation)
        lastNavigation = to

        return true
    }

    private fun goHistory(from: SimpleListProperty<Navigation>, to: SimpleListProperty<Navigation>) {

        val callable = from.removeAt(0)
        navigate(callable, to)
    }

    override fun close() {
        lastNavigation?.close()
        backHistory.clear()
        forwardHistory.clear()
    }
}
