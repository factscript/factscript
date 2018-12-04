package io.factdriven.flowlang.transformation

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface GraphicalElement {

    var parent: GraphicalElementSequence?
    val label: String
    val position: Position
    val dimension: Dimension

    val leftConnector: Position
    val rightConnector: Position

    fun rendered(): List<Rendered>

}

const val whitespace = 18
data class Position (val x: Int, val y: Int)
data class Dimension (val x: Int, val y: Int)

class GraphicalElementSequence(label: String = ""): AbstractGraphicalElement(label) {

    val children = mutableListOf<GraphicalElement>()

    fun add (node: GraphicalElement) {
        node.parent = this
        children.add(node)
    }

    override val dimension: Dimension get() {
        return if (children.isEmpty()) Dimension(0,0) else Dimension(children.sumBy { it.dimension.x }, children.maxBy { it.dimension.y }!!.dimension.y)
    }

    override fun rendered(): List<Rendered> {
        val symbols = children.map { it.rendered() }.flatten()
        var previous = children[0]
        val sequenceFlows = children.subList(1, children.size).map {
            val sequenceFlow = RenderedSequenceFlow("", previous.rightConnector, it.leftConnector, previous as Rendered, it as Rendered)
            previous = it
            sequenceFlow
        }
        return symbols + sequenceFlows
    }

    fun position(node: AbstractGraphicalElement): Position {
        val graphicalNodesBefore = children.subList(0, children.indexOf(node))
        return Position(position.x + graphicalNodesBefore.sumBy { it.dimension.x }, position.y + (dimension.y - node.dimension.y) / 2)
    }

    override val leftConnector get() = children.first().leftConnector
    override val rightConnector get() = children.last().rightConnector

}

abstract class AbstractGraphicalElement(override var label: String): GraphicalElement {

    override var parent: GraphicalElementSequence? = null

    override val position: Position get() {
        return parent?.position(this) ?: Position(0, 0)
    }

}

interface Rendered

interface RenderedSymbol: Rendered {

    val renderingPosition: Position
    val renderingDimension: Dimension

}

data class RenderedSequenceFlow(val label: String, val from: Position, val to: Position, val source: Rendered, val target: Rendered): Rendered

abstract class RenderedTask(label: String): AbstractGraphicalElement(label), RenderedSymbol {

    override val renderingDimension get() = Dimension(100,80)
    override val renderingPosition get() = Position(position.x + whitespace, position.y + whitespace)
    override val dimension get() = Dimension(renderingDimension.x + 2 * whitespace, renderingDimension.y + 2 * whitespace)
    override fun rendered() = listOf(this)

    override val leftConnector get() = Position(position.x + whitespace, position.y + dimension.y / 2)
    override val rightConnector get() = Position(position.x + dimension.x - whitespace, position.y + dimension.y / 2)

}

class RenderedServiceTask(label: String): RenderedTask(label)

abstract class RenderedEvent(label: String): AbstractGraphicalElement(label), RenderedSymbol {

    override val renderingDimension get() = Dimension(36,36)
    override val renderingPosition get() = Position(position.x + whitespace, position.y + whitespace)
    override val dimension get() = Dimension(renderingDimension.x + 2 * whitespace, renderingDimension.y + 2 * whitespace)
    override fun rendered() = listOf(this)

    override val leftConnector get() = Position(position.x + whitespace, position.y + dimension.y / 2)
    override val rightConnector get() = Position(position.x + dimension.x - whitespace, position.y + dimension.y / 2)

}

class RenderedStartEvent(label: String): RenderedEvent(label)
class RenderedEndEvent(label: String): RenderedEvent(label)
