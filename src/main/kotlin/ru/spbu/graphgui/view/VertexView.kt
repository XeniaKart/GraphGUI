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
    color: Color,
) : Circle(x, y, r.get(), color) {
    init {
        radiusProperty().bind(r)
    }

    var position: Pair<Double, Double>
        get() = centerX - graphSetting.graph.widthAndHeight.value / 2.0 to centerY - graphSetting.graph.widthAndHeight.value / 2.0
        set(value) {
            centerX = value.first + graphSetting.graph.widthAndHeight.value / 2.0
            centerY = value.second + graphSetting.graph.widthAndHeight.value / 2.0
        }

    var color: Color
        get() = fill as Color
        set(value) {
            fill = value
        }

    var label = text(vertex.toString()) {
        visibleProperty().bind(graphSetting.vertex.label)
        xProperty().bind(centerXProperty().subtract(layoutBounds.width / 2))
        yProperty().bind(centerYProperty().add(radiusProperty()).add(layoutBounds.height))
    }
//        set(value) {
//            fill = text(value.toString())
//        }
}
