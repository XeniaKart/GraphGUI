package ru.spbu.graphgui.view.JDBC

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


object Nodes : IdTable<String>() {
    override val id = varchar("id", 255).entityId()
    val name = varchar("name", 255)
    val color = varchar("color", 255)
    val coordX = double("coordX")
    val coordY = double("coordY")
    val radius = double("radius")
}

object Edges : Table() {
    val sourceNode = reference("Source", Nodes)
    val targetNode = reference("Target", Nodes)
    //    val direction = varchar("direction", 255)
    val weight = double("weight")
}