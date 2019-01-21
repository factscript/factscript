package io.factdriven.flow.lang


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowActionType {

    Acceptance,
    Progress,
    Success,
    Fix,
    Failure,
    Intent

}

interface ClassifiedFlowAction<I: FlowInstance> {

    infix fun by(message: I.() -> Any)

}

interface FlowAction<I: FlowInstance> : ClassifiedFlowAction<I> {

    infix fun intent(id: String): ClassifiedFlowAction<I>
    infix fun acceptance(id: String): ClassifiedFlowAction<I>
    infix fun progress(id: String): ClassifiedFlowAction<I>
    infix fun success(id: String): ClassifiedFlowAction<I>
    infix fun failure(id: String): ClassifiedFlowAction<I>

    fun asDefinition(): FlowActionDefinition {
        return this as FlowActionDefinition
    }

}

open class FlowActionImpl<I: FlowInstance>(override val parent: FlowDefinition): ClassifiedFlowAction<I>, FlowAction<I>, FlowActionDefinition {

    // Flow Action Definition

    override var name = ""
    override var type = FlowActionType.Success
    override var function: (FlowInstance.() -> FlowMessagePayload)? = null

    // Flow Action Factories

    override infix fun intent(name: String): ClassifiedFlowAction<I> {
        this.type = FlowActionType.Intent
        this.name = name
        return this
    }

    override infix fun acceptance(name: String): ClassifiedFlowAction<I> {
        this.type = FlowActionType.Acceptance
        this.name = name
        return this
    }

    override infix fun progress(name: String): ClassifiedFlowAction<I> {
        this.type = FlowActionType.Progress
        this.name = name
        return this
    }

    override infix fun success(name: String): ClassifiedFlowAction<I> {
        this.type = FlowActionType.Success
        this.name = name
        return this
    }

    override infix fun failure(name: String): ClassifiedFlowAction<I> {
        this.type = FlowActionType.Failure
        this.name = name
        return this
    }

    override fun by(message: I.() -> FlowMessagePayload) {
        @Suppress("UNCHECKED_CAST")
        this.function = message as FlowInstance.() -> FlowMessagePayload
    }

}
