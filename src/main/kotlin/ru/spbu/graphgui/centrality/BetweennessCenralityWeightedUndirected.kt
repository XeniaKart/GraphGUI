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

        for (i in distinctNodes.indices) {
            val nodeName = distinctNodes[i]
            val data = MyNode(nodeName)
            graphNodes[nodeName] = data
            graphNodesOnly.addLast(data)
        }

        for (t in sourceVertex.indices) {
            sourceNode.add(graphNodes[sourceVertex[t]])
        }

        for (t in targetVertex.indices) {
            targetNode.add(graphNodes[targetVertex[t]])
        }

        for (i in edgeWeight.indices) {
            g.addEdge(MyLink(edgeWeight[i]), sourceNode[i], targetNode[i], EdgeType.UNDIRECTED)
        }
        val wtTransformer: Transformer<MyLink, Double> =
            Transformer { link -> link.weight }
        val bc1 = BetweennessCentrality(g, wtTransformer)

        for (i in graphNodesOnly.indices) {
            valueCentralities[graphNodesOnly[i].id] = bc1.getVertexScore(graphNodesOnly[i]) ?: 0.0
//            println(
//                "Model.Graph.Model.Graph Node " + graphNodesOnly[i] + " Betweenness Centrality " + valueCentralities[graphNodesOnly[i].id]
//            )
        }
        return valueCentralities
    }

}