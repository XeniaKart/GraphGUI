package view

import ru.spbu.graphgui.controller.VertexDragController
import ru.spbu.graphgui.model.Edge
import ru.spbu.graphgui.model.Graph
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.add
import tornadofx.find

class GraphView<V, E>(private val graph: Graph<V, E> = Graph()) : Pane() {
    private val dragger = find(VertexDragController::class)
    val vertices by lazy {
        graph.vertices().associateWith {
            VertexView(it, 0.0, 0.0, graphSetting.vertex.radius, Color.RED)
        }
    }

    private val edges by lazy {
        graph.edges().associateWith {
            val first = vertices[it.vertices.first]
                ?: throw IllegalStateException("Model.Graph.view.VertexView for ${it.vertices.first} not found")
            val second = vertices[it.vertices.second]
                ?: throw IllegalStateException("Model.Graph.view.VertexView for ${it.vertices.second} not found")
            EdgeView(it, first, second)
        }
    }
    fun vertices(): Collection<VertexView<V>> = vertices.values
    fun verticesKeys(): Collection<V> = vertices.keys
    fun edges(): Collection<EdgeView<E, V>> = edges.values
    fun edgesVertex(): Collection<Edge<E, V>> = edges.keys

    init {
        vertices().forEach { v ->
            add(v)
            add(v.label)
            v.setOnMouseEntered { e -> e?.let { dragger.entered(it) } }
            v.setOnMousePressed { e -> e?.let { dragger.pressed(it) } }
            v.setOnMouseDragged { e -> e?.let { dragger.dragged(it) } }
            v.setOnMouseReleased { e -> e?.let { dragger.released(it) } }
            v.setOnMouseExited { e -> e?.let { dragger.exited(it) } }
        }
        edges().forEach {
            add(it)
            add(it.label)
        }
    }

}
