package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface GraphicalFlowNode {

    var parent: GraphicalFlowNodeSequence?
    var label: String?

    val position: Position
    val dimension: Dimension

}

data class Position (val x: Int, val y: Int)

const val whitespace = 18

data class Dimension (val x: Int, val y: Int) {

    constructor(netDimension: Dimension): this(netDimension.x + (whitespace * 2), netDimension.y + (whitespace * 2))

}

class GraphicalFlowNodeSequence(label: String? = null): AbstractGraphicalFlowNode(label) {

    private val graphicalNodes = mutableListOf<GraphicalFlowNode>()

    fun add(node: FlowNode) {
        val graphicalNode = when(node){
            is FlowReaction<*, *> -> GraphicalNoneStartEvent(node.label)
            is FlowExecution<*> -> {
                when (node.type) {
                    FlowDefinitionType.execution -> GraphicalFlowNodeSequence(node.label)
                    else -> GraphicalServiceTask(node.label)
                }
            }
            is FlowAction<*, *> -> GraphicalNoneEndEvent(node.label)
            else -> throw IllegalArgumentException()
        }
        add (graphicalNode)
    }

    fun add (node: GraphicalFlowNode) {
        node.parent = this
        graphicalNodes.add(node)
    }

    override val dimension: Dimension get() {
        return Dimension(graphicalNodes.sumBy { it.dimension.x }, graphicalNodes.maxBy { it.dimension.y }!!.dimension.y)
    }

    fun position(node: AbstractGraphicalFlowNode): Position {
        val graphicalNodesBefore = graphicalNodes.subList(0, graphicalNodes.indexOf(node))
        return Position(position.x + graphicalNodesBefore.sumBy { it.dimension.x }, position.y + (dimension.y - node.dimension.y) / 2)
    }

}

abstract class AbstractGraphicalFlowNode(override var label: String? = null): GraphicalFlowNode {

    override var parent: GraphicalFlowNodeSequence? = null

    override val position: Position get() {
        return parent?.position(this) ?: Position(0, 0)
    }

}

abstract class GraphicalTask(label: String): AbstractGraphicalFlowNode(label) {
    override val dimension = Dimension(100 + 2 * whitespace, 80 + 2 * whitespace)
}
class GraphicalServiceTask(label: String): GraphicalTask(label)

abstract class GraphicalEvent(label: String): AbstractGraphicalFlowNode(label) {
    override val dimension = Dimension(36 + 2 * whitespace, 36 + 2 * whitespace)
}
class GraphicalNoneStartEvent(label: String): GraphicalEvent(label)
class GraphicalNoneEndEvent(label: String): GraphicalEvent(label)
