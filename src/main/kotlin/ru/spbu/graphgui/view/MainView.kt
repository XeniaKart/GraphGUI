package ru.spbu.graphgui.view

//import java.util.*
//import jdk.internal.misc.Signal.handle

import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
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
import ru.spbu.graphgui.centrality.BetweennessCenralityWeightedDirected
import ru.spbu.graphgui.centrality.BetweennessCenralityWeightedUnidirected
import ru.spbu.graphgui.controller.Scroller
import tornadofx.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random
import kotlin.system.exitProcess


class MainView : View("Graph") {
    var countIterations = 10000
    var countNodes = 30
    var barnesHutTheta = 1.2
    var jitterTolerance = 1.0
    var linLogMode = false
    var scalingRatio = 2.0
    var gravity = 1.0
    var strongGravityMode = false
    var outboundAttractionDistribution = false
    var randomGraph = false
    var graph: GraphView<String, Double>? = null
    var boolDirect = true
    var sourcePath: String = "empty"
    var targetPath: String = "empty"
    var graphCreate = false
    override val root = vbox {
        var a = pane()
        separator(Orientation.HORIZONTAL)
        menubar {
            menu("File") {
                menu("Connect") {
                    item("Facebook").action { println("Connecting Facebook!") }
                    item("Twitter").action { println("Connecting Twitter!") }
                }
                item("Save", "Shortcut+S").action {
                    saveFilePC()
                }
                item("Quit", "Shortcut+Q").action {
                    println("Quitting!")
                }
                item("Open file").action {
                    chooseFilePC()
                    a.clear()
                    a.apply { add(graph!!) }
                    drawRandomGraph()
                }
            }
            menu("Edit") {
                item("Copy", "Shortcut+C").action {
                    println("Copying!")
                }
                item("Paste", "Shortcut+V").action {
                    println("Pasting!")
                }
            }
        }
        hbox(10) {
            vbox(10) {
                checkbox("Show vertices labels", graphSetting.vertex.label) {
                    action {
                        println("vertex labels are ${if (isSelected) "enabled" else "disabled"}")
                    }
                }
                checkbox("Show edges labels", graphSetting.edge.label) {
                    action {
                        println("edges labels are ${if (isSelected) "enabled" else "disabled"}")
                    }
                }
                button("Calculate Betweenness Centrality") {
                    action {
                        calculateBetweennessCentrality()
                    }
                }
                button {
                    this.text("DIRECTED (CLICK TO CHANGE)")
                    action {
                        boolDirect = if (boolDirect) {
                            this.text("UNDIRECTED (CLICK TO CHANGE)")
                            false
                        } else {
                            this.text("DIRECTED (CLICK TO CHANGE)")
                            true
                        }
                    }
                }
                hbox(5) {
                    label("Max count of nodes:")
                    textfield("30") {
                        action {
                            countNodes = this.text.toInt()
                        }
                    }
                }
                button("Create random graph") {
                    action {
                        targetPath = "randomGraph.csv"
                        graph = GraphView(graphSetting.createRandomGraph(countNodes))
                        csvSave(targetPath)
                        graphCreate = true
                        randomGraph = true
                        a.clear()
                        a.apply { add(graph!!) }
                        drawRandomGraph()
                    }
                }
                hbox(5) {
                    label("Number of iteration:")
                    textfield("10000") {
                        action {
                            countIterations = this.text.toInt()
                        }
                    }
                }
                checkbox("strongGravityMode") {
                    action {
                        strongGravityMode = !strongGravityMode
                    }
                }
                checkbox("LinLogMode") {
                    action {
                        linLogMode = !linLogMode
                    }
                }
                hbox(5) {
                    label("Gravity:")
                    textfield("1.0") {
                        action {
                            gravity = this.text.toDouble()
                        }
                    }
                }
                hbox(5) {
                    label("BarnesHutTheta:")
                    textfield("1.0") {
                        action {
                            barnesHutTheta = this.text.toDouble()
                        }
                    }
                }
                button("Make layout") {
                    action {
                        if (graphCreate) {
                            if (randomGraph) {
                                makeLayout(targetPath, a)
                            } else {
                                makeLayout(sourcePath, a)
                            }
                        }
                    }
                }
            }

            separator(Orientation.VERTICAL)

            a = pane {
                graph?.let { add(it) }
            }

            val scroller = find(Scroller::class)
            a.setOnScroll { e -> e?.let { scroller.scroll(it) } }
            a.setOnMouseEntered { e -> e?.let { scroller.entered(it) } }
            a.setOnMousePressed { e -> e?.let { scroller.pressed(it) } }
            a.setOnMouseDragged { e -> e?.let { scroller.dragged(it) } }
            a.setOnMouseReleased { e -> e?.let { scroller.released(it) } }
            a.setOnMouseExited { e -> e?.let { scroller.exited(it) } }
        }
    }

