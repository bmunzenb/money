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

    private lateinit var controller: ApplicationController

    override fun start(primaryStage: Stage) {

        val root: Parent = FXMLLoader(ApplicationController.LAYOUT).loadWithController { controller: ApplicationController ->
            this.controller = controller.apply { start(primaryStage) }
        }

        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    override fun stop() {
        controller.shutdown()
    }
}
