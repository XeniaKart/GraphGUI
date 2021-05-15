package ru.spbu.graphgui.view

import ru.spbu.graphgui.model.Graph
import tornadofx.booleanProperty
import tornadofx.doubleProperty
import java.io.File
import java.io.PrintWriter
import kotlin.math.abs
import kotlin.random.Random
import kotlin.system.exitProcess


@Suppress("ClassName")
object graphSetting {

    object vertex {
        val radius = doubleProperty(6.0)
        val label = booleanProperty()
    }

    object edge {
        val label = booleanProperty()
    }

    fun createRandomGraph(number: Int): Graph<String, Double> = Graph<String, Double>().apply {
        for (i in (0..number)) {
            for (j in i + 1..number) {
                val a = abs(Random.nextInt() % 6)
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

//        addVertex("A")
//        addVertex("B")
//        addVertex("C")
//        addVertex("D")
//        addVertex("E")
//        addVertex("F")
//
//        addVertex("G")
//
//        addEdge("G", "F", 0.2)
//        addEdge("F", "D", 0.2)
//        addEdge("B", "C", 0.9)
//        addEdge("C", "D", 0.57)
//        addEdge("D", "B", 1.0)
//        addEdge("D", "E", 0.8)
//        addEdge("E", "G", 0.4)
//        addEdge("F", "E", 0.6)
//        addEdge("A", "B", 0.7)
//        addEdge("C", "A", 1.3)
//        addEdge("A", "D", 0.3)
    }


    fun readGraph(path: String): Graph<String, Double> = Graph<String, Double>().apply {
        val file = File(path)
        if (!file.exists()) {
            System.err.println("$file not found.")
            exitProcess(1)
        }
        val lines = file.readLines()

        for (line in lines.drop(1)) {
            val array = line.split(",")
//            println(array)
            addEdge(array[0], array[1], array[6].toDouble())
        }
    }

}
