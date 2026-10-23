package com.munzenberger.money.desktop

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class MoneyApplication : Application() {
    override fun start(stage: Stage) {
        stage.title = "Money"
        stage.scene = Scene(StackPane(Label("Hello world")), 640.0, 480.0)
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}
