package io.factdriven.definition.impl

import io.factdriven.definition.api.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class NodeImpl(override val parent: Node?, override val entityType: KClass<*> = parent!!.entityType):
    Node {

    override val children: MutableList<Node> = mutableListOf()
    override var label: String? = null

}