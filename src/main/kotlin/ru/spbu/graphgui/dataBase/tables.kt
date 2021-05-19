package ru.spbu.graphgui.dataBase

import org.jetbrains.exposed.dao.id.IdTable
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
    val weight = double("weight")
}