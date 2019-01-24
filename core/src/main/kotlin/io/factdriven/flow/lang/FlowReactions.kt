package io.factdriven.flow.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {

    Message

}

class FlowReactions<I: Aggregate>(val parent: FlowDefinition) {

    infix fun <M: Any> message(type: KClass<M>): FlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M, Any>(parent, type)
        (parent.children as MutableList).add(reaction as FlowElement) // TODO clean
        return reaction

    }

    infix fun compensation(execution: FlowExecution<I>.() -> Unit): FlowExecution<I> {
        TODO()
    }

    infix fun timeout(timer: String): FlowReaction<I, Any> {
        TODO()
    }

    infix fun timeout(timer: () -> String): FlowReaction<I, Any> {
        TODO()
    }

}

interface FlowReaction<I: Aggregate, IN: Message> {

    infix fun <OUT: Message> create(action: FlowReactionAction<IN, OUT>): ActionableFlowReaction<I, IN, OUT>
    infix fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction
    infix fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    fun asDefinition(): FlowReactionDefinition {
        return this as FlowReactionDefinition
    }

}

interface FlowMessageReaction<I: Aggregate, M: Message> : FlowReaction<I, M> {

    infix fun having(property: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M>

    override fun asDefinition(): FlowMessageReactionDefinition {
        return this as FlowMessageReactionDefinition
    }

}

interface MatchableFlowMessageReaction<I: Aggregate, M: Message> {

    infix fun match(value: I.() -> Any?): FlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: Aggregate, IN: Message, OUT: Message> {

    infix fun by(reaction: I.(IN) -> OUT)

}

abstract class FlowReactionImpl<I: Aggregate, IN: Message, OUT: Message>(override val parent: FlowDefinition, override var flowElementType: String): FlowReactionDefinition, FlowReaction<I, IN>, ActionableFlowReaction<I, IN, OUT> {

    // Flow Reaction Definition

    override var flowReactionType = FlowReactionType.Message
    override lateinit var flowReactionAction: FlowReactionAction<*, *>

    // Flow Reaction Action Factory

    override infix fun <OUT: Message> create(action: FlowReactionAction<IN, OUT>): ActionableFlowReaction<I, IN, OUT> {
        this.flowReactionAction = action
        return this as ActionableFlowReaction<I, IN, OUT>
    }

    override fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction {
        this.flowReactionAction = action as FlowReactionAction<*, *>
        return action
    }

    override fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun by(reaction: I.(IN) -> OUT) {
        @Suppress("UNCHECKED_CAST")
        this.flowReactionAction.function = reaction as Aggregate.(Any) -> Message
    }

}

class FlowMessageReactionImpl<I: Aggregate, IN: Message, OUT: Message>(override val parent: FlowDefinition, override val type: KClass<IN>): FlowMessageReactionDefinition, FlowReactionImpl<I, IN, OUT>(parent, type.simpleName!!), FlowMessageReaction<I, IN>, MatchableFlowMessageReaction<I, IN> {

    override val propertyNames = mutableListOf<PropertyName>()
    override val propertyValues = mutableListOf<Aggregate?.() -> Any?>()

    init {
        flowReactionType = FlowReactionType.Message
    }

    // Message Patterns Refiner

    override fun having(property: String): MatchableFlowMessageReaction<I, IN> {
        assert(type.java.kotlin.memberProperties.find { it.name == property } != null)
            { "Message flowActionType '${type.simpleName}' does not have property '${property}'!" }
        propertyNames.add(property)
        return this
    }

    override fun match(value: I.() -> Any?): FlowMessageReaction<I, IN> {
        @Suppress("UNCHECKED_CAST")
        propertyValues.add(value as Aggregate?.() -> Any?)
        return this
    }

    override infix fun supporting(assertion: I.(IN) -> Boolean): FlowMessageReaction<I, IN> {
        TODO()
    }

}
