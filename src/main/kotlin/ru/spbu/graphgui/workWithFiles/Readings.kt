package ru.spbu.graphgui.workWithFiles

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.spbu.graphgui.dataBase.Edges
import ru.spbu.graphgui.dataBase.Nodes
import ru.spbu.graphgui.model.Graph
import ru.spbu.graphgui.view.GraphView
import ru.spbu.graphgui.view.graphCreator
import tornadofx.c
import java.io.File
import java.sql.Connection

fun readSql(path: String): GraphView<String, Double> {
    Database.connect("jdbc:sqlite:$path", "org.sqlite.JDBC").also {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
    return transaction {
        val graph = Graph<String, Double>().apply {
            Nodes.selectAll().forEach {
                addVertex(it[Nodes.name])
            }
            Edges.selectAll().forEach {
                addEdge(it[Edges.sourceNode].value, it[Edges.targetNode].value, it[Edges.weight])
            }
        }
        GraphView(graph).apply {
            Nodes.selectAll().forEach {
                for (i in vertices()) {
                    if (it[Nodes.name] == i.vertex) {
                        i.position = Pair(it[Nodes.coordX], it[Nodes.coordY])
                        i.color = c(it[Nodes.color])
                        graphCreator.vertex.radius.value = it[Nodes.radius]
                        break
                    }
                }
            }
        }
    }
}

fun readGraph(file: File): Graph<String, Double> = Graph<String, Double>().apply {
    if (!file.exists()) {
        System.err.println("$file not found.")
    }
    val lines = file.readLines()
    for (line in lines.drop(1)) {
        val array = line.split(",")
        addEdge(array[0], array[1], array[6].toDouble())
    }
}