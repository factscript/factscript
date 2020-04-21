package io.factdriven.execution.camunda.model

import io.factdriven.definition.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Sequence(node: Flow, parent: Element<*,*>): Group<Flow>(node,parent) {

    override val dimension: Dimension get() = Dimension(children.sumBy { it.dimension.width }, children.maxBy { it.dimension.height }?.dimension?.height ?: BpmnModel.margin.height)

    override val children: List<Element<*,*>> = node.children.mapNotNull {
        when (it) {
            is Calling -> ServiceTaskSymbol(it, this)
            is Promising -> CatchingEventSymbol(it, this)
            is Awaiting -> ReceiveTaskSymbol(it, this)
            is Throwing -> if (it.isFinish()) ThrowingEventSymbol(it, this) else SendTaskSymbol(it, this)
            is Branching -> Branch(it, this)
            is Looping -> Loop(it, this)
            is Flow -> Sequence(it, this)
            is Conditional -> null
            else -> throw IllegalStateException()
        }
    }

    override val paths: List<Path> =
        if (children.size > 1) children.subList(1, children.size).map {
            val previous = children.get(children.indexOf(it) - 1)
            Path(previous, it, this, if (previous is Loop) previous.node.children.last() as Conditional else null)
        } else emptyList()
    
    override fun entry(from: Direction): Position {
        return when(from) {
            Direction.North -> Position(dimension.width / 2, 0)
            Direction.East -> Position(dimension.width, children.maxBy { it.entry(Direction.East).y }?.entry(Direction.East)?.y ?: dimension.height / 2)
            Direction.South -> Position(dimension.width / 2, dimension.height)
            Direction.West -> Position(0, children.maxBy { it.entry(Direction.West).y }?.entry(Direction.West)?.y ?: dimension.height / 2)
        }
    }

    override fun position(child: Element<*,*>): Position {
        return position + entry(Direction.West) - child.entry(Direction.West) +
            Position(children.subList(0, children.indexOf(child)).sumBy { it.dimension.width }, 0)
    }

}