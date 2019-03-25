package com.munzenberger.money.app.navigation

import javafx.fxml.FXMLLoader
import javafx.scene.Node
import java.net.URL
import java.util.concurrent.Callable

interface Navigation : Callable<Node>

class LayoutControllerNavigation<T>(private val layoutLocation: URL, private val start: (T) -> Unit) : Navigation {

    private var node: Node? = null

    override fun call(): Node {

        if (node == null) {

            val loader = FXMLLoader(layoutLocation)
            node = loader.load()

            val controller: T = loader.getController()
            start.invoke(controller)
        }

        return node!!
    }
}
