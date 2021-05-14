package controller
import javafx.scene.Cursor
import javafx.scene.Node
import tornadofx.Controller
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane

class Scroller: Controller() {

    fun scroll(event: ScrollEvent) {
        if (event.target !is Pane) {
            return
        }
        val v = check(event)
        v.scaleX += event.deltaY / 150
        v.scaleY += event.deltaY / 150
    }

    fun entered(event: MouseEvent) {
        val v = check(event)
        if (!event.isSecondaryButtonDown)
            v.scene.cursor = Cursor.HAND

    }

    fun dragged(event: MouseEvent) {
        if (!event.isSecondaryButtonDown || event.target !is Pane)
            return
        val v = check(event)
        v.translateX = event.x
        v.translateY = event.y
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