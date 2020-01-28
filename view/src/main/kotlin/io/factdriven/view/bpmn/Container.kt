package io.factdriven.view.bpmn

import io.factdriven.def.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(definition: Definition): Container {

    fun translate(node: Node, parent: Container): Element {
        return when(node) {
            is Catching -> {
                if (node.isFirstChild) {
                    BpmnEventSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnEventType.message,
                        BpmnEventCharacteristic.catching
                    )
                } else {
                    BpmnTaskSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnTaskType.receive
                    )
                }
            }
            is Throwing -> {
                if (node.isLastChild) {
                    BpmnEventSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnEventType.message,
                        BpmnEventCharacteristic.throwing
                    )
                } else {
                    BpmnTaskSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnTaskType.send
                    )
                }
            }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(definition.id, definition.typeName)
    definition.children.forEach { translate(it, sequence) }
    return sequence

}


data class Position (val x: Int, val y: Int) {

    companion object {

        val zero = Position(0, 0)

    }

}

data class Dimension (val width: Int, val height: Int)

const val margin = 18

interface Element: Named,
    Identified {

    val parent: Container?
    val dimension: Dimension
    val position: Position get() = parent?.position(this) ?: Position.zero
    val center: Position
        get() = Position(
            position.x + (dimension.width / 2),
            position.y + (dimension.height / 2)
        )

}

abstract class Container(override val id: String, override val name: String, override val parent: Container? = null):
    Element {

    val children = mutableListOf<Element>()

    override val dimension: Dimension
        get() = if (children.isEmpty()) Dimension(
            0,
            0
        ) else Dimension(
            children.sumBy { it.dimension.width },
            children.maxBy { it.dimension.height }!!.dimension.height
        )

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

    override fun equals(other: Any?): Boolean {
        return other is Symbol && id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

class Sequence(id: String, name: String, parent: Container? = null): Container(id, name, parent) {

    override fun position(child: Element): Position {
        val predecessors = children.subList(0, children.indexOf(child))
        return Position(
            position.x + predecessors.sumBy { it.dimension.width },
            position.y + (dimension.height - child.dimension.height) / 2
        )
    }

    override fun add(child: Element) {
        // TODO cannot yet deal with sub containers
        val predecessor = if (children.isEmpty()) null else children.last() as Symbol
        predecessor?.connect(child as Symbol)
        super.add(child)
    }

}

interface Identified {

    val id: String

}

interface Named {

    val name: String

}

interface Graphical

abstract class Symbol(override val id: String, override val name: String, override val parent: Container): Graphical,
    Element {

    val outgoing = mutableListOf<Connector>()
    val incoming = mutableListOf<Connector>()

    init {
        parent.add(this)
    }

    override val dimension: Dimension
        get() = Dimension(
            inner.width + 2 * margin,
            inner.height + 2 * margin
        )

    abstract val inner: Dimension
    val topLeft: Position
        get() = Position(
            center.x - (inner.width / 2),
            center.y - (inner.height / 2)
        )
    val topRight: Position
        get() = Position(
            center.x + (inner.width / 2),
            center.y - (inner.height / 2)
        )
    val bottomLeft: Position
        get() = Position(
            center.x - (inner.width / 2),
            center.y + (inner.height / 2)
        )
    val bottomRight: Position
        get() = Position(
            center.x + (inner.width / 2),
            center.y + (inner.height / 2)
        )
    val topCenter: Position
        get() = Position(
            center.x,
            center.y - (inner.height / 2)
        )
    val bottomCenter: Position
        get() = Position(
            center.x,
            center.y + (inner.height / 2)
        )
    val leftCenter: Position
        get() = Position(
            center.x - (inner.width / 2),
            center.y
        )
    val rightCenter: Position
        get() = Position(
            center.x + (inner.width / 2),
            center.y
        )

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

class Connector(val source: Symbol, val target: Symbol): Graphical,
    Identified {

    override val id = source.id + "-" + target.id

    val waypoints: List<Position> get() {
        val from = source.waypoint(this)
        val to = target.waypoint(this)
        // TODO intermediate waypoints
        return listOf(from, to)
    }

    override fun equals(other: Any?): Boolean {
        return other is Symbol && id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

