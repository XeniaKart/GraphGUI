package ru.spbu.graphgui.view.JDBC

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Edge(id:  EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Edge>(Edges)

    var sourceNode by Edges.sourceNode
    var targetNode by Edges.targetNode
    var direction by Edges.direction
    var weight by Edges.weight
}