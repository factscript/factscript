package io.factdriven.visualization.bpmn

import io.factdriven.definition.*
import io.factdriven.impl.execution.type

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(flow: Flow): Container {

    fun translate(node: Node, parent: Container) {
        when(node) {
            is Executing -> {
                BpmnTaskSymbol(
                    node.id,
                    node.throwing.type.context,
                    node.throwing.type.local,
                    parent,
                    BpmnTaskType.service
                )
            }
            is Promising -> {
                BpmnEventSymbol(
                    node.id,
                    node.catching.type.context,
                    node.catching.type.local,
                    parent,
                    BpmnEventType.message,
                    BpmnEventCharacteristic.catching
                )
            }
            is Consuming -> {
                BpmnTaskSymbol(
                    node.id,
                    node.catching.type.context,
                    node.catching.type.local,
                    parent,
                    BpmnTaskType.receive
                )
            }
            is Throwing -> {
                if (node.isLast()) {
                    BpmnEventSymbol(
                        node.id,
                        node.throwing.type.context,
                        node.throwing.type.local,
                        parent,
                        BpmnEventType.message,
                        BpmnEventCharacteristic.throwing
                    )
                } else {
                    BpmnTaskSymbol(
                        node.id,
                        node.throwing.type.context,
                        node.throwing.type.local,
                        parent,
                        BpmnTaskType.send
                    )
                }
            }
            is Branching -> {
                val branch = Branch(node.id, "", "", parent)
                BpmnGatewaySymbol(
                    "${node.id}-split",
                    "",
                    node.label ?: "",
                    branch,
                    BpmnGatewayType.exclusive
                )
                node.children.forEach { translate(it, branch) }
                BpmnGatewaySymbol(
                    "${node.id}-join",
                    "",
                    "",
                    branch,
                    BpmnGatewayType.exclusive
                )
            }
            is Flow -> {
                val sequence = Sequence(node.id, node.entity.type.context, node.entity.type.local, parent)
                node.children.forEach { translate(it, sequence) }
            }
            is Checking -> { /* do nothing */ }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(flow.id, flow.entity.type.context, flow.entity.type.local)
    flow.children.forEach { translate(it, sequence) }
    return sequence

}


data class Position (val x: Int, val y: Int) {

    companion object {

        val zero = Position(0, 0)

    }

}

data class Dimension (val width: Int, val height: Int)

val margin = Dimension(18, 18)

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

abstract class Container(override val id: String, override val context: String, override val name: String, override val parent: Container? = null):
    Element {

    val children = mutableListOf<Element>()

    val symbols: Set<Symbol>
        get() = children.map {
            when (it) {
                is Symbol -> listOf(it)
                is Container -> it.symbols
                else -> throw IllegalStateException()
            }
        }.flatten().toSet()

    val connectors: Set<Connector> get() = symbols.map { it.outgoing }.flatten().toSet()

    val containers: Set<Container> get() = children.map {
        when (it) {
            is Symbol -> emptySet()
            is Container -> it.containers
            else -> throw IllegalStateException()
        }
        }.flatten().toSet() + this

    abstract fun position(child: Element): Position

    open fun add (child: Element) {
        children.add(child)
    }

}

class Sequence(id: String, context: String, name: String, parent: Container? = null): Container(id, context, name, parent) {

    init {
        parent?.add(this)
    }

    override val dimension: Dimension
        get() = if (children.isEmpty()) Dimension(
            0,
            margin.height
        ) else Dimension(
            children.sumBy { it.dimension.width },
            children.maxBy { it.dimension.height }!!.dimension.height
        )

    override fun position(child: Element): Position {
        val predecessors = children.subList(0, children.indexOf(child))
        if (child is Branch) {
            return Position(
                position.x + predecessors.sumBy { it.dimension.width },
                center.y - maxOf(child.fork.dimension.height, child.sequences[0].dimension.height) / 2
            )
        } else {
            return Position(
                position.x + predecessors.sumBy { it.dimension.width },
                position.y + (dimension.height - child.dimension.height) / 2
            )
        }
    }

    override fun add(child: Element) {
        if (child is Symbol) {
            val element = if (children.isEmpty()) null else children.last()
            val source = (if (element is Container) element.children.last() else element) as Symbol?
            source?.connect(child)
        }
        children.add(child)
    }

}

class Branch(id: String, context: String, name: String, override val parent: Container): Container(id, context, name, parent) {

    init {
        parent.add(this)
    }

    override val position: Position get() {
        return parent.position(this)
    }

    override val dimension: Dimension
        get() = if (children.isEmpty()) Dimension(
            0,
            0
        ) else Dimension(
            fork.dimension.width + sequences.maxBy { it.dimension.width }!!.dimension.width + join.dimension.width,
            sequences.sumBy { it.dimension.height }
        )

    val fork: Element
        get() = children.first()

    val join: Element
        get() = children.last()

    val sequences: List<Sequence>
        get() = children.subList(1, children.size - 1) as List<Sequence>

    override fun position(child: Element): Position {
        return if (child == fork) {
            Position(position.x, position.y + (children.maxBy { it.dimension.height }!!.dimension.height - fork.dimension.height) / 2 )
        } else if (child == join) {
            Position(position.x + fork.dimension.width + sequences.maxBy { it.dimension.width }!!.dimension.width, position.y + (children.maxBy { it.dimension.height }!!.dimension.height - fork.dimension.height) / 2)
        } else {
            val predecessors = sequences.subList(0, sequences.indexOf(child))
            Position(position.x + fork.dimension.width, position.y + predecessors.sumBy { it.dimension.height } )
        }
    }

    override fun add(child: Element) {
        if (child is Symbol) {
            if (children.size == 0) {
                val last = parent.children.elementAtOrNull(parent.children.size - 2) as Symbol?
                last?.connect(child)
            } else {
                val split = children.first() as Symbol
                val sequences =
                    if (children.size > 1) children.subList(1, children.size) as List<Sequence> else emptyList()
                sequences.forEach {
                    val first = (it.children.firstOrNull() ?: child) as Symbol
                    split.connect(first)
                    val last = it.children.lastOrNull() as Symbol?
                    last?.connect(child)
                }
            }
        }
        children.add(child)
    }

}

interface Identified {

    val id: String

}

interface Named {

    val context: String
    val name: String

}

interface Graphical

abstract class Symbol(override val id: String, override val context: String, override val name: String, override val parent: Container): Graphical,
    Element {

    val outgoing = mutableListOf<Connector>()
    val incoming = mutableListOf<Connector>()

    init {
        parent.add(this)
    }

    override val dimension: Dimension
        get() = Dimension(
            inner.width + 2 * margin.width,
            inner.height + 2 * margin.height
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

    open fun waypoint(connector: Connector): Position {
        // TODO select based on relative position
        return if (connector.source.equals(this)) rightCenter else leftCenter
    }

}

class Connector(val source: Symbol, val target: Symbol): Graphical,
    Identified {

    override val id = source.id + "-" + target.id

    val waypoints: List<Position> get() {
        val from = source.waypoint(this)
        val to = target.waypoint(this)
        return if (source is BpmnGatewaySymbol) {
            if (target is BpmnGatewaySymbol) {
                val sequence = source.parent.children.find { it is Sequence && it.children.isEmpty() }!!
                listOf(from, Position(from.x, sequence.center.y), Position(to.x, sequence.center.y), to)
            } else {
                listOf(from, Position(from.x, to.y), to)
            }
        } else if (target is BpmnGatewaySymbol) {
            listOf(from, Position(to.x, from.y), to)
        } else {
            listOf(from, to)
        }
    }

}

