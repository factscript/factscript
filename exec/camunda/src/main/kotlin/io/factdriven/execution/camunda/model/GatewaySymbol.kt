package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.definition.Node
import io.factdriven.execution.camunda.model.BpmnModel.Companion.margin
import io.factdriven.impl.definition.positionSeparator
import io.factdriven.impl.utils.asLines
import io.factdriven.impl.utils.toLines
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class GatewaySymbol<OUT: Gateway>(node: Node, parent: Element<out Node, *>): Symbol<Node, OUT>(node, parent) {

    override val dimension: Dimension = Dimension(
        width = 50 + margin.width * 2,
        height = 50 + margin.height * 2
    )

    override fun position(child: Element<*,*>): Position {
        return position + if (parent is Branch) Position(0, (node.label.toLines().size - 1) * - 13) else Position(0, dimension.height - margin.height + 6)
    }

    override fun init() {
        super.init()
        if (parent!!.children.indexOf(this) == 0) {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Fork", false)
            if (parent is Branch)
                model.setAttributeValue("name", node.label.asLines(), false)
        } else {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Join", false)
            if (parent is Loop)
                model.setAttributeValue("name", node.label.asLines(), false)
        }
    }

    override fun wayPoints(path: Path): List<Position> {
        if (path.parent!!.position.y + path.parent.entry(Direction.West).y == position.y + entry(Direction.West).y) {
            if (parent is Loop && path.from is GatewaySymbol && path.to is GatewaySymbol) {
                val from = position + entry(Direction.North) + Dimension(0, margin.height)
                if (path.from == this) {
                    return listOf(from, Position(from.x, parent.position.y + margin.height / 2))
                } else {
                    return listOf(Position(from.x, parent.position.y + margin.height / 2), from)
                }
            } else {
                if (path.from == this) {
                    return listOf(position + entry(Direction.East) - Dimension(margin.width, 0))
                } else {
                    return listOf(position + entry(Direction.West) + Dimension(margin.width, 0))
                }
            }
        } else {
            if (path.from == this) {
                val from = position + entry(Direction.South) - Dimension(0, margin.height)
                val to = path.parent.position + path.parent.entry(Direction.West) + Dimension(margin.width, 0)
                return listOf(from, Position(from.x, to.y))
            } else {
                val from = path.parent.position + path.parent.entry(Direction.East) - Dimension(margin.width, 0)
                val to = position + entry(Direction.South) - Dimension(0, margin.height)
                return listOf(Position(to.x, from.y), to)
            }
        }
    }

}

class ExclusiveGatewaySymbol(node: Node, parent: Element<*,*>): GatewaySymbol<ExclusiveGateway>(node, parent) {

    override val model = process.model.newInstance(ExclusiveGateway::class.java)

    override fun init() {
        super.init()
        model.diagramElement.isMarkerVisible = true
    }

}

class InclusiveGatewaySymbol(node: Branching, parent: Element<out Branching,*>): GatewaySymbol<InclusiveGateway>(node, parent) {

    override val model = process.model.newInstance(InclusiveGateway::class.java)

}

class ParallelGatewaySymbol(node: Branching, parent: Element<out Branching,*>): GatewaySymbol<ParallelGateway>(node, parent) {

    override val model = process.model.newInstance(ParallelGateway::class.java)

}

class EventBasedGatewaySymbol(node: Branching, parent: Element<out Branching,*>): GatewaySymbol<EventBasedGateway>(node, parent) {

    override val model = process.model.newInstance(EventBasedGateway::class.java)

}