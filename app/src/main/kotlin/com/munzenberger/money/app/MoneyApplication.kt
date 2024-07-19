package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}

class MoneyApplication : Application() {

    companion object {
        val CSS: String = MoneyApplication::class.java.getResource("money.css")!!.toExternalForm()
    }

    private val logger = Logger.getLogger(MoneyApplication::class.java.name)

    private lateinit var controller: ApplicationController

    override fun start(primaryStage: Stage) {

        FXMLLoader(ApplicationController.LAYOUT).load { root: Parent, controller: ApplicationController ->

            this.controller = controller

            primaryStage.apply {

                scene = Scene(root).apply {
                    stylesheets.add(CSS)
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
        if (!Executors.shutdownAndAwaitTermination(5, TimeUnit.SECONDS)) {
            logger.log(Level.WARNING, "Timeout while waiting for executors to shutdown.")
        }
    }
}
