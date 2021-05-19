package ru.spbu.graphgui.community

import javafx.scene.paint.Color
import ru.spbu.graphgui.view.GraphView
import kotlin.random.Random

fun detectCommunities(graph: GraphView<String, Double>?) {
    val sourceVertex = ArrayDeque<String>()
    val targetVertex = ArrayDeque<String>()
    val edgeWeights = ArrayDeque<Double>()

    graph?.let {
        for (i in graph.edgesVertex()) {
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

            for (j in graph.vertices()) {
                if (i.key == j.vertex) {
                    j.color = communitiesColors[i.value]!!
                }
            }
        }
    }
}
