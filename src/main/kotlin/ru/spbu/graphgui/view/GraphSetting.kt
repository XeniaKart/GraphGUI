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

    object graph {
        val widthAndHeight = doubleProperty(500_000.0)
        var probabilityOfCreationAnEdge = doubleProperty(0.5)
    }

    object edge {
        val width =  doubleProperty(1.0)
        val label = booleanProperty()
    }

    fun createRandomGraph(number: Int): Graph<String, Double> = Graph<String, Double>().apply {
        for (i in (0 until number)) {
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
}
