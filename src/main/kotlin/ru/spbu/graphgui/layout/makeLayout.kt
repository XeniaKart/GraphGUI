package ru.spbu.graphgui.layout

import javafx.beans.property.DoubleProperty
import org.gephi.graph.api.Graph
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.Node
import org.gephi.io.exporter.api.ExportController
import org.gephi.io.importer.api.EdgeDirectionDefault
import org.gephi.io.importer.api.ImportController
import org.gephi.io.processor.plugin.DefaultProcessor
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import org.gephi.layout.spi.Layout
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlin.math.floor

private fun writeOutput(formats: Set<String?>, output: String?) {
    try {
        val ec = Lookup.getDefault().lookup(ExportController::class.java)
        for (format in formats) {
            ec.exportFile(
                File(output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"),
                ec.getExporter(format)
            )
        }
    } catch (x: IOException) {
        x.printStackTrace()
    }
}

fun makeLayout(
    sourcePath: String,
    numberOfIterations: Int,
    outboundAttractionDistribution: Boolean,
    progressValueProperty: DoubleProperty,
    barnesHutThetaProperty: DoubleProperty,
    gravityProperty: DoubleProperty,
    jitterToleranceProperty: DoubleProperty,
    scalingRatioProperty: DoubleProperty,
    strongGravityMode: Boolean,
    linLogMode: Boolean
): Graph {
    val startTime = System.currentTimeMillis()
    val seed: Long? = null
    val threadCount = Runtime.getRuntime().availableProcessors()
    val formats: MutableSet<String?> = HashSet()
    val coordsFile: File? = null
    val file = File(sourcePath)
    if (!file.exists()) {
        System.err.println("$file not found.")
    }
    val output = "myGraph"
    if (numberOfIterations <= 0) {
        System.err.println("Number of iterations must be not positive!")
    }
    formats.add("gexf")
    formats.add("csv")
    if (formats.size == 0) {
        formats.add("txt")
    }
    val pc = Lookup.getDefault().lookup(ProjectController::class.java)
    pc.newProject()
    val workspace = pc.currentWorkspace
    val importController = Lookup.getDefault().lookup(
        ImportController::class.java
    )
    val graphModel = Lookup.getDefault().lookup(GraphController::class.java).graphModel
    val container = importController.importFile(file)
    val g: Graph = run {
        container.loader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED)
        graphModel.undirectedGraph
    }
    importController.process(container, DefaultProcessor(), workspace)
    val layout = ForceAtlas2(null)
    layout.setGraphModel(graphModel)
    val random = if (seed != null) java.util.Random(seed) else java.util.Random()
    for (node in g.nodes) {
        node.setX(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
        node.setY(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
    }
    if (coordsFile != null) {
        val idToNode: MutableMap<Any, Node> = java.util.HashMap()
        for (n in g.nodes) {
            idToNode[n.id] = n
        }
        val br = BufferedReader(FileReader(coordsFile))
        var sep = "\t"
        var s = br.readLine()
        for (test in arrayOf("\t", ",")) {
            if (s.indexOf(test) != -1) {
                sep = test
                break
            }
        }
        val header = listOf(*s.split(sep.toRegex()).toTypedArray())
        val idIndex = header.indexOf("id")
        val xIndex = header.indexOf("x")
        val yIndex = header.indexOf("y")
        while (br.readLine().also { s = it } != null) {
            val tokens = s.split(sep.toRegex()).toTypedArray()
            val id = tokens[idIndex]
            val n = idToNode[id]
            if (n != null) {
                n.setX(tokens[xIndex].toFloat())
                n.setY(tokens[yIndex].toFloat())
            } else {
                System.err.println("$id not found")
            }
        }
        br.close()
    }
    layout.barnesHutTheta = barnesHutThetaProperty.value
    layout.jitterTolerance = jitterToleranceProperty.value
    layout.isLinLogMode = linLogMode
    layout.scalingRatio = scalingRatioProperty.value
    layout.isStrongGravityMode = strongGravityMode
    layout.gravity = gravityProperty.value
    layout.isOutboundAttractionDistribution = outboundAttractionDistribution
    layout.threadsCount = threadCount
    layout.initAlgo()
    val layout1: Layout = layout
    val shutdownThread: Thread = object : Thread() {
        override fun run() {
            layout1.endAlgo()
        }
    }
    Runtime.getRuntime().addShutdownHook(shutdownThread)
    if (numberOfIterations > 0) {
        var lastPercent = 0
        for (iteration in 0 until numberOfIterations) {
            layout.goAlgo()
            val percent = floor(100 * (iteration + 1.0) / numberOfIterations).toInt()
            progressValueProperty.value = percent / 100.0
            if (percent != lastPercent) {
                print("*")
                lastPercent = percent
                if (percent % 25 == 0) {
                    println("$percent%")
                }
            }
        }
    }
    Runtime.getRuntime().removeShutdownHook(shutdownThread)
    layout.endAlgo()
    writeOutput(formats, output)
    val endTime = System.currentTimeMillis()
    println("Time = " + (endTime - startTime) / 1000.0 + "s")
    return g
}
