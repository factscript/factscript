package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowExecution<I: FlowInstance>: FlowNode {

    lateinit var type: FlowExecutionType
    lateinit var definition: FlowDefinition<I>

    infix fun service(service: FlowDefinition<I>.() -> Unit): FlowDefinition<I>  {
        definition = FlowDefinition<I>().apply(service)
        type = FlowExecutionType.service
        return definition
    }

    infix fun receive(receive: Receive.() -> Unit): Receive = TODO()

}

enum class FlowExecutionType {

    service, receive

}

class Receive
