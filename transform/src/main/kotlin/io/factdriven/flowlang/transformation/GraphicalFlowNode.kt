package io.factdriven.flowlang.transformation

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface GraphicalFlowNode {

    var parent: GraphicalFlowNodeSequence?
    val label: String
    val position: Position
    val dimension: Dimension

}

const val whitespace = 18
data class Position (val x: Int, val y: Int)
data class Dimension (val x: Int, val y: Int)

class GraphicalFlowNodeSequence(label: String = ""): AbstractGraphicalFlowNode(label) {

    val children = mutableListOf<GraphicalFlowNode>()

    fun add (node: GraphicalFlowNode) {
        node.parent = this
        children.add(node)
    }

    override val dimension: Dimension get() {
        return if (children.isEmpty()) Dimension(0,0) else Dimension(children.sumBy { it.dimension.x }, children.maxBy { it.dimension.y }!!.dimension.y)
    }

    fun position(node: AbstractGraphicalFlowNode): Position {
        val graphicalNodesBefore = children.subList(0, children.indexOf(node))
        return Position(position.x + graphicalNodesBefore.sumBy { it.dimension.x }, position.y + (dimension.y - node.dimension.y) / 2)
    }

}

abstract class AbstractGraphicalFlowNode(override var label: String): GraphicalFlowNode {

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
