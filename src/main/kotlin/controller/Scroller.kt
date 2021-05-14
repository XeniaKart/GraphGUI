package controller
import javafx.scene.Cursor
import javafx.scene.Node
import tornadofx.Controller
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane

class Scroller: Controller() {
    var x = 0.0
    var y = 0.0
    var xx = 0.0
    var yy = 0.0
    fun scroll(event: ScrollEvent) {
        if (event.target !is Pane) {
            return
        }
        val v = check(event)
        v.scaleX += event.deltaY / 200
        v.scaleY += event.deltaY / 200
    }

    fun entered(event: MouseEvent) {
        val v = check(event)
        if (!event.isSecondaryButtonDown)
            v.scene.cursor = Cursor.HAND
    }

    fun pressed(event: MouseEvent) {
        val v = check(event)
        if (!event.isSecondaryButtonDown)
            return
        v.scene.cursor =  Cursor.CLOSED_HAND
        event.consume()
        x = event.x
        y = event.y
        xx = v.translateX
        yy = v.translateY
    }

    fun dragged(event: MouseEvent) {
        if (!event.isSecondaryButtonDown || event.target !is Pane)
            return
        val v = check(event)
        v.translateX = xx + event.x - x
        v.translateY = yy + event.y - y
        event.consume()
    }

    fun released(event: MouseEvent) {
        val v = check(event)
        v.scene.cursor = Cursor.HAND
        event.consume()
    }

    fun exited(event: MouseEvent) {
        val v = check(event)
        if (!event.isSecondaryButtonDown)
            v.scene.cursor = Cursor.DEFAULT
    }

    private fun check(event: MouseEvent): Node {
        return event.target as Node
    }

    private fun check(event: ScrollEvent): Pane {
        return event.target as Pane
    }
}