package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

enum class FlowActionType {
    acceptance, progress, success, fix, failure, intent
}

interface FlowActionMessage {

    infix fun by(action: () -> Any)

}

open class FlowActionImpl<I: FlowInstance, M: Any>: FlowNode, FlowActionMessage {

    override var name = ""

    var actionType = FlowActionType.success
    var action: (() -> Any) = {}

    infix fun intent(name: String): FlowActionMessage {
        actionType = FlowActionType.intent
        this.name = name
        return this
    }

    infix fun acceptance(name: String): FlowActionMessage {
        actionType = FlowActionType.acceptance
        this.name = name
        return this
    }

    infix fun progress(name: String): FlowActionMessage {
        actionType = FlowActionType.progress
        this.name = name
        return this
    }

    infix fun success(name: String): FlowActionMessage {
        actionType = FlowActionType.success
        this.name = name
        return this
    }

    infix fun failure(name: String): FlowActionMessage {
        actionType = FlowActionType.failure
        this.name = name
        return this
    }

    override fun by(action: () -> Any) {
        this.action = action
    }

}
