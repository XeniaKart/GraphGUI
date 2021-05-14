package ru.spbu.graphgui
/*
import view.MainView
import org.gephi.graph.api.Model.Graph
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.Node
import org.gephi.graph.api.NodeIterable
import org.gephi.io.exporter.api.ExportController
import org.gephi.io.importer.api.EdgeDirectionDefault
import org.gephi.io.importer.api.ImportController
import org.gephi.io.processor.plugin.DefaultProcessor
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import org.gephi.layout.spi.Layout
import org.gephi.project.api.ProjectController
import org.openide.util.Lookup
import java.io.*
import java.util.*

private val argsMap: MutableMap<String, Arg> = LinkedHashMap()
private fun writeOutput(g: Model.Graph, formats: Set<String?>, output: String?) {
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
        System.exit(1)
    }
}

private fun addArg(flag: String, description: String, not_boolean: Boolean, defaultValue: Any) {
    argsMap["--" + flag.toLowerCase()] =
        Arg(flag, description, not_boolean, "" + defaultValue)
}

private fun addArg(flag: String, description: String, not_boolean: Boolean) {
    argsMap["--" + flag.toLowerCase()] =
        Arg(flag, description, not_boolean, null)
}

private fun getArg(flag: String): String? {
    val a = argsMap["--" + flag.toLowerCase()]
    return a?.value
}

fun makeLayout2(sourcePath: String): Model.Graph {

    val startTime = System.currentTimeMillis()
    var i = 0
    var nsteps = 0
    var targetChangePerNode = 0.0
    var targetSteps = 0
    var seed: Long? = null
    var threadCount = Runtime.getRuntime().availableProcessors()
    var barnesHutTheta: Double? = null
    var jitterTolerance: Double? = null
    var linLogMode: Boolean? = null
    var scalingRatio: Double? = null
    var strongGravityMode: Boolean? = null
    var gravity: Double? = null
    var outboundAttractionDistribution: Boolean? = null
    val formats: MutableSet<String?> = HashSet()
    var coordsFile: File? = null
    var sourcePath: String
    if (randomGraph){
        sourcePath = "D:\\2сем\\javafxtornadofx2\\randomGraph.csv"
    }else{
        sourcePath = path
    }
    val file = File(sourcePath)
    if (!file.exists()) {
        System.err.println("$file not found.")
        System.exit(1)
    }
    val output = "D:\\2сем\\javafxtornadofx2\\myGraph"
    nsteps = 10000
    if (nsteps == 0 && targetChangePerNode == 0.0) {
        System.err.println("Either --nsteps or --targetChangePerNode must be set!")
        System.exit(1)
    }
    if (nsteps > 0 && targetChangePerNode > 0.0) {
        System.err.println("--nsteps and --targetChangePerNode are mutually exclusive!")
        System.exit(1)
    }

    barnesHutTheta = 1.2
    jitterTolerance = 1.0
    linLogMode = false
    scalingRatio = 2.0
    gravity = 1.0
    strongGravityMode = false
    outboundAttractionDistribution = false
    formats.add("gexf")
    formats.add("csv")
    if (getArg("coords") != null) {
        coordsFile = File(getArg("coords"))
        if (!coordsFile.exists()) {
            System.err.println("$coordsFile not found.")
            System.exit(1)
        }
    }
    TODO()
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
    val g: Model.Graph = if (/*!getArg("directed").equals("true", ignoreCase = */true) {
        container.loader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED)
        graphModel.undirectedGraph
    } else {
        container.loader.setEdgeDefault(EdgeDirectionDefault.DIRECTED)
        graphModel.directedGraph
    }
    importController.process(container, DefaultProcessor(), workspace)
    val layout = ForceAtlas2(null)
    layout.setGraphModel(graphModel)
    val random = if (seed != null) Random(seed) else Random()
    var num_nodes = 0
    for (node in g.nodes) {
        ++num_nodes
        node.setX(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
        node.setY(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
    }
    if (coordsFile != null) {
        val idToNode: MutableMap<Any, Node> = HashMap()
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
        val header = Arrays.asList(*s.split(sep.toRegex()).toTypedArray())
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
    layout.setBarnesHutTheta(barnesHutTheta)
    layout.setJitterTolerance(jitterTolerance)
    layout.setLinLogMode(linLogMode)
    layout.setScalingRatio(scalingRatio)
    layout.setStrongGravityMode(strongGravityMode)
    layout.setGravity(gravity)
    layout.setOutboundAttractionDistribution(outboundAttractionDistribution)
    layout.setThreadsCount(threadCount)
    layout.initAlgo()
    val _formats: Set<String?> = formats
    val _layout: Layout = layout
    val distanceWriter = if (nsteps > 0) PrintWriter(FileWriter("$output.distances.txt")) else null
    if (nsteps > 0) distanceWriter!!.print("step\tdistance\n")
    val shutdownThread: Thread = object : Thread() {
        override fun run() {
            _layout.endAlgo()
                writeOutput(g, _formats, output)
            distanceWriter?.close()
        }
    }
    Runtime.getRuntime().addShutdownHook(shutdownThread)
    if (nsteps > 0) {
        var lastPercent = 0
            var distance: Double
        for (i in 0 until nsteps) {
            layout.goAlgo()
            val c = view.MainView()
            if (i %10 ==0){
                c.draw(g)
            }
                distance = layout.distance
                distanceWriter!!.print(i)
                distanceWriter.print("\t")
                distanceWriter.print(distance)
                distanceWriter.print("\n")
                distanceWriter.flush()

            val percent = Math.floor(100 * (i + 1.0) / nsteps).toInt()
            if (percent != lastPercent) {
                print("*")
                lastPercent = percent
                if (percent % 25 == 0) {
                    println("$percent%")
                }
            }
        }
    } else {
        nsteps = 0
            var changePerNode: Double
            do {
                ++nsteps
                layout.goAlgo()
                changePerNode = layout.getDistance() / num_nodes
                if (nsteps % 100 == 0) println("$nsteps iterations, change_per_node = $changePerNode")
            } while (nsteps == 1 || changePerNode > targetChangePerNode && nsteps < targetSteps)
            println("Finished in $nsteps iterations, change_per_node = $changePerNode")
    }
    Runtime.getRuntime().removeShutdownHook(shutdownThread)
    layout.endAlgo()
    writeOutput(g, formats, output)
    distanceWriter?.close()
    val endTime = System.currentTimeMillis()
    println("Time = " + (endTime - startTime) / 1000.0 + "s")
    return g
}

private class Arg(
    var flag: String,
    var description: String,
    var not_boolean: Boolean,
    var defaultValue: String?
) {
    var value: String? = null
    override fun toString(): String {
        return flag
    }

    init {
        if (defaultValue != null) {
            value = defaultValue
        }
    }

*/