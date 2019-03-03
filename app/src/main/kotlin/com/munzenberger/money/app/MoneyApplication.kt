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

    private lateinit var applicationController: ApplicationController

    override fun start(primaryStage: Stage) {

        val loader = FXMLLoader(ApplicationController.LAYOUT)
        val root: Parent = loader.load()
        applicationController = loader.getController()

        applicationController.stage = primaryStage

        primaryStage.title = "Money"
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    override fun stop() {
        applicationController.onApplicationClose()
    }
}
