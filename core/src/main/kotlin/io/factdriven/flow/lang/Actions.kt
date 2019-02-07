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

interface ClassifiedFlowAction<I: Entity, O: Fact> {

    infix fun by(message: I.() -> O?)

}

interface FlowAction<I: Entity> {

    infix fun intent(id: String)
    infix fun acceptance(id: String)
    infix fun progress(id: String)
    infix fun success(id: String)
    infix fun failure(id: String)

    infix fun <O: Fact> intent(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Fact> acceptance(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Fact> progress(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Fact> success(type: KClass<O>): ClassifiedFlowAction<I, O>
    infix fun <O: Fact> failure(type: KClass<O>): ClassifiedFlowAction<I, O>

    fun asDefinition(): FlowActionDefinition {
        return this as FlowActionDefinition
    }

}

open class FlowActionImpl<I: Entity, O: Fact>(override val parent: FlowDefinition<*>): ClassifiedFlowAction<I, O>, FlowAction<I>, FlowActionDefinition {

    // Flow Action Definition

    override lateinit var name: ElementName
    override var messageType: FactType<*>? = null
    override var actionType = FlowActionType.Success
    override var function: (Entity.() -> Fact)? = null

    // Flow Action Factories

    override infix fun intent(name: String) {
        this.actionType = FlowActionType.Intent
        this.name = name
    }

    override infix fun <O: Fact> intent(type: KClass<O>): ClassifiedFlowAction<I, O> {
        this.messageType = type
        intent(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun acceptance(name: String) {
        this.actionType = FlowActionType.Acceptance
        this.name = name
    }

    override infix fun <O: Fact> acceptance(type: KClass<O>): ClassifiedFlowAction<I, O> {
        this.messageType = type
        acceptance(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun progress(name: String) {
        this.actionType = FlowActionType.Progress
        this.name = name
    }

    override infix fun <O: Fact> progress(type: KClass<O>): ClassifiedFlowAction<I, O> {
        this.messageType = type
        progress(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun success(name: String) {
        this.actionType = FlowActionType.Success
        this.name = name
    }

    override infix fun <O: Fact> success(type: KClass<O>): ClassifiedFlowAction<I, O> {
        this.messageType = type
        success(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override infix fun failure(name: String) {
        this.actionType = FlowActionType.Failure
        this.name = name
    }

    override infix fun <O: Fact> failure(type: KClass<O>): ClassifiedFlowAction<I, O> {
        this.messageType = type
        failure(type.simpleName!!)
        return this as ClassifiedFlowAction<I, O>
    }

    override fun by(message: I.() -> O?) {
        @Suppress("UNCHECKED_CAST")
        this.function = message as Entity.() -> Fact
    }

}
