package ru.spbu.graphgui.view

import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.text

open class VertexView<V>(
    var vertex: V,
    x: Double,
    y: Double,
    r: DoubleProperty,
    color: Color
) : Circle(x, y, r.get(), color) {
    init {
        radiusProperty().bind(r)
    }

    var position: Pair<Double, Double>
        get() = centerX - graphCreator.graph.widthAndHeight.value / 2 to centerY - graphCreator.graph.widthAndHeight.value / 2
        set(value) {
            centerX = value.first + graphCreator.graph.widthAndHeight.value / 2
            centerY = value.second + graphCreator.graph.widthAndHeight.value / 2
        }

    var color: Color
        get() = fill as Color
        set(value) {
            fill = value
        }

    var label = text(vertex.toString()) {
        visibleProperty().bind(graphCreator.vertex.label)
        xProperty().bind(centerXProperty().subtract(layoutBounds.width / 2))
        yProperty().bind(centerYProperty().add(radiusProperty()).add(layoutBounds.height))
    }
}
