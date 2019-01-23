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

        val reaction = FlowMessageReactionImpl<I, M>(parent, type)
        parent.flowElements.add(reaction as FlowElement)
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

interface FlowReaction<I: Aggregate, A: Any> {

    infix fun create(action: FlowReactionAction<A>): ActionableFlowReaction<I, A>
    infix fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    fun asDefinition(): FlowReactionDefinition {
        return this as FlowReactionDefinition
    }

}

interface FlowMessageReaction<I: Aggregate, M: FlowMessagePayload> : FlowReaction<I, M> {

    infix fun having(property: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M>

    override fun asDefinition(): FlowMessageReactionDefinition {
        return this as FlowMessageReactionDefinition
    }

}

interface MatchableFlowMessageReaction<I: Aggregate, M: FlowMessagePayload> {

    infix fun match(value: I.() -> Any?): FlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: Aggregate, A: Any> {

    infix fun by(reaction: I.(A) -> FlowMessagePayload)

}

abstract class FlowReactionImpl<I: Aggregate, A: Any>(override val container: FlowDefinition, override var flowElementType: String): FlowReactionDefinition, FlowReaction<I, A>, ActionableFlowReaction<I, A> {

    // Flow Reaction Definition

    override var flowReactionType = FlowReactionType.Message
    override lateinit var flowReactionAction: FlowReactionAction<A>

    // Flow Reaction Action Factory

    override infix fun create(action: FlowReactionAction<A>): ActionableFlowReaction<I, A> {
        this.flowReactionAction = action
        return this
    }

    override fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun by(reaction: I.(A) -> FlowMessagePayload) {
        @Suppress("UNCHECKED_CAST")
        this.flowReactionAction.function = reaction as Aggregate.(Any) -> FlowMessagePayload
    }

}

class FlowMessageReactionImpl<I: Aggregate, M: FlowMessagePayload>(override val container: FlowDefinition, override val payloadType: KClass<M>): FlowMessageReactionDefinition, FlowReactionImpl<I, M>(container, payloadType.simpleName!!), FlowMessageReaction<I, M>, MatchableFlowMessageReaction<I, M> {

    override val keys = mutableListOf<FlowMessageProperty>()
    override val values = mutableListOf<Aggregate.() -> Any?>()

    init {
        flowReactionType = FlowReactionType.Message
    }

    // Message Patterns Refiner

    override fun having(property: String): MatchableFlowMessageReaction<I, M> {
        assert(payloadType.java.kotlin.memberProperties.find { it.name == property } != null)
            { "Message flowActionType '${payloadType.simpleName}' does not have property '${property}'!" }
        keys.add(property)
        return this
    }

    override fun match(value: I.() -> Any?): FlowMessageReaction<I, M> {
        @Suppress("UNCHECKED_CAST")
        values.add(value as Aggregate.() -> Any?)
        return this
    }

    override infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M> {
        TODO()
    }

}
