//package ru.spbu.graphgui.view
//
//import org.gephi.graph.api.Graph
//import org.gephi.graph.api.GraphController
//import org.gephi.graph.api.Node
//import org.gephi.io.exporter.api.ExportController
//import org.gephi.io.importer.api.EdgeDirectionDefault
//import org.gephi.io.importer.api.ImportController
//import org.gephi.io.processor.plugin.DefaultProcessor
//import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
//import org.gephi.layout.spi.Layout
//import org.gephi.project.api.ProjectController
//import org.openide.util.Lookup
//
//import javafx.concurrent.Task
//import javafx.scene.layout.Pane
//import java.io.*
//import kotlin.system.exitProcess
//
//class ProgressBarView : Task<Void?>() {
//    var countIterations = 10000
//        set(value) {
//            field = value
//        }
//    var countNodes = 30
//        set(value) {
//            field = value
//        }
//    var barnesHutTheta = 1.2
//        set(value) {
//            field = value
//        }
//    var jitterTolerance = 1.0
//        set(value) {
//            field = value
//        }
//    var linLogMode = false
//        set(value) {
//            field = value
//        }
//    var scalingRatio = 2.0
//        set(value) {
//            field = value
//        }
//    var gravity = 1.0
//        set(value) {
//            field = value
//        }
//    var strongGravityMode = false
//        set(value) {
//            field = value
//        }
//    var outboundAttractionDistribution = false
//        set(value) {
//            field = value
//        }
//    var sourcePath = "empty"
//        set(value) {
//            field = value
//        }
//
//    public override fun call(): Void? {
//        val graphForceAtlas2 = makeLayout2()
//        return null
//    }
//
//    val MAX_WORK = 100
//
//    //private val argsMap: MutableMap<String, Arg> = LinkedHashMap()
//    private fun writeOutput(g: Graph, formats: Set<String?>, output: String?) {
//        try {
//            // ExporterCSV, ExporterDL, ExporterGDF, ExporterGEXF, ExporterGML, ExporterGraphML, ExporterPajek, ExporterVNA, PDFExporter, PNGExporter, SVGExporter
//            val ec = Lookup.getDefault().lookup(ExportController::class.java)
//            for (format in formats) {
//                if (format == "txt") {
//                    val pw = PrintWriter(
//                        FileWriter(
//                            output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"
//                        )
//                    )
//                    pw.print(
//                        """
//                            id	x	y
//
//                            """.trimIndent()
//                    )
//                    for (n in g.nodes) {
//                        pw.print(n.id)
//                        pw.print("\t")
//                        pw.print(n.x())
//                        pw.print("\t")
//                        pw.print(n.y())
//                        pw.print("\n")
//                    }
//                    pw.close()
//                } else {
//                    ec.exportFile(
//                        File(output + if (output!!.toLowerCase().endsWith(".$format")) "" else ".$format"),
//                        ec.getExporter(format)
//                    )
//                }
//            }
//        } catch (x: IOException) {
//            x.printStackTrace()
//            exitProcess(1)
//        }
//    }
//
////private fun addArg(flag: String, description: String, not_boolean: Boolean, defaultValue: Any) {
////    argsMap["--" + flag.toLowerCase()] =
////        Arg(flag, description, not_boolean, "" + defaultValue)
////}
////
////private fun addArg(flag: String, description: String, not_boolean: Boolean) {
////    argsMap["--" + flag.toLowerCase()] =
////        Arg(flag, description, not_boolean, null)
////}
////
////private fun getArg(flag: String): String? {
////    val a = argsMap["--" + flag.toLowerCase()]
////    return a?.value
////}
//
//    fun makeLayout2(): Graph {
//        val startTime = System.currentTimeMillis()
//        var i = 0
//        var nsteps = countIterations
//        val targetChangePerNode = 0.0
//        var targetSteps = 0
//        val seed: Long? = null
//        val threadCount = Runtime.getRuntime().availableProcessors()
//        val formats: MutableSet<String?> = HashSet()
//        val coordsFile: File? = null
////    var sourcePath: String
////    if (randomGraph){
////        sourcePath = "D:\\2сем\\javafxtornadofx2\\randomGraph.csv"
////    }else{
////        sourcePath = path
////    }
//        val file = File(sourcePath)
//        if (!file.exists()) {
//            System.err.println("$file not found.")
//            exitProcess(1)
//        }
//        val output = "myGraph"
////        nsteps = 10000
//        if (nsteps == 0 && targetChangePerNode == 0.0) {
//            System.err.println("Either --nsteps or --targetChangePerNode must be set!")
//            exitProcess(1)
//        }
//        if (nsteps > 0 && targetChangePerNode > 0.0) {
//            System.err.println("--nsteps and --targetChangePerNode are mutually exclusive!")
//            exitProcess(1)
//        }
//
//        formats.add("gexf")
//        formats.add("csv")
////    if (getArg("coords") != null) {
////        coordsFile = File(getArg("coords"))
////        if (!coordsFile.exists()) {
////            System.err.println("$coordsFile not found.")
////            System.exit(1)
////        }
////    }
////    TODO()
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
//        val g: Graph = if (/*!getArg("directed").equals("true", ignoreCase = */true) {
//            container.loader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED)
//            graphModel.undirectedGraph
//        } else {
//            container.loader.setEdgeDefault(EdgeDirectionDefault.DIRECTED)
//            graphModel.directedGraph
//        }
//        importController.process(container, DefaultProcessor(), workspace)
//        val layout = ForceAtlas2(null)
//        layout.setGraphModel(graphModel)
//        val random = if (seed != null) java.util.Random(seed) else java.util.Random()
//        for ((numNodes, node) in g.nodes.withIndex()) {
//            node.setX(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
//            node.setY(((0.01 + random.nextDouble()) * 1000).toFloat() - 500)
//        }
//        if (coordsFile != null) {
//            val idToNode: MutableMap<Any, Node> = java.util.HashMap()
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
//        layout.barnesHutTheta = barnesHutTheta
//        layout.jitterTolerance = jitterTolerance
//        layout.isLinLogMode = linLogMode
//        layout.scalingRatio = scalingRatio
//        layout.isStrongGravityMode = strongGravityMode
//        layout.gravity = gravity
//        layout.isOutboundAttractionDistribution = outboundAttractionDistribution
//        layout.threadsCount = threadCount
//        layout.initAlgo()
//        //val _formats: Set<String?> = formats
//        val _layout: Layout = layout
////        val distanceWriter = if (nsteps > 0) PrintWriter(FileWriter("$output.distances.txt")) else null
////        if (nsteps > 0) distanceWriter!!.print("step\tdistance\n")
//        val shutdownThread: Thread = object : Thread() {
//            override fun run() {
//                _layout.endAlgo()
////                writeOutput(g, _formats, output)
////                distanceWriter?.close()
//            }
//        }
//        Runtime.getRuntime().addShutdownHook(shutdownThread)
//        if (nsteps > 0) {
//            var lastPercent = 0
////            var distance: Double
//            thread.isDaemon = true
//            thread.start()
//
////            for (i in 0 until nsteps) {
////                layout.goAlgo()
//////                distance = layout.distance
//////                distanceWriter!!.print(i)
//////                distanceWriter.print("\t")
//////                distanceWriter.print(distance)
//////                distanceWriter.print("\n")
//////                distanceWriter.flush()
////
////                val percent = floor(100 * (i + 1.0) / nsteps).toInt()
////                if (percent != lastPercent) {
////                    print("*")
////                    lastPercent = percent
////                    if (percent % 25 == 0) {
////                        println("$percent%")
////                    }
////                }
//////                if (i % 10 == 0) {
////////                    println ("debug")
//////                    draw(g, a)
//////                }
////            }
//        } else {
//            nsteps = 0
////            var changePerNode: Double
////            do {
////                ++nsteps
////                layout.goAlgo()
////                changePerNode = layout.getDistance() / num_nodes
////                if (nsteps % 100 == 0) println("$nsteps iterations, change_per_node = $changePerNode")
////            } while (nsteps == 1 || changePerNode > targetChangePerNode && nsteps < targetSteps)
////            println("Finished in $nsteps iterations, change_per_node = $changePerNode")
//        }
//        Runtime.getRuntime().removeShutdownHook(shutdownThread)
//        layout.endAlgo()
//        writeOutput(g, formats, output)
////        distanceWriter?.close()
//        val endTime = System.currentTimeMillis()
//        println("Time = " + (endTime - startTime) / 1000.0 + "s")
//        return g
//    }
//}