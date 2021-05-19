package ru.spbu.graphgui.centrality

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.graph.UndirectedSparseGraph
import edu.uci.ics.jung.graph.util.EdgeType
import org.apache.commons.collections15.Transformer
import java.util.*
import kotlin.collections.ArrayDeque

class BetweennessCenralityWeightedUnidirected {

    var edgeCountDirected = 0


    inner class MyNode(var id: String) {
        override fun toString(): String {
            return "V$id"
        }
    }

    inner class MyLink(var weight: Double) {
        var id: Int = edgeCountDirected++
        override fun toString(): String {
            return "E$id"
        }

    }

    fun betweennessCentralityScoreUndirected(
        distinctNodes: ArrayDeque<String>,
        sourceVertex: ArrayDeque<String>,
        targetVertex: ArrayDeque<String>,
        edgeWeight: ArrayDeque<Double>
    ): HashMap<String, Double> {

        val valueCentralities = hashMapOf<String, Double>()
        val g: Graph<MyNode?, MyLink> = UndirectedSparseGraph()

        val graphNodes = Hashtable<String, MyNode>()
        val sourceNode = ArrayDeque<MyNode?>()
        val targetNode = ArrayDeque<MyNode?>()
        val graphNodesOnly = ArrayDeque<MyNode>()

        for (nodeName in distinctNodes) {
            val data = MyNode(nodeName)
            graphNodes[nodeName] = data
            graphNodesOnly.addLast(data)
        }

        for (t in sourceVertex) {
            sourceNode.add(graphNodes[t])
        }

        for (t in targetVertex) {
            targetNode.add(graphNodes[t])
        }

        for (i in edgeWeight.indices) {
            g.addEdge(MyLink(edgeWeight[i]), sourceNode[i], targetNode[i], EdgeType.UNDIRECTED)
        }
        val wtTransformer: Transformer<MyLink, Double> = Transformer { it.weight }
        val bc1 = BetweennessCentrality(g, wtTransformer)

        for (node in graphNodesOnly) {
            valueCentralities[node.id] = bc1.getVertexScore(node) ?: 0.0
        }
        return valueCentralities
    }

}