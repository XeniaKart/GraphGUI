package ru.spbu.graphgui.view

import javafx.scene.control.MenuBar
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import org.gephi.graph.api.Graph
import org.gephi.graph.api.GraphController
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
import tornadofx.*
import java.io.*
import kotlin.math.floor
import kotlin.random.Random
import kotlin.system.exitProcess
import org.gephi.graph.api.Node as GephiNode


class MainView : View("Graph") {
    private val numberOfIterationsProperty = intProperty()
    private var numberOfIterations by numberOfIterationsProperty
    private var countNodes = 30
    private var barnesHutTheta = 1.2
    private var jitterTolerance = 1.0
    private var linLogMode = false
    private var scalingRatio = 2.0
    private var gravity = 1.0
    private var strongGravityMode = false
    private var outboundAttractionDistribution = false
    private var randomGraph = false
    private var graphProperty = objectProperty<GraphView<String, Double>>()
    private var graph: GraphView<String, Double>? by graphProperty
    private var boolDirect = true
    private lateinit var sourcePath: String
    private lateinit var targetPath: String
    private var graphCreate = false
    override val root = borderpane {
        centerProperty().bind(graphProperty.objectBinding { graph ->
            graph?.let {
                ScrollPane(it).apply {
                    isPannable = true
                    isFitToHeight = true
                    isFitToWidth = true
                    hvalue = 0.5
                    vvalue = 0.5
                    vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
//                    addEventFilter(ScrollEvent.SCROLL) { event -> event.consume() }
                }
            }
        })
        top = MenuBar().apply {
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
        left = VBox(10.0).apply {
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
                    drawRandomGraph()
                }
            }
            hbox(5) {
                label("Number of iteration:")
                textfield("10000") {
                    numberOfIterationsProperty.bind(textProperty().integerBinding { it!!.toInt() })
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
                            makeLayout(targetPath)
                        } else {
                            makeLayout(sourcePath)
                        }
                    }
                }
            }
        }
    }

    private fun chooseFilePC() {
        val file = chooseFile(
            "Выбрать файл",
            arrayOf(FileChooser.ExtensionFilter("CSV", "*.csv")),
            null,
            FileChooserMode.Single,
            currentWindow
        )
        graph = GraphView(graphSetting.readGraph(file.first()))
        sourcePath = file.first().toString()
        graphCreate = true
        randomGraph = false
    }

    private fun saveFilePC() {
        val file = chooseFile(
            "Выбрать файл",
            arrayOf(FileChooser.ExtensionFilter("CSV", "*.csv")),
            null,
            FileChooserMode.Save,
            currentWindow
        )
        targetPath = file.first().toString()
        csvSave(targetPath)
    }

    private fun csvSave(targetPath: String) {
        val writer = PrintWriter(targetPath)
        writer.print("Source,Target,Type,Id,Label,timeset,Weight\n")
        var count = 1
        var first: String
        var second: String
        for (i in graph!!.edgesVertex()) {
            first = i.vertices.first
            second = i.vertices.second
            writer.print("$first,$second,Undirected, $count,,,1\n")
            count++
        }
        writer.flush()
        writer.close()
    }

    private fun drawRandomGraph() {
        for (y in graph!!.vertices.values) {
            y.position = Pair(((Random.nextDouble() * 60)) * 5, (-((Random.nextDouble() * 60)) * 5))
        }
    }

    private fun makeLayout(path: String) {
        val graphForceAtlas2 = makeLayout2(path)
        for (z: Int in 0 until graph!!.vertices().size) {
            val n: GephiNode = graphForceAtlas2.nodes.drop(z).first()
            for (y: VertexView<String> in graph!!.vertices.values) {
                if (y.vertex == n.id.toString()) {
                    y.position = Pair(n.x().toDouble(), n.y().toDouble())
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
                targetVertex.addLast(i.vertices.second)
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
                intervals = (maximum - minimum) / 7
            }
            for (i in valueCentralities) {
                when {
                    i.value >= minimum!! && i.value < minimum + intervals -> setColor(i, Color.RED)
                    i.value >= minimum + intervals && i.value < minimum + 2 * intervals -> setColor(i, Color.ORANGE)
                    i.value >= minimum + 2 * intervals && i.value < minimum + 3 * intervals -> setColor(i, Color.YELLOW)
                    i.value >= minimum + 3 * intervals && i.value < minimum + 4 * intervals -> setColor(i, Color.GREEN)
                    i.value >= minimum + 4 * intervals && i.value < minimum + 5 * intervals -> setColor(i, Color.AQUA)
                    i.value >= minimum + 5 * intervals && i.value < minimum + 6 * intervals -> setColor(i, Color.BLUE)
                    i.value >= minimum + 6 * intervals && i.value < minimum + 7 * intervals -> setColor(i, Color.PURPLE)
                }
            }
        }
    }

    private fun setColor(i: MutableMap.MutableEntry<String, Double>, color: Color) {
        for (j in graph!!.vertices()) {
            if (i.key == j.vertex) {
                j.color = color
                break
            }
        }
    }

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

    private fun makeLayout2(sourcePath: String): Graph {

        val startTime = System.currentTimeMillis()
        val seed: Long? = null
        val threadCount = Runtime.getRuntime().availableProcessors()
        val formats: MutableSet<String?> = HashSet()
        val coordsFile: File? = null
        val file = File(sourcePath)
        if (!file.exists()) {
            System.err.println("$file not found.")
            exitProcess(1)
        }
        val output = "myGraph"
        if (numberOfIterations <= 0) {
            System.err.println("Number of iterations must be not positive!")
            exitProcess(1)
        }
        formats.add("gexf")
        formats.add("csv")
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
            val idToNode: MutableMap<Any, GephiNode> = java.util.HashMap()
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
        writeOutput(g, formats, output)
        val endTime = System.currentTimeMillis()
        println("Time = " + (endTime - startTime) / 1000.0 + "s")
        return g
    }
}

