package io.factdriven.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Gateway: Node {

    val gatewayType: GatewayType

}

enum class GatewayType { Exclusive }

open class GatewayImpl(parent: Node): Gateway, NodeImpl(parent) {

    override lateinit var gatewayType: GatewayType

}