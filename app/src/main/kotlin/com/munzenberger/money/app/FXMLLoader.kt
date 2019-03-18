package com.munzenberger.money.app

import javafx.fxml.FXMLLoader

fun <T, C> FXMLLoader.load(block: (T, C) -> Unit) {

    val root: T = load()
    val controller: C = getController()

    block.invoke(root, controller)
}
