package ru.spbu.graphgui.view

import ru.spbu.graphgui.view.JDBC.*
import javafx.geometry.Orientation
import javafx.scene.control.MenuBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.openide.util.Lookup
import ru.spbu.graphgui.centrality.BetweennessCenralityWeightedDirected
import ru.spbu.graphgui.centrality.BetweennessCenralityWeightedUnidirected
import tornadofx.*
import java.io.*
import kotlin.math.floor
import kotlin.random.Random
import kotlin.system.exitProcess
import org.gephi.graph.api.Node as GephiNode
import org.jetbrains.exposed.sql.*
import ru.spbu.graphgui.community.CommunityDetection
import javax.xml.transform.Source


private const val dbPath = "exposed_database.db"

class MainView : View("Graph") {

    private val delete by lazy { ("DELETE FROM Edges;") }
    private val numberOfIterationsProperty = intProperty(10000)
    private var numberOfIterations by numberOfIterationsProperty
    private var progressValueProperty = doubleProperty()
    private var progressValue by progressValueProperty
    private var tableAvailability = false
    //    private var countNodesProperty = intProperty(30)
    private var countNodes = 30
    private var barnesHutThetaProperty = doubleProperty(1.2)
    private var gravity = 1.0
    private var jitterToleranceProperty = doubleProperty(1.0)
    private var linLogMode = false
    private var scalingRatioProperty = doubleProperty(2.0)
    private var strongGravityMode = false
    private var outboundAttractionDistribution = false
    private var randomGraph = false
    private var graphProperty = objectProperty<GraphView<String, Double>>()
    private var graph: GraphView<String, Double>? by graphProperty
    private var boolDirect = true
    private lateinit var sourcePath: String
    private lateinit var targetPath: String
    private var graphCreate = false
    private val toggleGroup = ToggleGroup()
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
                item("Save", "Shortcut+S").action {
                    saveFilePC()
                }
                item("Open file").action {
                    chooseFilePC()
                    drawRandomGraph()
                }
                item("JDBC").action {
                    if (!tableAvailability){
                        tableAvailability = true
                        addJdbc()
                    } else {

                    }
                }
            }
        }
        left = hbox {
            vbox(10.0) {
                vbox(5) {
                    checkbox("Show vertices Id", graphSetting.vertex.label) {
                        action {
                            println("vertex labels are ${if (isSelected) "enabled" else "disabled"}")
                        }
                    }
//                      checkbox("Show edges labels", graphSetting.edge.label) {
//                          action {
//                               println("edges labels are ${if (isSelected) "enabled" else "disabled"}")
//                          }
//                      }
                    borderpane {
                        left = label("Radius of nodes")
                        right = textfield("6.0") {
                            graphSetting.vertex.radius.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }
                    }
                    hbox {
                        togglebutton("Directed", toggleGroup)
                        togglebutton("Undirected", toggleGroup)
                    }
//                    button {
//                        this.text("DIRECTED (CLICK TO CHANGE)")
//                        action {
//                            if (boolDirect)
//                                this.text("UNDIRECTED (CLICK TO CHANGE)")
//                            else
//                                this.text("DIRECTED (CLICK TO CHANGE)")
//                            boolDirect = !boolDirect
//                        }
//                    }
                    borderpane {
                        left = label("Max count of nodes")
//                          textfield("30") {
//                              countNodesProperty.bind(textProperty().integerBinding { it!!.toInt() })
//                          }
                        right = textfield("30") {
                            textProperty().addListener { _, old, new ->
                                countNodes = setIntFromTextfield(new, old)
                                if (countNodes != 0)
                                    textProperty().value = countNodes.toString()
                            }
                        }
                    }
                    borderpane {
                        left = label("Probability of \nedge creation")
                        right = textfield("0.5") {
                            graphSetting.graph.probabilityOfCreationAnEdge.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }
                    }
                    button("Create random graph") {
                        action {
                            //runAsync {
                            targetPath = "randomGraph.csv"
                            graph = GraphView(graphSetting.createRandomGraph(countNodes))
                            csvSave(targetPath)
                            graphCreate = true
                            randomGraph = true
                            drawRandomGraph()
                            //} ui {}
                        }
                    }
                    separator(Orientation.HORIZONTAL)
                    button("Calculate Betweenness Centrality") {
                        action {
                            runAsync {
                                calculateBetweennessCentrality()
                            } success {}

                        }
                    }
                    button("Detect communities") {
                        action {
                            detectCommunities()
                        }
                    }
                    separator(Orientation.HORIZONTAL)
                    borderpane {
                        left = label("Number of iteration")
                        right = textfield("10000") {
                            textProperty().addListener { _, old, new ->
                                numberOfIterations = setIntFromTextfield(new, old)
                                if (numberOfIterations != 0)
                                    textProperty().value = numberOfIterations.toString()
                            }
//                    numberOfIterationsProperty.bind(textProperty().integerBinding { it!!.toInt() })
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
                    borderpane {
                        left = label("Gravity")
                        right = textfield("1.0") {
                            textProperty().addListener { _, old, new ->
                                textProperty().value = setDoubleToTextfield(new, old)
                            }
                            focusedProperty().addListener { _, _, new ->
                                if (!new) {
                                    gravity = setDoubleFromTextfield(text)
                                    textProperty().value = gravity.toString()
                                }
                            }
//                            gravity.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }

                    }
                    borderpane {
                        left = label("Jitter tolerance")
                        right = textfield("1.0") {
                            jitterToleranceProperty.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }
                    }
                    borderpane {
                        left = label("Scaling ratio")
                        right = textfield("2.0") {
                            scalingRatioProperty.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }
                    }
                    borderpane {
                        left = label("Barnes hut theta")
                        right = textfield("1.2") {
//                    textProperty().addListener { _, old, new ->
//                        intermediateVariable = setDoubleFromTextfield(new, old)
//                        if (intermediateVariable != 0.0 && intermediateVariable) {
//                            barnesHutTheta = intermediateVariable
//                            textProperty().value = intermediateVariable.toString()
//                        }
//                    }
                            barnesHutThetaProperty.bind(textProperty().doubleBinding { it!!.toDouble() })
                        }
                    }
                    button("Make layout") {
                        action {
                            if (graphCreate) {
                                runAsync {
                                    if (randomGraph) {
                                        makeLayout(targetPath)
                                    } else {
                                        makeLayout(sourcePath)
                                    }
                                } ui {}
                            }
                        }
                    }
                    progressbar(progressValueProperty) {
                        progressValueProperty
                    }
                }
            }
            separator(Orientation.VERTICAL)
        }
//            separator(Orientation.VERTICAL)
    }

    private fun setIntFromTextfield(new: String, old: String): Int =
        if (new.toIntOrNull() != null && new.isNotEmpty())
            new.toInt()
        else if (new.isEmpty())
            0
        else
            old.toInt()

    private fun setDoubleToTextfield(new: String, old: String): String =
        if (new.last() == 'd' ||  new.last() == 'f')
            old
        else if (new.toDoubleOrNull() != null)
            new
        else if (new.toIntOrNull() != null && new.filter { c -> c == '.' } == "")
            new
        else if (new.last() == '.' && new.filter { c -> c == '.' } == ".")
            new
        else
            old

    private fun setDoubleFromTextfield(text: String): Double =
        if (text.toDoubleOrNull() != null)
            text.toDouble()
        else if (text.filter { c -> c == '.' } == "")
            ("$text.0").toDouble()
        else if (text.last() == '.' && text.filter { c -> c == '.' } == ".")
            ("${text}0").toDouble()
        else
            0.0

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
        for (i in graph!!.edgesVertex())
            writer.print("${i.vertices.first},${i.vertices.second},Undirected, ${count++},,,${i.element})\n")
        writer.flush()
        writer.close()
    }

    private fun drawRandomGraph() {
        var numberOfNodes = graph!!.vertices.size * 3
        if (numberOfNodes < 100) {
            numberOfNodes = 100
        }
        for (y in graph!!.vertices.values) {
            y.position =
                Pair((2 * Random.nextDouble() - 1) * numberOfNodes, (2 * Random.nextDouble() - 1) * numberOfNodes)
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

    private fun detectCommunities() {
        val sourceVertex = ArrayDeque<String>()
        val targetVertex = ArrayDeque<String>()
        val edgeWeights = ArrayDeque<Double>()

        if (graph != null) {
            for (i in graph!!.edgesVertex()) {
                sourceVertex.addLast(i.vertices.first)
                targetVertex.addLast(i.vertices.second)
                edgeWeights.addLast(i.element)
            }

            val communities = CommunityDetection().detectCommunities(sourceVertex, targetVertex, edgeWeights)
            val communitiesColors = hashMapOf<Int, Color>()

            val clrRandom = Random(99) // всегда с того же числа, чтобы получать тот же порядок цветов
            for (i in communities) {
                if (!communitiesColors.contains(i.value)) {
                    communitiesColors[i.value] = Color(
                        clrRandom.nextDouble(),
                        clrRandom.nextDouble(),
                        clrRandom.nextDouble(),
                        1.0
                    )
                }

                for (j in graph!!.vertices()) {
                    if (i.key == j.vertex) {
                        j.color = communitiesColors[i.value]!!
                    }
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

    fun addJdbc() {
        val connection = Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Nodes)
            SchemaUtils.create(Edges)
            graph?.let {
                for (i in it.vertices()) {
                    Node.new {
                        name = i.vertex
                        coordX = i.position.first
                        coordY = i.position.second
                        color = i.color.toString()
                        radius = i.radius
                    }
                }
            }
            graph?.let {
                for (i in it.edgesVertex()) {
                    val eg = Edge.new {
                        sourceNode = i.vertices.first
                        targetNode = i.vertices.second
                        direction = if (boolDirect) {
                            "Directed"
                        } else {
                            "Undirected"
                        }
                        weight = i.element
                    }
                    eg.delete()
                }
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
        layout.barnesHutTheta = barnesHutThetaProperty.value
        layout.jitterTolerance = jitterToleranceProperty.value
        layout.isLinLogMode = linLogMode
        layout.scalingRatio = scalingRatioProperty.value
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
                progressValue = percent / 100.0
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

