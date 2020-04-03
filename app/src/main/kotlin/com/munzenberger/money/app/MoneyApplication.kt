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

        FXMLLoader(ApplicationController.LAYOUT).load { root: Parent, controller: ApplicationController ->

            this.controller = controller.apply { start(primaryStage) }

            primaryStage.apply {

                scene = Scene(root).apply {
                    stylesheets.add(MoneyApplication::class.java.getResource("money.css").toExternalForm())
                }

                width = 800.0
                height = 600.0
                minWidth = width
                minHeight = height
                show()
            }
        }
    }

    override fun stop() {
        controller.close()
    }
}
