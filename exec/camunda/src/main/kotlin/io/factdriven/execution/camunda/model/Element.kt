package io.factdriven.execution.camunda.model

import io.factdriven.definition.Node
import io.factdriven.execution.camunda.diagram.Dimension
import io.factdriven.execution.camunda.diagram.Direction
import io.factdriven.execution.camunda.diagram.Position

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class Element<IN: Node, OUT: Any>(val node: IN, val parent: Element<*,*>? = null) {

    internal abstract val children: List<Element<*,*>>

    internal open val paths: List<Path> = emptyList()

    internal abstract val model: OUT

    internal val process: BpmnModel get() = parent?.process ?: this as BpmnModel

    internal open val position: Position get() = parent!!.position(this)
    internal abstract val dimension: Dimension

    internal abstract fun position(child: Element<*,*>): Position

    internal abstract fun entry(from: Direction = Direction.West): Position

    internal open fun wayPoints(path: Path): List<Position> {
        return listOf(
            if(path.from == this) position + entry(Direction.East)
            else position + entry()
        )
    }

    open fun toExecutable(): OUT {
        init()
        children.forEach { it.toExecutable() }
        paths.forEach { it.toExecutable() }
        return model
    }

    internal abstract fun init()

}

val List<Element<*,*>>.dimensions: List<Dimension> get() = map { it.dimension }