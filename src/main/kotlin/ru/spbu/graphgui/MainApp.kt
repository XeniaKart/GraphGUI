package ru.spbu.graphgui

import javafx.stage.Stage
import ru.spbu.graphgui.styles.Styles
import ru.spbu.graphgui.view.MainView
import tornadofx.App
import tornadofx.launch

class MainApp : App(MainView::class, Styles::class) {
    override fun start(stage: Stage) {
        stage.width = 800.0
        stage.height = 800.0

        super.start(stage)

    }
}

fun main(args: Array<String>) {
    launch<MainApp>(args)
}