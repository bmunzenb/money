package com.munzenberger.money.app

import javafx.scene.control.Alert

object AlertBuilder {

    fun error(title: String? = "Error", header: String? = null, error: Throwable) = error(title, header, error.message)

    fun error(title: String? = "Error", header: String? = null, errorText: String?) = Alert(Alert.AlertType.ERROR).apply {
        this.title = title
        this.headerText = header
        this.contentText = errorText
    }
}
