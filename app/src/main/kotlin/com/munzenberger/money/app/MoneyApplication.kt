package com.munzenberger.money.app

import com.munzenberger.money.core.MoneyDatabase
import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}

class MoneyApplication : Application() {

    override fun start(primaryStage: Stage) {

        val root: Parent = FXMLLoader(ApplicationController.LAYOUT).loadWithController { controller: ApplicationController ->
            controller.start(primaryStage)
        }

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
