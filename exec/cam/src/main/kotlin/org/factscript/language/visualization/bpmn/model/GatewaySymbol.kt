package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.Branching
import org.factscript.language.definition.Node
import org.factscript.language.visualization.bpmn.diagram.*
import org.factscript.language.impl.definition.positionSeparator
import org.factscript.language.impl.utils.asLines
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class GatewaySymbol<OUT: Gateway>(node: Node, parent: Group<out Node>): Symbol<Node, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(50, 50, 18)

    override val id: String get() = "${super.id}${positionSeparator}${if (parent.elements.indexOf(this) == 0) "Fork" else "Join"}"

    override fun initModel() {
        super.initModel()
        if (parent.elements.indexOf(this) == 0) {
            if (parent is Branch)
                model.setAttributeValue("name", node.description.asLines(), false)
        } else {
            if (parent is Loop)
                model.setAttributeValue("name", node.description.asLines(), false)
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