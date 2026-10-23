package com.munzenberger.money.desktop

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class MoneyApplication : Application() {
    override fun start(stage: Stage) {
        stage.title = strings.getString("app_title")
        stage.scene = Scene(StackPane(), 640.0, 480.0)
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}
