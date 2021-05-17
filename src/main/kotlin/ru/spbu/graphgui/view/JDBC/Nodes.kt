package ru.spbu.graphgui.view.JDBC

import org.jetbrains.exposed.dao.id.IntIdTable

object Nodes : IntIdTable() {
    val name = varchar("name", 255)
    val color = varchar("color", 255)
    val coordX = double("coordX")
    val coordY = double("coordY")
    val radius = double("radius")
//    val centrality = double("centrality")
}