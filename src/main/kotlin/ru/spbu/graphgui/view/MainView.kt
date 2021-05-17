package ru.spbu.graphgui.view

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
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
import ru.spbu.graphgui.community.CommunityDetection
import java.sql.Connection

class MainView : View("Graph") {
    private val numberOfIterationsProperty = intProperty(10000)
    private var numberOfIterations by numberOfIterationsProperty
    private var progressValueProperty = doubleProperty()
    private var progressValue by progressValueProperty
    private var countNodesProperty = intProperty(30)
    private var barnesHutThetaProperty = doubleProperty(1.2)
    private var gravityProperty = doubleProperty(1.0)
    private var jitterToleranceProperty = doubleProperty(1.0)
    private var linLogMode = false
    private var scalingRatioProperty = doubleProperty(2.0)
    private var strongGravityMode = false
    private var outboundAttractionDistribution = false
    private var randomGraph = false
    var fileJdbc = File("sql.sqlite")
    private var graphProperty = objectProperty<GraphView<String, Double>>()
    private var graph: GraphView<String, Double>? by graphProperty
    private var boolDirect = true
    private lateinit var sourcePath: String
    private lateinit var targetPath: String
    private var graphCreate = false
    private val toggleGroup = ToggleGroup()

    init {
        if (fileJdbc.exists()) {
            graphCreate = true
            graph = readJdbc()
            targetPath = "randomGraph.csv"
            csvSave(targetPath)
        }
    }

    private class BorderpaneWithDoubleValue(
        labelText: String,
        DefaultValue: String,
        doubleVariableProperty: DoubleProperty
    ) : View() {
        override val root = borderpane {
            left = label(labelText)
            right = textfield(DefaultValue) {
                textProperty().addListener { _, old, new ->
                    if (text != "")
                        textProperty().value = setDoubleToTextfield(new, old)
                }
                focusedProperty().addListener { _, _, new ->
                    if (!new) {
                        if (textProperty().value != "") {
                            doubleVariableProperty.value = setDoubleFromTextfield(text)
                            textProperty().value = doubleVariableProperty.value.toString()
                        } else {
                            doubleVariableProperty.value = DefaultValue.toDouble()
                            textProperty().value = DefaultValue
                        }
                    }
                }
            }
        }

        private fun setDoubleToTextfield(new: String, old: String): String =
            if (new.last() == 'd' || new.last() == 'f')
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
            else if (text.filter { c -> c == '.' } == "" && text != "")
                ("$text.0").toDouble()
            else
                ("${text}0").toDouble()
    }

