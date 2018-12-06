package io.factdriven.flow.lang

import io.factdriven.flow.FlowInstance
import io.factdriven.flow.FlowMessage

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

enum class FlowActionType {

    acceptance,
    progress,
    success,
    fix,
    failure,
    intent

}

interface ClassifiedFlowAction {

    infix fun by(action: () -> Any)

}

interface FlowAction<I: FlowInstance> : ClassifiedFlowAction {

    infix fun intent(id: String): ClassifiedFlowAction
    infix fun acceptance(id: String): ClassifiedFlowAction
    infix fun progress(id: String): ClassifiedFlowAction
    infix fun success(id: String): ClassifiedFlowAction
    infix fun failure(id: String): ClassifiedFlowAction

}

open class FlowActionImpl<I: FlowInstance>: ClassifiedFlowAction, FlowAction<I>, FlowActionDefinition {

    // Flow Action Definition

    override var name = ""
    override var actionType = FlowActionType.success
    override var function: (() -> FlowMessage)? = null

    // Flow Action Factories

    override infix fun intent(name: String): ClassifiedFlowAction {
        this.actionType = FlowActionType.intent
        this.name = name
        return this
    }

    override infix fun acceptance(id: String): ClassifiedFlowAction {
        this.actionType = FlowActionType.acceptance
        this.name = id
        return this
    }

    override infix fun progress(id: String): ClassifiedFlowAction {
        this.actionType = FlowActionType.progress
        this.name = id
        return this
    }

    override infix fun success(id: String): ClassifiedFlowAction {
        this.actionType = FlowActionType.success
        this.name = id
        return this
    }

    override infix fun failure(id: String): ClassifiedFlowAction {
        this.actionType = FlowActionType.failure
        this.name = id
        return this
    }

    override fun by(message: () -> FlowMessage) {
        this.function = message
    }

}
