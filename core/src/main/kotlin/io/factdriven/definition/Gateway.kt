package io.factdriven.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Gateway: Child {

    val gatewayType: GatewayType

}

enum class GatewayType { Exclusive }

open class GatewayImpl(parent: Node): Gateway, ChildImpl(parent) {

    override lateinit var gatewayType: GatewayType

}