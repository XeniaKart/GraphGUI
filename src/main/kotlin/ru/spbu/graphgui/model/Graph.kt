package ru.spbu.graphgui.model

import tornadofx.objectProperty

class Graph<V, E> {
    private val vertices = hashMapOf<V, V>()
    private val edges = hashMapOf<Pair<V, V>, Edge<E, V>>()

    fun vertices(): Collection<V> {
        return vertices.keys
    }

    fun edges(): Collection<Edge<E, V>> = edges.values

    fun addVertex(v: V): V = vertices.getOrPut(v) { v }

    fun addEdge(u: V, v: V, e: E): Edge<E, V> {
        val first = addVertex(u)
        val second = addVertex(v)
        return edges.getOrPut(Pair(u, v)) { Edge(e, first, second) }
    }
}


class Edge<E, V>(
    var weight: E,
    var first: V,
    var second: V,
) {
    var weightProperty = objectProperty(weight)
    val vertices: Pair<V, V> = Pair(first, second)
}
