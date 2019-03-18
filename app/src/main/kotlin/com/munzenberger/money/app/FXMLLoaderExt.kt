package com.munzenberger.money.app

import javafx.fxml.FXMLLoader

fun <T, C> FXMLLoader.loadWithController(block: (C) -> Unit): T {

    val root: T = load()
    val controller: C = getController()

    block.invoke(controller)

    return root
}
