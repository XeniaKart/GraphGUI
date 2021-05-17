package ru.spbu.graphgui.view

import ru.spbu.graphgui.model.Graph
import tornadofx.booleanProperty
import tornadofx.doubleProperty
import java.io.File
import kotlin.math.abs
import kotlin.random.Random
import kotlin.system.exitProcess

@Suppress("ClassName")
object graphSetting {

    object vertex {
        val radius = doubleProperty(6.0)
        val label = booleanProperty()
    }

    object graph {
        val widthAndHeight = doubleProperty(500_000.0)
        var probabilityOfCreationAnEdge = doubleProperty(0.5)
    }

    object edge {
        val width = doubleProperty(1.0)
        val label = booleanProperty()
    }

    fun createRandomGraphTree(number: Int): Graph<String, Double> = Graph<String, Double>().apply {
        var nextVertexID = 0
        var newVertices = ArrayDeque<String>()

        newVertices.addLast("0")
        nextVertexID++

        // этот граф похож на зонтики укропа
        while (true) {
            if (newVertices.size == 0 || nextVertexID >= number)
                break

            val newVertex = newVertices.removeFirst()
            val addVertices = Random.nextInt(5, 10)

            println("adding $addVertices edges from $newVertex")

            for (i in 0 until addVertices) {
                val newVertexID = nextVertexID++
                if (newVertexID >= number) {
                    break
                }
                val edgeWeight = abs(Random.nextDouble())
                addEdge(newVertex, newVertexID.toString(), edgeWeight)
                newVertices.addLast(newVertexID.toString())
            }
        }
    }

    fun createRandomGraph(number: Int): Graph<String, Double> = Graph<String, Double>().apply {
        for (i in (0 until number)) {
            addVertex(i.toString())
            for (j in i + 1 until number) {
                val a = abs(Random.nextInt() % (1.0 / graph.probabilityOfCreationAnEdge.value)).toInt()
                val b = abs(Random.nextInt() % 2)
                val m = abs(Random.nextDouble())
                if (a == 0) {
                    if (b == 0) {
                        addEdge(i.toString(), j.toString(), m)
                    } else {
                        addEdge(j.toString(), i.toString(), m)
                    }
                }
            }
        }
    }

    fun readGraph(file: File): Graph<String, Double> = Graph<String, Double>().apply {
        if (!file.exists()) {
            System.err.println("$file not found.")
            exitProcess(1)
        }
        val lines = file.readLines()

        for (line in lines.drop(1)) {
            val array = line.split(",")
            addEdge(array[0], array[1], array[6].toDouble())
        }
    }

//    private fun readJdbc() {
//        Database.connect("jdbc:sqlite:sql.sqlite", "org.sqlite.JDBC").also {
//            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
//        }
//        return transaction {
//            Graph<String, Double>().apply {
//                Nodes.selectAll().forEach {
//                    addVertex(
//                        it[Nodes.name]
//                    )
//                }
//                Edges.selectAll().forEach {
//                    addEdge(it[Edges.sourceNode].value, it[Edges.targetNode].value, it[Edges.weight])
//                }
//            }
//        }
//    }
}
