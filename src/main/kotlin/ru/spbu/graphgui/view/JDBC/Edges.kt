package ru.spbu.graphgui.view.JDBC

import org.jetbrains.exposed.dao.id.IntIdTable

object Edges : IntIdTable() {
    val sourceNode = varchar("Source", 255)
    val targetNode = varchar("Target", 255)
    val direction = varchar("direction", 255)
    val weight = double("weight")
}