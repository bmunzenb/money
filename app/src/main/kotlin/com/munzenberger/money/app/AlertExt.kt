package com.munzenberger.money.app

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

fun Alert.showAndDoWhen(buttonType: ButtonType, block: () -> Unit) {

    val result = showAndWait()

    if (result.isPresent && result.get() == buttonType) {
         block.invoke()
    }
}
