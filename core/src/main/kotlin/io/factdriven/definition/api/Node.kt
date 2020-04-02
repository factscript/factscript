package io.factdriven.definition.api

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val parent: Node?
    val label: String?
    val entityType: KClass<*>
    val children: List<Node>

}