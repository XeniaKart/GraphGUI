package ru.spbu.graphgui.view

import javafx.scene.shape.Line
import ru.spbu.graphgui.model.Edge
import tornadofx.text
import tornadofx.visibleWhen

class EdgeView<E, V>(
    edge: Edge<E, V>,
    first: VertexView<V>,
    second: VertexView<V>
) : Line() {

    init {
//        var ww = edge.weight.toString().toDouble() * graphSetting.edge.width.value
//        var wwProperty: DoubleProperty = doubleProperty(ww)
//        wwProperty.bind(graphSetting.edge.width)
        startXProperty().bind(first.centerXProperty())
        startYProperty().bind(first.centerYProperty())
        endXProperty().bind(second.centerXProperty())
        endYProperty().bind(second.centerYProperty())
//        strokeWidth = edge.weight.toString().toDouble()
        strokeWidthProperty().bind(graphSetting.edge.width)
    }

    val label = text(edge.weight.toString()) {
        visibleWhen(graphSetting.edge.label)
        xProperty().bind(
            startXProperty().add(endXProperty()).divide(2).subtract(layoutBounds.width / 2)
        )
        yProperty().bind(
            startYProperty().add(endYProperty()).divide(2).add(layoutBounds.height / 1.5)
        )
    }
}
