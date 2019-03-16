package com.munzenberger.money.app

import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.ProgressIndicator
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

class ProgressDialog private constructor(private val stage: Stage) {

    fun close() {
        stage.close()
    }

    companion object {

        fun doInDialog(ownerWindow: Window, title: String, block: (dialog: ProgressDialog) -> Unit) {

            val progressIndicator = ProgressIndicator().apply {
                minHeight = 60.0
                minWidth = 60.0
            }

            val root = Group(progressIndicator)

            val scene = Scene(root, 400.0, 200.0)

            val stage = Stage().apply {
                initOwner(ownerWindow)
                initModality(Modality.APPLICATION_MODAL)
                setTitle(title)
                setScene(scene)
            }

            stage.show()

            block.invoke(ProgressDialog(stage))
        }
    }
}
