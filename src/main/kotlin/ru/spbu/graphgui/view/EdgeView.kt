package ru.spbu.graphgui.view

import javafx.beans.property.DoubleProperty
import javafx.scene.shape.Line
import ru.spbu.graphgui.model.Edge
import tornadofx.doubleProperty
import tornadofx.text
import tornadofx.visibleWhen

class EdgeView<E, V>(
    edge: Edge<E, V>,
    first: VertexView<V>,
    second: VertexView<V>
) : Line() {

    init {
        startXProperty().bind(first.centerXProperty())
        startYProperty().bind(first.centerYProperty())
        endXProperty().bind(second.centerXProperty())
        endYProperty().bind(second.centerYProperty())
        strokeWidthProperty().bind(graphSetting.edge.width)
    }

    val label = text(edge.element.toString()) {
        visibleWhen(graphSetting.edge.label)
        xProperty().bind(
            startXProperty().add(endXProperty()).divide(2).subtract(layoutBounds.width / 2)
        )
        yProperty().bind(
            startYProperty().add(endYProperty()).divide(2).add(layoutBounds.height / 1.5)
        )
    }
}
