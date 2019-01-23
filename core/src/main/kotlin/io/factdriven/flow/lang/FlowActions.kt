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

interface ClassifiedFlowAction<I: Aggregate> {

    infix fun by(message: I.() -> Any)

}

interface FlowAction<I: Aggregate> : ClassifiedFlowAction<I> {

    infix fun intent(id: String): ClassifiedFlowAction<I>
    infix fun acceptance(id: String): ClassifiedFlowAction<I>
    infix fun progress(id: String): ClassifiedFlowAction<I>
    infix fun success(id: String): ClassifiedFlowAction<I>
    infix fun failure(id: String): ClassifiedFlowAction<I>

    fun asDefinition(): FlowActionDefinition {
        return this as FlowActionDefinition
    }

}

open class FlowActionImpl<I: Aggregate>(override val parent: FlowDefinition): ClassifiedFlowAction<I>, FlowAction<I>, FlowActionDefinition {

    // Flow Action Definition

    override var flowElementType = ""
    override var flowActionType = FlowActionType.Success
    override var function: (Aggregate.() -> Message)? = null

    // Flow Action Factories

    override infix fun intent(name: String): ClassifiedFlowAction<I> {
        this.flowActionType = FlowActionType.Intent
        this.flowElementType = name
        return this
    }

    override infix fun acceptance(name: String): ClassifiedFlowAction<I> {
        this.flowActionType = FlowActionType.Acceptance
        this.flowElementType = name
        return this
    }

    override infix fun progress(name: String): ClassifiedFlowAction<I> {
        this.flowActionType = FlowActionType.Progress
        this.flowElementType = name
        return this
    }

    override infix fun success(name: String): ClassifiedFlowAction<I> {
        this.flowActionType = FlowActionType.Success
        this.flowElementType = name
        return this
    }

    override infix fun failure(name: String): ClassifiedFlowAction<I> {
        this.flowActionType = FlowActionType.Failure
        this.flowElementType = name
        return this
    }

    override fun by(message: I.() -> Message) {
        @Suppress("UNCHECKED_CAST")
        this.function = message as Aggregate.() -> Message
    }

}
