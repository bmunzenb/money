package com.munzenberger.money.app

import com.munzenberger.money.core.MoneyDatabase
import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import picocli.CommandLine
import java.util.concurrent.Callable

fun main(args: Array<String>) {
    CommandLine.call(Launcher(), *args)
}

@CommandLine.Command
class Launcher : Callable<Unit> {

    override fun call() {
        Application.launch(MoneyApplication::class.java)
    }
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
        database?.close()
    }

    companion object {

        private val databaseProperty = SimpleObjectProperty<MoneyDatabase?>()

        var database: MoneyDatabase?
            get() = databaseProperty.get()
            set(value) {
                databaseProperty.value?.close()
                databaseProperty.value = value
            }

        val observableDatabase: ObservableValue<MoneyDatabase?> = databaseProperty
    }
}
