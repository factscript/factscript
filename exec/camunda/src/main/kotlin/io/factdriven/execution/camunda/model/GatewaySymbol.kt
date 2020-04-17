package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.execution.camunda.model.BpmnModel.Companion.margin
import io.factdriven.impl.definition.positionSeparator
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class GatewaySymbol<OUT: Gateway>(node: Branching, parent: Element<out Branching, *>): Symbol<Branching, OUT>(node, parent) {

    override val dimension: Dimension = Dimension(
        width = 50 + margin.width * 2,
        height = 50 + margin.height * 2
    )

    override fun position(child: Element<*,*>): Position {
        return position + Position(0, node.label.toCharArray().filter { it == ' '  }.size * - 13)
    }

    override fun init() {
        super.init()
        if (parent!!.children.indexOf(this) == 0) {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Fork", false)
            model.setAttributeValue("name", node.label.sentenceCase().replace(" ", "\n"), false)
        } else {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Join", false)
        }
    }

    override fun wayPoints(path: Path): List<Position> {
        if (path.parent!!.position.y + path.parent.entry(Direction.West).y == position.y + entry(Direction.West).y) {
            if (path.from == this) {
                return listOf(position + entry(Direction.East) - Dimension(margin.width, 0))
            } else {
                return listOf(position + entry(Direction.West) + Dimension(margin.width, 0))
            }
        } else {
            if (path.from == this) {
                val from = position + entry(Direction.South) - Dimension(0, margin.width)
                val to = path.parent.position + path.parent.entry(Direction.West) + Dimension(margin.width, 0)
                return listOf(from, Position(from.x, to.y))
            } else {
                val from = path.parent.position + path.parent.entry(Direction.East) - Dimension(margin.width, 0)
                val to = position + entry(Direction.South) - Dimension(0, margin.width)
                return listOf(Position(to.x, from.y), to)
            }
        }
    }

}

class ExclusiveGatewaySymbol(node: Branching, parent: Element<out Branching,*>): GatewaySymbol<ExclusiveGateway>(node, parent) {

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