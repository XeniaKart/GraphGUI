package ru.spbu.graphgui.view

import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import ru.spbu.graphgui.controller.Scroller
import ru.spbu.graphgui.controller.VertexDragController
import ru.spbu.graphgui.model.Edge
import ru.spbu.graphgui.model.Graph
import tornadofx.add
import tornadofx.find
import tornadofx.onChange

class GraphView<V, E>(private val graph: Graph<V, E> = Graph()) : Pane() {
    private val dragger = find(VertexDragController::class)

    init {
        val scroller = find(Scroller::class)
        setOnScroll { e -> e?.let { scroller.scroll(it) } }
        widthProperty().onChange { println("Width=$it") }
        heightProperty().onChange { println("Height=$it") }
        minWidth = 65_536.0
        minHeight = 65_536.0
//        minWidth = 500.0
//        minHeight = 500.0

        //setOnMouseEntered { e -> e?.let { scroller.entered(it) } }
        //setOnMousePressed { e -> e?.let { scroller.pressed(it) } }
        //setOnMouseDragged { e -> e?.let { scroller.dragged(it) } }
        //setOnMouseReleased { e -> e?.let { scroller.released(it) } }
        //setOnMouseExited { e -> e?.let { scroller.exited(it) } }
    }

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
        edges().forEach {
            add(it)
            add(it.label)
        }
        vertices().forEach { v ->
            add(v)
            add(v.label)
            v.setOnMouseEntered { e -> e?.let { dragger.entered(it) } }
            v.setOnMousePressed { e -> e?.let { dragger.pressed(it) } }
            v.setOnMouseDragged { e -> e?.let { dragger.dragged(it) } }
            v.setOnMouseReleased { e -> e?.let { dragger.released(it) } }
            v.setOnMouseExited { e -> e?.let { dragger.exited(it) } }
        }
    }

}
