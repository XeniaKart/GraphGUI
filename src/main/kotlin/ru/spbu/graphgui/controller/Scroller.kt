package ru.spbu.graphgui.controller

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import tornadofx.Controller
import tornadofx.runLater
import tornadofx.success
import java.lang.Thread.sleep

class Scroller : Controller() {
    var coordinateXOfCursorScrolled = 0.0
    var coordinateYOfCursorScrolled = 0.0
    var coordinatePanelX = 0.0
    var coordinatePanelY = 0.0
    fun BeatufulScroll() {
        TODO("Write scroll relative to the coordinates")
    }
    fun scroll(event: ScrollEvent) {
        if (event.target !is Pane)
            return
        event.consume()
        val panelWithGraph = check(event)
        if (event.deltaY > 0) {
            panelWithGraph.scaleX *= event.deltaY / 30
            panelWithGraph.scaleY *= event.deltaY / 30
        } else {
            panelWithGraph.scaleX /= (-event.deltaY / 30)
            panelWithGraph.scaleY /= (-event.deltaY / 30)
        }
        panelWithGraph.parent.parent.layout()
    }

    private fun check(event: ScrollEvent): Pane {
        return event.target as Pane
    }
}