    fun chooseFilePC() {
        val fileChooser = FileChooser()
        fileChooser.title = "Выбрать файл";
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("CSV", "*.csv"))
        val file = fileChooser.showOpenDialog(primaryStage)
        if (file != null) {
            sourcePath = file.absolutePath
            println(sourcePath)
        }
        graph = GraphView(graphSetting.readGraph(sourcePath))
        graphCreate = true
        randomGraph = false
    }

    fun saveFilePC() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "Выбрать папку"
        val dir = directoryChooser.showDialog(primaryStage)
        if (dir != null) {
            targetPath = dir.absolutePath
            println(targetPath)
        } else {
            targetPath = "randomGraph.csv"
        }
        csvSave(targetPath)
    }

    fun csvSave(targetPath: String) {
        val writer = PrintWriter(targetPath)
        writer.print("Source,Target,Type,Id,Label,timeset,Weight\n")
        var count = 1
        var first = ""
        var second = ""
        for (i in graph!!.edgesVertex()) {
            first = i.vertices.first
            second = i.vertices.second
            writer.print("$first,$second,Undirected, $count,,,1\n")
            count++
        }
        writer.flush()
        writer.close()
    }

    fun drawRandomGraph() {
        for (y in graph!!.vertices.values) {
            y.position = Pair(((Random.nextDouble() * 60) + 30) * 5, (-((Random.nextDouble() * 60) + 30) * 5) + 500)
        }
    }

    fun makeLayout(path: String, a: Pane) {
        val graphForceAtlas2 = makeLayout2(path, a)
        for (z in 0 until graph!!.vertices().size) {
            val n = graphForceAtlas2.nodes.drop(z).first()
            for (y in graph!!.vertices.values) {
                if (y.vertex == n.id.toString()) {
                    y.position = Pair((n.x().toDouble() + 30) * 5, (-(n.y().toDouble() + 30) * 5) + 500)
                    break
                }
            }
        }
    }

    private fun calculateBetweennessCentrality() {
        if (graph != null) {
            val graphBeetwCent1: BetweennessCenralityWeightedDirected
            val graphBeetwCent2: BetweennessCenralityWeightedUnidirected
            val valueCentralities: HashMap<String, Double>
            val distinctVertex = ArrayDeque<String>()
            val sourceVertex = ArrayDeque<String>()
            val targetVertex = ArrayDeque<String>()
            val edgeWeight = ArrayDeque<Double>()
            for (i in graph!!.verticesKeys()) {
                distinctVertex.addLast(i)
            }
            for (i in graph!!.edgesVertex()) {
                sourceVertex.addLast(i.vertices.first)
                println(i.vertices.first)
                targetVertex.addLast(i.vertices.second)
            }
            for (i in graph!!.edgesVertex()) {
                edgeWeight.addLast(i.element)
            }
            if (boolDirect) {
                graphBeetwCent1 = BetweennessCenralityWeightedDirected()
                valueCentralities =
                    graphBeetwCent1.betweennessCentralityScoreDirected(
                        distinctVertex,
                        sourceVertex,
                        targetVertex,
                        edgeWeight
                    )
            } else {
                graphBeetwCent2 = BetweennessCenralityWeightedUnidirected()
                valueCentralities =
                    graphBeetwCent2.betweennessCentralityScoreUndirected(
                        distinctVertex,
                        sourceVertex,
                        targetVertex,
                        edgeWeight
                    )
            }
            val minimum = valueCentralities.values.minOrNull()
            val maximum = valueCentralities.values.maxOrNull()
            var intervals = 0.0
            if ((maximum != null) && (minimum != null)) {
                intervals = (maximum - minimum) / 3
            }
            for (i in valueCentralities) {
                if (i.value >= minimum!! && i.value < minimum + intervals) {
                    for (j in graph!!.vertices()) {
                        if (i.key == j.vertex) {
                            j.color = Color.ORANGE
                        }
                    }
                } else if (i.value >= minimum + intervals && i.value <= minimum + 2 * intervals) {
                    for (j in graph!!.vertices()) {
                        if (i.key == j.vertex) {
                            j.color = Color.BLUE
                        }
                    }
                } else {
                    for (j in graph!!.vertices()) {
                        if (i.key == j.vertex) {
                            j.color = Color.GREEN
                        }
                    }
                }
            }
        }
    }


    //private val argsMap: MutableMap<String, Arg> = LinkedHashMap()
    private fun writeOutput(g: Graph, formats: Set<String?>, output: String?) {
        try {
            // ExporterCSV, ExporterDL, ExporterGDF, ExporterGEXF, ExporterGML, ExporterGraphML, ExporterPajek, ExporterVNA, PDFExporter, PNGExporter, SVGExporter
            val ec = Lookup.getDefault().lookup(ExportController::class.java)
            for (format in formats) {
                if (format == "txt") {
                    val pw = PrintWriter(
                        FileWriter(
                            output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"
                        )
                    )
                    pw.print(
                        """
                            id	x	y

                            """.trimIndent()
                    )
                    for (n in g.nodes) {
                        pw.print(n.id)
                        pw.print("\t")
                        pw.print(n.x())
                        pw.print("\t")
                        pw.print(n.y())
                        pw.print("\n")
                    }
                    pw.close()
                } else {
                    ec.exportFile(
                        File(output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"),
                        ec.getExporter(format)
                    )
                }
            }
        } catch (x: IOException) {
            x.printStackTrace()
            exitProcess(1)
        }
    }

//private fun addArg(flag: String, description: String, not_boolean: Boolean, defaultValue: Any) {
//    argsMap["--" + flag.toLowerCase()] =
//        Arg(flag, description, not_boolean, "" + defaultValue)
//}
//
//private fun addArg(flag: String, description: String, not_boolean: Boolean) {
//    argsMap["--" + flag.toLowerCase()] =
//        Arg(flag, description, not_boolean, null)
//}
//
//private fun getArg(flag: String): String? {
//    val a = argsMap["--" + flag.toLowerCase()]
//    return a?.value
//}

    fun makeLayout2(sourcePath: String, a: Pane): Graph {

        val startTime = System.currentTimeMillis()
        var i = 0
        var nsteps = countIterations
        val targetChangePerNode = 0.0
        var targetSteps = 0
        val seed: Long? = null
        val threadCount = Runtime.getRuntime().availableProcessors()
        val formats: MutableSet<String?> = HashSet()
        val coordsFile: File? = null
//    var sourcePath: String
//    if (randomGraph){
//        sourcePath = "D:\\2сем\\javafxtornadofx2\\randomGraph.csv"
//    }else{
//        sourcePath = path
//    }
        val file = File(sourcePath)
        if (!file.exists()) {
            System.err.println("$file not found.")
            exitProcess(1)
        }
        val output = "myGraph"
//        nsteps = 10000
        if (nsteps == 0 && targetChangePerNode == 0.0) {
            System.err.println("Either --nsteps or --targetChangePerNode must be set!")
            exitProcess(1)
        }
        if (nsteps > 0 && targetChangePerNode > 0.0) {
            System.err.println("--nsteps and --targetChangePerNode are mutually exclusive!")
            exitProcess(1)
        }

        formats.add("gexf")
        formats.add("csv")
//    if (getArg("coords") != null) {
//        coordsFile = File(getArg("coords"))
//        if (!coordsFile.exists()) {
//            System.err.println("$coordsFile not found.")
//            System.exit(1)
//        }
//    }
//    TODO()
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
        val g: Graph = if (/*!getArg("directed").equals("true", ignoreCase = */true) {
            container.loader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED)
            graphModel.undirectedGraph
        } else {
            container.loader.setEdgeDefault(EdgeDirectionDefault.DIRECTED)
            graphModel.directedGraph
        }
        importController.process(container, DefaultProcessor(), workspace)
        val layout = ForceAtlas2(null)
        layout.setGraphModel(graphModel)
        val random = if (seed != null) java.util.Random(seed) else java.util.Random()
        for ((numNodes, node) in g.nodes.withIndex()) {
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
        layout.barnesHutTheta = barnesHutTheta
        layout.jitterTolerance = jitterTolerance
        layout.isLinLogMode = linLogMode
        layout.scalingRatio = scalingRatio
        layout.isStrongGravityMode = strongGravityMode
        layout.gravity = gravity
        layout.isOutboundAttractionDistribution = outboundAttractionDistribution
        layout.threadsCount = threadCount
        layout.initAlgo()
        //val _formats: Set<String?> = formats
        val _layout: Layout = layout
//        val distanceWriter = if (nsteps > 0) PrintWriter(FileWriter("$output.distances.txt")) else null
//        if (nsteps > 0) distanceWriter!!.print("step\tdistance\n")
        val shutdownThread: Thread = object : Thread() {
            override fun run() {
                _layout.endAlgo()
//                writeOutput(g, _formats, output)
//                distanceWriter?.close()
            }
        }
        Runtime.getRuntime().addShutdownHook(shutdownThread)
        if (nsteps > 0) {
            var lastPercent = 0
//            var distance: Double
            for (i in 0 until nsteps) {
                layout.goAlgo()
//                distance = layout.distance
//                distanceWriter!!.print(i)
//                distanceWriter.print("\t")
//                distanceWriter.print(distance)
//                distanceWriter.print("\n")
//                distanceWriter.flush()

                val percent = floor(100 * (i + 1.0) / nsteps).toInt()
                if (percent != lastPercent) {
                    print("*")
                    lastPercent = percent
                    if (percent % 25 == 0) {
                        println("$percent%")
                    }
                }
//                if (i % 10 == 0) {
////                    println ("debug")
//                    draw(g, a)
//                }
            }
        } else {
            nsteps = 0
//            var changePerNode: Double
//            do {
//                ++nsteps
//                layout.goAlgo()
//                changePerNode = layout.getDistance() / num_nodes
//                if (nsteps % 100 == 0) println("$nsteps iterations, change_per_node = $changePerNode")
//            } while (nsteps == 1 || changePerNode > targetChangePerNode && nsteps < targetSteps)
//            println("Finished in $nsteps iterations, change_per_node = $changePerNode")
        }
        Runtime.getRuntime().removeShutdownHook(shutdownThread)
        layout.endAlgo()
        writeOutput(g, formats, output)
//        distanceWriter?.close()
        val endTime = System.currentTimeMillis()
        println("Time = " + (endTime - startTime) / 1000.0 + "s")
        return g
    }
}
