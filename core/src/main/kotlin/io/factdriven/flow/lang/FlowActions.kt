package io.factdriven.flow.lang

import io.factdriven.flow.FlowInstance

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

enum class FlowActionType {
    acceptance, progress, success, fix, failure, intent
}

interface FlowActionMessage {

    infix fun by(action: () -> Any)

}

open class FlowActionImpl<I: FlowInstance, M: Any>: FlowNode,
    FlowActionMessage {

    override var id = ""

    var actionType = FlowActionType.success
    var action: (() -> Any)? = null

    infix fun intent(id: String): FlowActionMessage {
        actionType = FlowActionType.intent
        this.id = id
        return this
    }

    infix fun acceptance(id: String): FlowActionMessage {
        actionType = FlowActionType.acceptance
        this.id = id
        return this
    }

    infix fun progress(id: String): FlowActionMessage {
        actionType = FlowActionType.progress
        this.id = id
        return this
    }

    infix fun success(id: String): FlowActionMessage {
        actionType = FlowActionType.success
        this.id = id
        return this
    }

    infix fun failure(id: String): FlowActionMessage {
        actionType = FlowActionType.failure
        this.id = id
        return this
    }

    override fun by(action: () -> Any) {
        this.action = action
    }

}
