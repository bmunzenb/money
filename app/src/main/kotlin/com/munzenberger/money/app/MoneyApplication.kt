package com.munzenberger.money.app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}

class MoneyApplication : Application() {

    override fun start(primaryStage: Stage) {

        val loader = FXMLLoader(ApplicationController.LAYOUT)
        val root: Parent = loader.load()
        val controller: ApplicationController = loader.getController()

        controller.start(primaryStage)

        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    override fun stop() {

    }
}
