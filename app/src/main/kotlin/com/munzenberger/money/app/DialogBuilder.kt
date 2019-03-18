package com.munzenberger.money.app

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.net.URL

object DialogBuilder {

    fun <T> build(location: URL, block: (Stage, T) -> Unit) {

        FXMLLoader(location).load { node: Parent, controller: T ->

            val scene = Scene(node)

            val stage = Stage().apply {
                setScene(scene)
                initModality(Modality.APPLICATION_MODAL)
            }

            block.invoke(stage, controller)
        }
    }
}
