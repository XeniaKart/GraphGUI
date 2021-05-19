package ru.spbu.graphgui.view

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.geometry.Orientation
import javafx.scene.control.MenuBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import ru.spbu.graphgui.centrality.*
import tornadofx.*
import java.io.*
import org.gephi.graph.api.Node as GephiNode
import ru.spbu.graphgui.community.*
import ru.spbu.graphgui.workWithFiles.*
import ru.spbu.graphgui.layout.*

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
    var fileSql = File("sql.sqlite")
    private var graphProperty = objectProperty<GraphView<String, Double>>()
    private var graph: GraphView<String, Double>? by graphProperty
    private var boolDirect = true
    private lateinit var sourcePath: String
    private lateinit var targetPath: String
    private var graphCreate = false
    private val toggleGroup = ToggleGroup()

    init {
        if (fileSql.exists()) {
            graphCreate = true
            graph = readSql("sql.sqlite")
            targetPath = "lastGraph.csv"
            csvSave(targetPath, graph)
            sourcePath = "lastGraph.csv"
        }
        primaryStage.setOnCloseRequest {
            saveSql("sql.sqlite", graph)
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
                }
            }
        })
        top = MenuBar().apply {
            menu("File") {
                item("Save").action {
                    saveFilePC()
                }
                item("Open").action {
                    if (chooseFilePC().equals(".csv")) {
                        graphCreator.drawRandomGraph(graph)
                    }
                }
            }
        }
        left = hbox {
            vbox(10.0) {
                vbox(5) {
                    checkbox("Show vertices Id", graphCreator.vertex.label) {
                        action {
                            println("vertex labels are ${if (isSelected) "enabled" else "disabled"}")
                        }
                    }
                    add(BorderpaneWithDoubleValue("Radius of nodes", "6.0", graphCreator.vertex.radius))
                    add(BorderpaneWithDoubleValue("Width of lines", "1.0", graphCreator.edge.width))
                    hbox {
                        togglebutton("Directed", toggleGroup) {
                            action {
                                boolDirect = true
                            }
                        }
                        togglebutton("Undirected", toggleGroup) {
                            action {
                                boolDirect = false
                            }
                        }
                    }
                    add(BorderpaneWithIntValue("Max count of nodes", "30", countNodesProperty))
                    add(
                        BorderpaneWithDoubleValue(
                            "Probability of \nedge creation",
                            "0.5",
                            graphCreator.graph.probabilityOfCreationAnEdge
                        )
                    )
                    button("Create random graph") {
                        action {
                            targetPath = "randomGraph.csv"
                            graph = GraphView(graphCreator.createRandomGraph(countNodesProperty.value))
                            csvSave(targetPath, graph)
                            graphCreate = true
                            randomGraph = true
                            graphCreator.drawRandomGraph(graph)
                        }
                    }
                    button("Create random tree graph") {
                        action {
                            targetPath = "randomGraph.csv"
                            graph = GraphView(graphCreator.createRandomGraphTree(countNodesProperty.value))
                            csvSave(targetPath, graph)
                            graphCreate = true
                            randomGraph = true
                            graphCreator.drawRandomGraph(graph)
                        }
                    }
                    separator(Orientation.HORIZONTAL)
                    button("Calculate Betweenness Centrality") {
                        action {
                            runAsync {
                                calculateBetweennessCentality(graph, boolDirect)
                            } success {}

                        }
                    }
                    button("Detect communities") {
                        action {
                            runAsync {
                                detectCommunities(graph)
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
                                        makeLayoutAndDraw(targetPath)
                                    } else {
                                        makeLayoutAndDraw(sourcePath)
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

    private fun chooseFilePC(): String {
        val file = chooseFile(
            "Choose file",
            arrayOf(
                FileChooser.ExtensionFilter("CSV", "*.csv"),
                FileChooser.ExtensionFilter("SQLite", "*.sqlite")
            ),
            null,
            FileChooserMode.Single,
            currentWindow
        )
        if (file.isEmpty()) {
            return "empty"
        }
        sourcePath = file.first().toString()
        if (sourcePath.drop(sourcePath.length - 4).equals(".csv")) {
            graph = GraphView(readGraph(file.first()))
            graphCreate = true
            randomGraph = false
            return ".csv"
        } else {
            graph = readSql(file.first().toString())
            graphCreate = true
            randomGraph = false
            return ".sqlite"
        }
    }

    private fun saveFilePC() {
        val file = chooseFile(
            "Choose directory",
            arrayOf(
                FileChooser.ExtensionFilter("CSV", "*.csv"),
                FileChooser.ExtensionFilter("SQLite", "*.sqlite")
            ),
            null,
            FileChooserMode.Save,
            currentWindow
        )
        if (file.isEmpty()) {
            return
        }
        if (graphCreate) {
            targetPath = file.first().toString()
            if (targetPath.drop(targetPath.length - 4).equals(".csv")) {
                csvSave(targetPath, graph)
            } else {
                saveSql(targetPath, graph)
            }
        }
    }

    private fun makeLayoutAndDraw(path: String) {
        val graphForceAtlas2 = makeLayout(
            path,
            numberOfIterations,
            outboundAttractionDistribution,
            progressValueProperty,
            barnesHutThetaProperty,
            gravityProperty,
            jitterToleranceProperty,
            scalingRatioProperty,
            strongGravityMode,
            linLogMode
        )
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
}

//    private fun writeOutput(g: Graph, formats: Set<String?>, output: String?) {
//        try {
//            val ec = Lookup.getDefault().lookup(ExportController::class.java)
//            for (format in formats) {
//                ec.exportFile(
//                    File(output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"),
//                    ec.getExporter(format)
//                )
//            }
//        } catch (x: IOException) {
//            x.printStackTrace()
//        }
//    }
//
//    private fun makeLayout(sourcePath: String): Graph {
//        val startTime = System.currentTimeMillis()
//        val seed: Long? = null
//        val threadCount = Runtime.getRuntime().availableProcessors()
//        val formats: MutableSet<String?> = HashSet()
//        val coordsFile: File? = null
//        val file = File(sourcePath)
//        if (!file.exists()) {
//            System.err.println("$file not found.")
//        }
//        val output = "myGraph"
//        if (numberOfIterations <= 0) {
//            System.err.println("Number of iterations must be not positive!")
//        }
//        formats.add("gexf")
//        formats.add("csv")
//        if (formats.size == 0) {
//            formats.add("txt")
//        }
//        val pc = Lookup.getDefault().lookup(ProjectController::class.java)
//        pc.newProject()
//        val workspace = pc.currentWorkspace
//        val importController = Lookup.getDefault().lookup(
//            ImportController::class.java
//        )
//        val graphModel = Lookup.getDefault().lookup(GraphController::class.java).graphModel
//        val container = importController.importFile(file)
//        val g: Graph = run {
//            container.loader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED)
//            graphModel.undirectedGraph
//        }
//        importController.process(container, DefaultProcessor(), workspace)
//        val layout = ForceAtlas2(null)
//        layout.setGraphModel(graphModel)
//        val random = if (seed != null) java.util.Random(seed) else java.util.Random()
//        for (node in g.nodes) {
//            node.setX(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
//            node.setY(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
//        }
//        if (coordsFile != null) {
//            val idToNode: MutableMap<Any, GephiNode> = java.util.HashMap()
//            for (n in g.nodes) {
//                idToNode[n.id] = n
//            }
//            val br = BufferedReader(FileReader(coordsFile))
//            var sep = "\t"
//            var s = br.readLine()
//            for (test in arrayOf("\t", ",")) {
//                if (s.indexOf(test) != -1) {
//                    sep = test
//                    break
//                }
//            }
//            val header = listOf(*s.split(sep.toRegex()).toTypedArray())
//            val idIndex = header.indexOf("id")
//            val xIndex = header.indexOf("x")
//            val yIndex = header.indexOf("y")
//            while (br.readLine().also { s = it } != null) {
//                val tokens = s.split(sep.toRegex()).toTypedArray()
//                val id = tokens[idIndex]
//                val n = idToNode[id]
//                if (n != null) {
//                    n.setX(tokens[xIndex].toFloat())
//                    n.setY(tokens[yIndex].toFloat())
//                } else {
//                    System.err.println("$id not found")
//                }
//            }
//            br.close()
//        }
//        layout.barnesHutTheta = barnesHutThetaProperty.value
//        layout.jitterTolerance = jitterToleranceProperty.value
//        layout.isLinLogMode = linLogMode
//        layout.scalingRatio = scalingRatioProperty.value
//        layout.isStrongGravityMode = strongGravityMode
//        layout.gravity = gravityProperty.value
//        layout.isOutboundAttractionDistribution = outboundAttractionDistribution
//        layout.threadsCount = threadCount
//        layout.initAlgo()
//        val layout1: Layout = layout
//        val shutdownThread: Thread = object : Thread() {
//            override fun run() {
//                layout1.endAlgo()
//            }
//        }
//        Runtime.getRuntime().addShutdownHook(shutdownThread)
//        if (numberOfIterations > 0) {
//            var lastPercent = 0
//            for (iteration in 0 until numberOfIterations) {
//                layout.goAlgo()
//                val percent = floor(100 * (iteration + 1.0) / numberOfIterations).toInt()
//                progressValue = percent / 100.0
//                if (percent != lastPercent) {
//                    print("*")
//                    lastPercent = percent
//                    if (percent % 25 == 0) {
//                        println("$percent%")
//                    }
//                }
//            }
//        }
//        Runtime.getRuntime().removeShutdownHook(shutdownThread)
//        layout.endAlgo()
//        writeOutput(g, formats, output)
//        val endTime = System.currentTimeMillis()
//        println("Time = " + (endTime - startTime) / 1000.0 + "s")
//        return g
//    }
//}