    private class BorderpaneWithIntValue(
        labelText: String,
        DefaultValue: String,
        intVariableProperty: IntegerProperty
    ) : View() {
        override val root = borderpane {
            left = label(labelText)
            right = textfield(DefaultValue) {
                textProperty().addListener { _, old, new ->
                    if (textProperty().value != "")
                        textProperty().value = setIntToTextfield(new, old)
                }
                focusedProperty().addListener { _, _, new ->
                    if (!new) {
                        if (textProperty().value != "") {
                            intVariableProperty.value = textProperty().value.toInt()
                        } else {
                            intVariableProperty.value = DefaultValue.toInt()
                            textProperty().value = DefaultValue
                        }
                    }
                }
            }
        }

        private fun setIntToTextfield(new: String, old: String): String =
            if (new.toIntOrNull() != null)
                new
            else
                old
    }

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
                item("Save as csv").action {
                    saveFilePC()
                }
                item("Open file").action {
                    if (!chooseFilePC()) {
                        drawRandomGraph()
                    }
                }
                item("Save as JDBC", "Shortcut+S").action {
                    if (graphCreate) {
                        addJdbc()
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
                    add(BorderpaneWithDoubleValue("Radius of nodes", "6.0", graphSetting.vertex.radius))
                    add(BorderpaneWithDoubleValue("Width of lines", "1.0", graphSetting.edge.width))
                    hbox {
                        togglebutton("Directed", toggleGroup)
                        togglebutton("Undirected", toggleGroup)
                    }
                    add(BorderpaneWithIntValue("Max count of nodes", "30", countNodesProperty))
                    add(
                        BorderpaneWithDoubleValue(
                            "Probability of \nedge creation",
                            "0.5",
                            graphSetting.graph.probabilityOfCreationAnEdge
                        )
                    )
                    button("Create random graph") {
                        action {
                            targetPath = "randomGraph.csv"
                            graph = GraphView(graphSetting.createRandomGraph(countNodesProperty.value))
                            csvSave(targetPath)
                            graphCreate = true
                            randomGraph = true
                            drawRandomGraph()
                        }
                    }
                    button("Create random tree graph") {
                        action {
                            targetPath = "randomGraph.csv"
                            graph = GraphView(graphSetting.createRandomGraphTree(countNodesProperty.value))
                            csvSave(targetPath)
                            graphCreate = true
                            randomGraph = true
                            drawRandomGraph()
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
                            runAsync() {
                                detectCommunities()
                            } success {}
                        }
                    }
                    separator(Orientation.HORIZONTAL)
                    add(BorderpaneWithIntValue("Number of iteration", "10000", numberOfIterationsProperty))
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
                    add(BorderpaneWithDoubleValue("Gravity", "1.0", gravityProperty))
                    add(BorderpaneWithDoubleValue("Jitter tolerance", "1.0", jitterToleranceProperty))
                    add(BorderpaneWithDoubleValue("Scaling ratio", "2.0", scalingRatioProperty))
                    add(BorderpaneWithDoubleValue("Barnes hut theta", "1.2", barnesHutThetaProperty))
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
    }

    private fun chooseFilePC(): Boolean {
        val file = chooseFile(
            "Выбрать файл",
            arrayOf(FileChooser.ExtensionFilter("CSV", "*.csv")),
            null,
            FileChooserMode.Single,
            currentWindow
        )
        if (file.isEmpty()) {
            return true
        }
        graph = GraphView(graphSetting.readGraph(file.first()))
        sourcePath = file.first().toString()
        graphCreate = true
        randomGraph = false
        return false
    }

    private fun saveFilePC() {
        val file = chooseFile(
            "Выбрать файл",
            arrayOf(FileChooser.ExtensionFilter("CSV", "*.csv")),
            null,
            FileChooserMode.Save,
            currentWindow
        )
        if (file.isEmpty()) {
            return
        }
        targetPath = file.first().toString()
        csvSave(targetPath)
    }

    private fun csvSave(targetPath: String) {
        val writer = PrintWriter(targetPath)
        writer.print("Source,Target,Type,Id,Label,timeset,Weight\n")
        var count = 1
        for (i in graph!!.edgesVertex())
            writer.print("${i.vertices.first},${i.vertices.second},Undirected, ${count++},,,${i.weight})\n")
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
                edgeWeights.addLast(i.weight)
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
                edgeWeight.addLast(i.weight)
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
                    else -> setColor(i, Color.PURPLE)
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

    private fun addJdbc() {
        Database.connect("jdbc:sqlite:sql.sqlite", "org.sqlite.JDBC").also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Nodes, Edges)
            SchemaUtils.create(Nodes, Edges)
            Nodes.batchInsert(graph!!.vertices()) {
                this[Nodes.id] = it.vertex
                this[Nodes.name] = it.vertex
                this[Nodes.color] = it.color.toString()
                this[Nodes.coordX] = it.position.first
                this[Nodes.coordY] = it.position.second
                this[Nodes.radius] = it.radius
            }
            Edges.batchInsert(graph!!.edgesVertex()) {
                this[Edges.sourceNode] = it.vertices.first
                this[Edges.targetNode] = it.vertices.second
                this[Edges.weight] = it.weight
            }
        }
    }

    private fun readJdbc(): GraphView<String, Double> {
        Database.connect("jdbc:sqlite:sql.sqlite", "org.sqlite.JDBC").also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
        return transaction {
            val graph = ru.spbu.graphgui.model.Graph<String, Double>().apply {
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
                            graphSetting.vertex.radius.value = it[Nodes.radius]
                            break
                        }
                    }
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

