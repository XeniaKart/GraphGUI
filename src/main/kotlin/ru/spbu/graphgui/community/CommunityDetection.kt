package ru.spbu.graphgui.community

import nl.cwts.networkanalysis.Clustering
import nl.cwts.networkanalysis.IterativeCPMClusteringAlgorithm
import nl.cwts.networkanalysis.LeidenAlgorithm
import nl.cwts.networkanalysis.Network
import nl.cwts.networkanalysis.run.RunNetworkClustering
import java.util.*
import kotlin.collections.ArrayDeque

class CommunityDetection {
    fun detectCommunities(
        sourceVertex: ArrayDeque<String>,
        targetVertex: ArrayDeque<String>,
        edgeWeights: ArrayDeque<Double>
    ): HashMap<String, Int> {
        var nextVertexID = 0
        val int2vertex = hashMapOf<Int, String>()
        val vertex2int = hashMapOf<String, Int>()

        val edges = arrayOfNulls<ArrayList<Int>>(2)
        edges[0] = ArrayList(100)
        edges[1] = ArrayList(100)
        val weights = edgeWeights.toDoubleArray()

        for (i in sourceVertex.indices) {
            if (!vertex2int.contains(sourceVertex[i])) {
                val newVertexID = nextVertexID++
                int2vertex[newVertexID] = sourceVertex[i]
                vertex2int[sourceVertex[i]] = newVertexID
            }

            if (!vertex2int.contains(targetVertex[i])) {
                val newVertexID = nextVertexID++
                int2vertex[newVertexID] = targetVertex[i]
                vertex2int[targetVertex[i]] = newVertexID
            }

            edges[0]!!.add(vertex2int[sourceVertex[i]]!!)
            edges[1]!!.add(vertex2int[targetVertex[i]]!!)
        }

        val edges2 = arrayOfNulls<IntArray>(2)
        edges2[0] = edges[0]!!.toIntArray()
        edges2[1] = edges[1]!!.toIntArray()

        val resolution = RunNetworkClustering.DEFAULT_RESOLUTION
//        val resolution = 0.2
        val iterations = RunNetworkClustering.DEFAULT_N_ITERATIONS
        val randomness = RunNetworkClustering.DEFAULT_RANDOMNESS

        var network = Network(nextVertexID, true, edges2, weights, false, true)
        val initialClustering = Clustering(network.nNodes)
        network = network.createNormalizedNetworkUsingAssociationStrength()

        val resolution2: Double = resolution
        val random = Random()

        val algorithm: IterativeCPMClusteringAlgorithm = LeidenAlgorithm(
            resolution2,
            iterations,
            randomness,
            random
        )
        var finalClustering: Clustering? = null

        var maxQuality = Double.NEGATIVE_INFINITY
        val nRandomStarts = RunNetworkClustering.DEFAULT_N_RANDOM_STARTS

        for (i in 0 until nRandomStarts) {
            val clustering = initialClustering.clone()
            algorithm.improveClustering(network, clustering)
            val quality = algorithm.calcQuality(network, clustering)
            if (nRandomStarts > 1)
                System.err.println("Quality function in random start " + (i + 1) + " equals " + quality + ".")
            if (quality > maxQuality) {
                finalClustering = clustering
                maxQuality = quality
            }
        }
        finalClustering!!.orderClustersByNNodes()
        algorithm.removeSmallClustersBasedOnNNodes(network, finalClustering, 2)

        val vertex2cluster = hashMapOf<String, Int>()
        for (i in 0 until finalClustering.nNodes) {
            val cluster = finalClustering.getCluster(i)
            println("$i -> $cluster")
            vertex2cluster[int2vertex[i]!!] = cluster
        }

        return vertex2cluster
    }
}