package ru.spbu.graphgui.controller

import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import ru.spbu.graphgui.view.graphCreator
import tornadofx.Controller

class Scroller : Controller() {
    fun scroll(event: ScrollEvent, x: Double, y: Double) {
        val target = event.target
        if (target !is Pane)
            return

        event.consume()
        val scrollingSpeed = 28
        target.apply {
            if (event.deltaY > 0) {
                scaleX *= event.deltaY / scrollingSpeed
                scaleY *= event.deltaY / scrollingSpeed
                translateX -= (event.x - x + 614.4 - graphCreator.graph.widthAndHeight.value / 2.0) / scrollingSpeed * scaleX
                translateY -= (event.y - y + 384.0 - graphCreator.graph.widthAndHeight.value / 2.0) / scrollingSpeed * scaleX
            } else {
                scaleX /= (-event.deltaY / scrollingSpeed)
                scaleY /= (-event.deltaY / scrollingSpeed)
                translateX += (event.x - x + 614.4 - graphCreator.graph.widthAndHeight.value / 2.0) / scrollingSpeed * scaleX
                translateY += (event.y - y + 384.0 - graphCreator.graph.widthAndHeight.value / 2.0) / scrollingSpeed * scaleX
            }
            parent.parent.layout()
        }
    }
}
