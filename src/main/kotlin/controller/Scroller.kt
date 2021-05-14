package controller
import javafx.scene.Cursor
import javafx.scene.Node
import tornadofx.Controller
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane

class Scroller: Controller() {
    var coordCursorPressedX = 0.0
    var coordCursorPressedY = 0.0
    var coordinatePanelX = 0.0
    var coordinatePanelY = 0.0
    fun scroll(event: ScrollEvent) {
        if (event.target !is Pane) {
            return
        }
        val panelWithGraph = check(event)
        if (event.deltaY > 0) {
            panelWithGraph.scaleX *= event.deltaY / 30
            panelWithGraph.scaleY *= event.deltaY / 30
        } else {
            panelWithGraph.scaleX /= (-event.deltaY / 30)
            panelWithGraph.scaleY /= (-event.deltaY / 30)
        }

    }

    fun entered(event: MouseEvent) {
        val panelWithGraph = check(event)
        if (!event.isPrimaryButtonDown)
            panelWithGraph.scene.cursor = Cursor.HAND
    }

    fun pressed(event: MouseEvent) {
        val panelWithGraph = check(event)
        if (!event.isPrimaryButtonDown)
            return
        panelWithGraph.scene.cursor =  Cursor.CLOSED_HAND
        event.consume()
        coordCursorPressedX = event.x
        coordCursorPressedY = event.y
        coordinatePanelX = panelWithGraph.translateX
        coordinatePanelY = panelWithGraph.translateY
    }

    fun dragged(event: MouseEvent) {
        if (!event.isPrimaryButtonDown || event.target !is Pane)
            return
        val panelWithGraph = check(event)
        panelWithGraph.translateX = coordinatePanelX + (event.x - coordCursorPressedX)
        panelWithGraph.translateY = coordinatePanelY + (event.y - coordCursorPressedY)
        event.consume()
    }

    fun released(event: MouseEvent) {
        val v = check(event)
        v.scene.cursor = Cursor.HAND
        event.consume()
    }

    fun exited(event: MouseEvent) {
        val v = check(event)
        if (!event.isPrimaryButtonDown)
            v.scene.cursor = Cursor.DEFAULT
    }

    private fun check(event: MouseEvent): Node {
        return event.target as Node
    }

    private fun check(event: ScrollEvent): Pane {
        return event.target as Pane
    }
}