package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.definition.Node
import io.factdriven.execution.camunda.diagram.*
import io.factdriven.impl.definition.positionSeparator
import io.factdriven.impl.utils.asLines
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class GatewaySymbol<OUT: Gateway>(node: Node, parent: Group<out Node>): Symbol<Node, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(50, 50, 18)

    override fun initModel() {
        super.initModel()
        if (parent.children.indexOf(this) == 0) {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Fork", false)
            if (parent is Branch)
                model.setAttributeValue("name", node.label.asLines(), false)
        } else {
            model.setAttributeValue("id", model.getAttributeValue("id") + "${positionSeparator}Join", false)
            if (parent is Loop)
                model.setAttributeValue("name", node.label.asLines(), false)
        }
    }

}

class ExclusiveGatewaySymbol(node: Node, parent: Group<out Node>): GatewaySymbol<ExclusiveGateway>(node, parent) {

    override val model = process.model.newInstance(ExclusiveGateway::class.java)

    override fun initModel() {
        super.initModel()
        model.diagramElement.isMarkerVisible = true
    }

}

class InclusiveGatewaySymbol(node: Branching, parent: Group<out Branching>): GatewaySymbol<InclusiveGateway>(node, parent) {

    override val model = process.model.newInstance(InclusiveGateway::class.java)

}

class ParallelGatewaySymbol(node: Branching, parent: Group<out Branching>): GatewaySymbol<ParallelGateway>(node, parent) {

    override val model = process.model.newInstance(ParallelGateway::class.java)

}

class EventBasedGatewaySymbol(node: Branching, parent: Group<out Branching>): GatewaySymbol<EventBasedGateway>(node, parent) {

    override val model = process.model.newInstance(EventBasedGateway::class.java)

}