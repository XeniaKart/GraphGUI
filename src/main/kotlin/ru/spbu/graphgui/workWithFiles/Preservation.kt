package ru.spbu.graphgui.workWithFiles

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.spbu.graphgui.dataBase.Edges
import ru.spbu.graphgui.dataBase.Nodes
import ru.spbu.graphgui.view.GraphView
import java.io.PrintWriter
import java.sql.Connection

fun saveSql(path: String, graph: GraphView<String, Double>?) {
    graph?.let {
        Database.connect("jdbc:sqlite:$path", "org.sqlite.JDBC").also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Nodes, Edges)
            SchemaUtils.create(Nodes, Edges)
            Nodes.batchInsert(graph.vertices()) {
                this[Nodes.id] = it.vertex
                this[Nodes.name] = it.vertex
                this[Nodes.color] = it.color.toString()
                this[Nodes.coordX] = it.position.first
                this[Nodes.coordY] = it.position.second
                this[Nodes.radius] = it.radius
            }
            Edges.batchInsert(graph.edgesVertex()) {
                this[Edges.sourceNode] = it.vertices.first
                this[Edges.targetNode] = it.vertices.second
                this[Edges.weight] = it.weight
            }
        }
    }
}

fun csvSave(targetPath: String, graph: GraphView<String, Double>?) {
    graph?.let {
        val writer = PrintWriter(targetPath)
        writer.print("Source,Target,Type,Id,Label,timeset,Weight\n")
        var count = 1
        for (i in graph.edgesVertex())
            writer.print("${i.vertices.first},${i.vertices.second},Undirected, ${count++},,,${i.weight})\n")
        writer.flush()
        writer.close()
    }
}