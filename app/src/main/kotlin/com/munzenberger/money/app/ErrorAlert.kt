package com.munzenberger.money.app

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

class ErrorAlert(error: Throwable) : Alert(AlertType.ERROR) {

    init {

        title = error.javaClass.simpleName
        headerText = error.javaClass.name
        contentText = error.message

        // expandable stacktrace
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        error.printStackTrace(pw)
        val stacktrace = sw.toString()

        val label = Label("The error stacktrace was:")

        val textArea = TextArea(stacktrace).apply {
            isEditable = false
            isWrapText = true
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
        }

        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val gridPane = GridPane().apply {
            maxWidth = Double.MAX_VALUE
            add(label, 0, 0)
            add(textArea, 0, 1)
        }

        dialogPane.expandableContent = gridPane
    }

    companion object {

        fun showAndWait(error: Throwable): Optional<ButtonType> {
            error.printStackTrace(System.err)
            return ErrorAlert(error).showAndWait()
        }
    }
}
