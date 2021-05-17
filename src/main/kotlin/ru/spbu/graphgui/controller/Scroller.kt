package ru.spbu.graphgui.controller

import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import ru.spbu.graphgui.view.graphSetting
import tornadofx.Controller

class Scroller : Controller() {
    fun scroll(event: ScrollEvent, x: Double, y: Double) {
        if (event.target !is Pane)
            return
        event.consume()
        val panelWithGraph = check(event)
        val scrollingSpeed = 28
        if (event.deltaY > 0) {
            panelWithGraph.scaleX *= event.deltaY / scrollingSpeed
            panelWithGraph.scaleY *= event.deltaY / scrollingSpeed
            panelWithGraph.translateX -= (event.x - x + 614.4 - graphSetting.graph.widthAndHeight.value / 2.0) / scrollingSpeed * panelWithGraph.scaleX
            panelWithGraph.translateY -= (event.y - y + 384.0 - graphSetting.graph.widthAndHeight.value / 2.0) / scrollingSpeed * panelWithGraph.scaleX
        } else {
            panelWithGraph.scaleX /= (-event.deltaY / scrollingSpeed)
            panelWithGraph.scaleY /= (-event.deltaY / scrollingSpeed)
            panelWithGraph.translateX += (event.x - x + 614.4 - graphSetting.graph.widthAndHeight.value / 2.0) / scrollingSpeed * panelWithGraph.scaleX
            panelWithGraph.translateY += (event.y - y + 384.0 - graphSetting.graph.widthAndHeight.value / 2.0) / scrollingSpeed * panelWithGraph.scaleX
        }
        panelWithGraph.parent.parent.layout()
    }

    private fun check(event: ScrollEvent): Pane {
        return event.target as Pane
    }
}
