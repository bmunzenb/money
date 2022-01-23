package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
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

            this.controller = controller

            primaryStage.apply {

                scene = Scene(root).apply {

                    val css = MoneyApplication::class.java.getResource("money.css").toExternalForm()

                    stylesheets.add(css)
                }

                width = 800.0
                height = 600.0
                minWidth = width
                minHeight = height

                show()
            }

            controller.start(primaryStage)
        }
    }

    override fun stop() {
        controller.close()
        Executors.shutdown()
    }
}
