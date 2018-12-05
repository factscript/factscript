package io.factdriven.flowlang.transformation

import java.lang.IllegalStateException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

data class Position (val x: Int, val y: Int) {
    
    companion object {
        
        val zero = Position(0,0)
        
    }
    
}

data class Dimension (val width: Int, val height: Int)

const val margin = 18

interface Element: Identified {

    val parent: Container?
    val dimension: Dimension
    val position: Position get() = parent?.position(this) ?: Position.zero
    val center: Position get() = Position(position.x + (dimension.width / 2), position.y + (dimension.height / 2))

}

abstract class Container(override val id: Id, override val parent: Container? = null): Element {
     
    val children = mutableListOf<Element>()

    override val dimension: Dimension get() = if (children.isEmpty()) Dimension(0,0) else Dimension(children.sumBy { it.dimension.width }, children.maxBy { it.dimension.height }!!.dimension.height)

    val symbols: Set<Symbol> get() = children.map { when (it) {
        is Symbol -> listOf(it)
        is Container -> it.symbols
        else -> throw IllegalStateException()
    }}.flatten().toSet()

    val connectors: Set<Connector> get() = symbols.map { it.outgoing }.flatten().toSet()
    
    abstract fun position(child: Element): Position

    open fun add (child: Element) {
        children.add(child)
    }

}

class Sequence(id: Id, parent: Container? = null): Container(id, parent) {

    override fun position(child: Element): Position {
        val predecessors = children.subList(0, children.indexOf(child))
        return Position(position.x + predecessors.sumBy { it.dimension.width }, position.y + (dimension.height - child.dimension.height) / 2)
    }

    override fun add(child: Element) {
        // TODO cannot yet deal with sub containers
        val predecessor = if (children.isEmpty()) null else children.last() as Symbol
        predecessor?.connect(child as Symbol)
        super.add(child)
    }
    
}

interface Identified {

    val id: Id

}

interface Graphical

abstract class Symbol(override val id: Id, override val parent: Container): Graphical, Element {

    val outgoing = mutableListOf<Connector>()
    val incoming = mutableListOf<Connector>()

    init {
        parent.add(this)
    }

    override val dimension: Dimension get() = Dimension(inner.width + 2 * margin, inner.height + 2 * margin)

    abstract val inner: Dimension
    val topLeft: Position get() = Position(center.x - (inner.width / 2), center.y - (inner.height / 2))
    val topRight: Position get() = Position(center.x + (inner.width / 2), center.y - (inner.height / 2))
    val bottomLeft: Position get() = Position(center.x - (inner.width / 2), center.y + (inner.height / 2))
    val bottomRight: Position get() = Position(center.x + (inner.width / 2), center.y + (inner.height / 2))
    val topCenter: Position get() = Position(center.x, center.y - (inner.height / 2))
    val bottomCenter: Position get() = Position(center.x, center.y + (inner.height / 2))
    val leftCenter: Position get() = Position(center.x - (inner.width / 2), center.y)
    val rightCenter: Position get() = Position(center.x + (inner.width / 2), center.y)

    fun connect(target: Symbol) {
        val connector = Connector(this, target)
        outgoing.add(connector)
        target.incoming.add(connector)
    }
    
    fun waypoint(connector: Connector): Position {
        // TODO select based on relative position
        return if (connector.source.equals(this)) rightCenter else leftCenter
    }

    override fun equals(other: Any?): Boolean {
        return other is Symbol && id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

data class Connector(val source: Symbol, val target: Symbol): Graphical, Identified {

    override val id: Id = Id(source.id.key + "-" + target.id.key)
    
    val waypoints: List<Position> get() {
        val from = source.waypoint(this)
        val to = target.waypoint(this)
        // TODO intermediate waypoints
        return listOf(from, to)
    }

}

data class Id (val key: String) {

    val label: String = {

        val regex = String.format("%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ).toRegex()

        val split = key.split(regex)

        (split[0] + if (split.size > 1) split.subList(1, split.size).joinToString(separator = "") { " " + it.substring(0, 1).toLowerCase() + it.substring(1) } else "").trim()

    }.invoke()

}

interface GraphicalElement {

    var parent: GraphicalElementSequence?
    val id: String
    val position: Position
    val dimension: Dimension

    val leftConnector: Position
    val rightConnector: Position

    fun rendered(): List<Rendered>

}

class GraphicalElementSequence(label: String = ""): AbstractGraphicalElement(label) {

    val children = mutableListOf<GraphicalElement>()

    fun add (node: GraphicalElement) {
        node.parent = this
        children.add(node)
    }

    override val dimension: Dimension get() {
        return if (children.isEmpty()) Dimension(0,0) else Dimension(children.sumBy { it.dimension.width }, children.maxBy { it.dimension.height }!!.dimension.height)
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
        return Position(position.x + graphicalNodesBefore.sumBy { it.dimension.width }, position.y + (dimension.height - node.dimension.height) / 2)
    }

    override val leftConnector get() = children.first().leftConnector
    override val rightConnector get() = children.last().rightConnector

}

abstract class AbstractGraphicalElement(override var id: String): GraphicalElement {

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
    override val renderingPosition get() = Position(position.x + margin, position.y + margin)
    override val dimension get() = Dimension(renderingDimension.width + 2 * margin, renderingDimension.height + 2 * margin)
    override fun rendered() = listOf(this)

    override val leftConnector get() = Position(position.x + margin, position.y + dimension.height / 2)
    override val rightConnector get() = Position(position.x + dimension.width - margin, position.y + dimension.height / 2)

}

class RenderedServiceTask(label: String): RenderedTask(label)

abstract class RenderedEvent(label: String): AbstractGraphicalElement(label), RenderedSymbol {

    override val renderingDimension get() = Dimension(36,36)
    override val renderingPosition get() = Position(position.x + margin, position.y + margin)
    override val dimension get() = Dimension(renderingDimension.width + 2 * margin, renderingDimension.height + 2 * margin)
    override fun rendered() = listOf(this)

    override val leftConnector get() = Position(position.x + margin, position.y + dimension.height / 2)
    override val rightConnector get() = Position(position.x + dimension.width - margin, position.y + dimension.height / 2)

}

class RenderedStartEvent(label: String): RenderedEvent(label)
class RenderedEndEvent(label: String): RenderedEvent(label)
