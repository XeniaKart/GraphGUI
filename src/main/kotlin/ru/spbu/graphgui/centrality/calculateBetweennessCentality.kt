package ru.spbu.graphgui.centrality

import javafx.scene.paint.Color
import ru.spbu.graphgui.view.GraphView

fun calculateBetweennessCentality(graph: GraphView<String, Double>?, boolDirect: Boolean) {
    graph?.let {
        val graphBeetwCent1: BetweennessCenralityWeightedDirected
        val graphBeetwCent2: BetweennessCenralityWeightedUnidirected
        val valueCentralities: HashMap<String, Double>
        val distinctVertex = ArrayDeque<String>()
        val sourceVertex = ArrayDeque<String>()
        val targetVertex = ArrayDeque<String>()
        val edgeWeight = ArrayDeque<Double>()
        for (i in graph.verticesKeys()) {
            distinctVertex.addLast(i)
        }
        for (i in graph.edgesVertex()) {
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
                i.value >= minimum!! && i.value < minimum + intervals -> setColor(i, Color.RED, graph)
                i.value >= minimum + intervals && i.value < minimum + 2 * intervals -> setColor(i, Color.ORANGE, graph)
                i.value >= minimum + 2 * intervals && i.value < minimum + 3 * intervals -> setColor(
                    i,
                    Color.YELLOW,
                    graph
                )
                i.value >= minimum + 3 * intervals && i.value < minimum + 4 * intervals -> setColor(
                    i,
                    Color.GREEN,
                    graph
                )
                i.value >= minimum + 4 * intervals && i.value < minimum + 5 * intervals -> setColor(
                    i,
                    Color.AQUA,
                    graph
                )
                i.value >= minimum + 5 * intervals && i.value < minimum + 6 * intervals -> setColor(
                    i,
                    Color.BLUE,
                    graph
                )
                else -> setColor(i, Color.PURPLE, graph)
            }
        }
    }
}

private fun setColor(i: MutableMap.MutableEntry<String, Double>, color: Color, graph: GraphView<String, Double>) {
    for (j in graph.vertices()) {
        if (i.key == j.vertex) {
            j.color = color
            break
        }
    }
}
