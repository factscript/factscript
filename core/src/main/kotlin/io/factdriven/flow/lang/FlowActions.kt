package io.factdriven.flow.lang

import kotlin.reflect.KClass


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

interface ClassifiedFlowAction<I: Aggregate, O: Message> {

    infix fun by(message: I.() -> O?)

}

interface FlowAction<I: Aggregate> {

    infix fun intent(id: String)
    infix fun acceptance(id: String)
    infix fun progress(id: String)
    infix fun success(id: String)
    infix fun failure(id: String)

    infix fun <O: Message> intent(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Message> acceptance(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Message> progress(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Message> success(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Message> failure(type: KClass<O>): ClassifiedFlowAction<I, O>

    fun asDefinition(): FlowActionDefinition {
        return this as FlowActionDefinition
    }

}

open class FlowActionImpl<I: Aggregate, O: Message>(override val parent: FlowDefinition): ClassifiedFlowAction<I, O>, FlowAction<I>, FlowActionDefinition {

    // Flow Action Definition

    override var flowElementType = ""
    override var flowActionType = FlowActionType.Success
    override var function: (Aggregate.() -> Message)? = null

    // Flow Action Factories

    override infix fun intent(name: String) {
        this.flowActionType = FlowActionType.Intent
        this.flowElementType = name
    }

    override infix fun <O: Message> intent(type: KClass<O>): ClassifiedFlowAction<I, O> {
        intent(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun acceptance(name: String) {
        this.flowActionType = FlowActionType.Acceptance
        this.flowElementType = name
    }

    override infix fun <O: Message> acceptance(type: KClass<O>): ClassifiedFlowAction<I, O> {
        acceptance(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun progress(name: String) {
        this.flowActionType = FlowActionType.Progress
        this.flowElementType = name
    }

    override infix fun <O: Message> progress(type: KClass<O>): ClassifiedFlowAction<I, O> {
        progress(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun success(name: String) {
        this.flowActionType = FlowActionType.Success
        this.flowElementType = name
    }

    override infix fun <O: Message> success(type: KClass<O>): ClassifiedFlowAction<I, O> {
        success(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun failure(name: String) {
        this.flowActionType = FlowActionType.Failure
        this.flowElementType = name
    }

    override infix fun <O: Message> failure(type: KClass<O>): ClassifiedFlowAction<I, O> {
        failure(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override fun by(message: I.() -> O?) {
        @Suppress("UNCHECKED_CAST")
        this.function = message as Aggregate.() -> Message
    }

}
