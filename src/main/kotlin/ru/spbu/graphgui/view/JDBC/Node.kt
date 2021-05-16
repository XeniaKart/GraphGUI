package ru.spbu.graphgui.view.JDBC

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Node(id:  EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Node>(Nodes)

    var name by Nodes.name
    var color by Nodes.color
    var coordX by Nodes.coordX
    var coordY by Nodes.coordY
    var radius by Nodes.radius
//    var centrality by Nodes.centrality

//    override fun toString(): String = "User(name = $name, color = $color, centrality = $centrality, )"
}